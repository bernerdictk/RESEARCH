package com.swift.swamdclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.swift.swamdclient.DBAdapter;
import com.swift.swamdclient.R;




import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Demo application to show how to use the 
 * built-in SQLite database with a cursor to populate
 * a ListView.
 */
public class MainActivity extends Activity {
    int[] imageIDs = {
            R.drawable.bug,
            R.drawable.down,
            R.drawable.fish,
            R.drawable.heart,
            R.drawable.help,
            R.drawable.lightning,
            R.drawable.star,
            R.drawable.up
    };
    int nextImageIndex = 0;

    // using the ANDROID_ID constant, generated at the first device boot as DeviceID
    String deviceId;
    DBAdapter myDb;
    DBAdapterMD myMsgDigestDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        openDB();
        populateListViewFromDB();
        registerListClickCallback();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void openDB() {
        myDb = new DBAdapter(this);

        myDb.open();

       // myMsgDigestDB = new DBAdapterMD(this);

     //   myMsgDigestDB.open();

    }
    private void closeDB() {
        myDb.close();

    }

    /*
     * UI Button Callbacks
     */
    public void onClick_AddRecord(View v) {
        int imageId = imageIDs[nextImageIndex];
        nextImageIndex = (nextImageIndex + 1) % imageIDs.length;

        // Add it to the DB and re-draw the ListView
       long newID =  myDb.insertRow("Jenny" + nextImageIndex, imageId, "Green");

        // Now calculate the message Digest for the newly inserted row..., where mobileID = ?(try using the MAC/Serial Number from variable??)
        // Inserting a new record in Data Table means this device has not yet sync with Server



        String toDigest = "Jenny" + nextImageIndex + imageId + "Green";

        // using the ANDROID_ID constant, generated at the first device boot as DeviceID

        Log.d("DEVICE-ID:",deviceId+" *******************************8");
        myDb.insertRow(MDSyncHash.md5(toDigest), newID, deviceId, 1);


        populateListViewFromDB();
    }

    public void onClick_ClearAll(View v) {

        /***
         * On deleting rows, A new request has to be sent to the server,
         *  with parameters deviceID and an '*' to indicate that this device
         *  has deleted all their data from the database! MAKE A NEW Volley Request!
         */
        confirmDialog();



    }
    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setMessage("Are you sure about this?")
                .setPositiveButton("Yes",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Yes-code
                        myDb.deleteAll();
                        myDb.deleteAllMDRows();

                        databaseCleared();

                        populateListViewFromDB();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }



    /**
     * When this Button is clicked, we want to synchronize our records by sending
     *  an httpRequest to the server using MDSync Algorithm as follows
     *  1.
     * @param view
     */
    public void onClick_SyncRecords(View view){


        //Next Send data to the server
        methodTest();

    }


    private void populateListViewFromDB() {
        Cursor cursor = myDb.getAllRows();

        // Allow activity to manage lifetime of the cursor.
        // DEPRECATED! Runs on the UI thread, OK for small/short queries.
        startManagingCursor(cursor);

        // Setup mapping from cursor to view fields:
        String[] fromFieldNames = new String[]
                {DBAdapter.KEY_NAME, DBAdapter.KEY_STUDENTNUM, DBAdapter.KEY_FAVCOLOUR, DBAdapter.KEY_STUDENTNUM};



        int[] toViewIDs = new int[]
                {R.id.item_name,     R.id.item_icon,           R.id.item_favcolour,     R.id.item_studentnum};

        // Create adapter to may columns of the DB onto elemesnt in the UI.
        SimpleCursorAdapter myCursorAdapter =
                new SimpleCursorAdapter(
                        this,		// Context
                        R.layout.item_layout,	// Row layout template
                        cursor,					// cursor (set of DB records to map)
                        fromFieldNames,			// DB Column names
                        toViewIDs				// View IDs to put information in
                );

        // Set the adapter for the list view
        ListView myList = (ListView) findViewById(R.id.listViewFromDB);
        myList.setAdapter(myCursorAdapter);
    }

    private void registerListClickCallback() {
        ListView myList = (ListView) findViewById(R.id.listViewFromDB);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long idInDB) {

                updateItemForId(idInDB);
                displayToastForId(idInDB);
            }
        });
    }

    private void updateItemForId(long idInDB) {
        Cursor cursor = myDb.getRow(idInDB);

        if (cursor.moveToFirst()) {
            long idDB = cursor.getLong(DBAdapter.COL_ROWID);
            String name = cursor.getString(DBAdapter.COL_NAME);

            int studentNum = cursor.getInt(DBAdapter.COL_STUDENTNUM);
            String favColour = cursor.getString(DBAdapter.COL_FAVCOLOUR);

            favColour += "!";
            myDb.updateRow(idInDB, name, studentNum, favColour);
            //this is where the messageDigest value has to be recalculated
            String toDigest = name + favColour + studentNum;

            myDb.updateDigest(MDSyncHash.md5(toDigest), idInDB);

        }
        cursor.close();
        populateListViewFromDB();
    }

    private void displayToastForId(long idInDB) {
        Cursor cursor = myDb.getRow(idInDB);
        if (cursor.moveToFirst()) {
            long idDB = cursor.getLong(DBAdapter.COL_ROWID);
            String name = cursor.getString(DBAdapter.COL_NAME);
            int studentNum = cursor.getInt(DBAdapter.COL_STUDENTNUM);
            String favColour = cursor.getString(DBAdapter.COL_FAVCOLOUR);

            String message = "ID: " + idDB + "\n"
                    + "Name: " + name + "\n"
                    + "Std#: " + studentNum + "\n"
                    + "FavColour: " + favColour;
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
        cursor.close();
    }




    private void databaseCleared() {

            RequestQueue queue = Volley.newRequestQueue(this);

             String url = "http://10.0.2.2:8080/mdsync/SyncRecord";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    // Posting parameters to the Servlet url

                    params.put("DeviceID_", deviceId);
                    params.put("flag_", "*");

                    return params;
                }
            };
            queue.add(postRequest);

    }





    private void methodTest() {
        RequestQueue queue = Volley.newRequestQueue(this);

        final Cursor cursor = myDb.getRowsForSynchronization();// (works!!!)go on and create a method that returns only those rows whose flag = 1(true)
        String url = "http://10.0.2.2:8080/mdsync/SyncRecord";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                // Posting parameters to update url

                ArrayList<String> namelist = new ArrayList<>();
                ArrayList<String> studIDList = new ArrayList<>();
                ArrayList<String> colorlist = new ArrayList<>();

                ArrayList<String> digests = new ArrayList<>();
                ArrayList<String> idMains = new ArrayList<>();
                ArrayList<String> flags = new ArrayList<>();

                cursor.move(-1);
               // cursor.
                //before posting params, check if flag value is 1?
                while ( cursor.moveToNext() ) {
                    Log.d("Here It Is","In the looooogggggggeere");
                    //Next Step initialize fields
                   // params.put("name", cursor.getString(DBAdapter.COL_NAME));
                   // params.put("studNUM", cursor.getString(DBAdapter.COL_STUDENTNUM));
                //    params.put("passwd_", cursor.getString(DBAdapter.COL_FAVCOLOUR));

                    namelist.add(cursor.getString(0));
                    studIDList.add(cursor.getString(1));
                    colorlist.add(cursor.getString(2));
                    digests.add(cursor.getString(3));
                    idMains.add(cursor.getString(4));
                    flags.add(cursor.getString(5));

                    Log.d("mdValues TAG:", cursor.getString(3));
                    Log.d("Flags TAG:", cursor.getString(5));


                }
                String tmp = "";
                String tmp2 = "";
                String tmp3= "";
                String tmp4 = "";/**/
                for (int i=0; i< namelist.size(); i++) {
                    Log.d("The NAMES----!- ", namelist.get(i).toString() );
                    Log.d("The IDS----!- ", studIDList.get(i).toString());
                    Log.d("The COLORS----!- ", colorlist.get(i).toString());
                     tmp += namelist.get(i).toString();
                     tmp2 += studIDList.get(i).toString();
                    tmp3 += colorlist.get(i).toString();
                    tmp4 += digests.get(i).toString();

                    params.put("NAMES", tmp);
                    params.put("StudentIDs", tmp2);
                    params.put("Colors", tmp3);
                    params.put("DigestValues", tmp4);

                    //After Posting params, set flag value to 0 . . . .(Preparing METHOD to do this(DONE Below!!)..!! )
                    //You need to also send the mdvalue(foreach row) and the deviceID
                    //they will be used for checking with server-side
                    myDb.updateRow( digests.get(i).toString(), Integer.parseInt(idMains.get(i)), deviceId, 1);
                }

                params.put("DeviceID", deviceId);
                cursor.close();
                return params;
            }
        };
        queue.add(postRequest);
    }






}













