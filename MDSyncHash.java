package com.swift.swamdclient;

import java.security.MessageDigest;

/**
 * Created by root on 9/23/15.
 */
public class MDSyncHash {

    private static String SHAHash;


        public static void main(String[] args) {

            String s = "SecretKey20111013000_";
            String res = md5(s);
            System.out.println(res);


        }

    /**
     * Calculates the Message Digest Value based on MD5 Algorithm
     * @param toEncrypt the String to convert to a digest
     * @return a string of random length
     */


    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toHexString(0xFF & bytes[i]));

            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return "";
        }
    }


    }

