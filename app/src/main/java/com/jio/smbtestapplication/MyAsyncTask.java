package com.jio.smbtestapplication;

import static java.net.URLEncoder.encode;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;

import jcifs.CIFSContext;
import jcifs.Config;
import jcifs.context.BaseContext;
import jcifs.context.SingletonContext;
import jcifs.internal.SmbBasicFileInfo;
import jcifs.internal.util.SMBUtil;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import kotlin.text.Charsets;

public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "JASHWANT";
    public static final char AND= '&';
    public static final char AT = '@';
    public static final char SLASH = '/';
    public static final char COLON = ':';
    public static final String SMB_URI_PREFIX= "smb://";
    public static final String user="smbtest";
    public static final String pass="smbtest";
    public static final String ip="192.168.29.159";
    public static final String smbpath= "smb://192.168.29.159/";
    private static SmbFile rootsmb = null;

    @Override
    protected Void doInBackground(Void... voids) {
        Config.registerSmbURLHandler();
        try {
            sendRequest();
        } catch (Exception e) {
            Log.d(TAG, "sendrequest exception: "+e);
            e.printStackTrace();
        }
        // Perform your network operation here
        return null;
    }


    public static void sendRequest() throws Exception {
        CIFSContext base = SingletonContext.getInstance();
        CIFSContext authed2 = base.withCredentials(new NtlmPasswordAuthenticator("",
                "smbtest", "smbtest"));
        /*CIFSContext authed1 = base.withCredentials(new NtlmPasswordAuthentication(base, "",
                "smbtest", "smbtest"));*/
        try {

            SmbFile smbFile = new SmbFile(smbpath, authed2);
            rootsmb= smbFile;

            Log.d(TAG, "sendRequest: smbfile created");
            if (smbFile.exists()) {
                if (smbFile.isDirectory()) {

                    Log.d(TAG, "sendRequest: Listing contents of the directory:");
                    System.out.println("Listing contents of the directory:");
                    for (SmbFile file : smbFile.listFiles()) {
                        Log.d(TAG, "sendRequest: file name -" + file.getName());
                        System.out.println(file.getName());
                    }
                } else {
                    System.out.println("Downloading file...");
                    try (InputStream in = smbFile.getInputStream();
                         OutputStream out = new FileOutputStream("tarun")) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        System.out.println("File downloaded successfully!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("SMB file does not exist.");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        }
    }
    public boolean setLastModified(final long date) {
            try {
                SmbFile smbFile = rootsmb;
                if (smbFile != null) {
                    smbFile.setLastModified(date);
                    Log.d(TAG, "sendRequest: smbfile setlastmodified-"+date);
                    return true;
                } else {
                    return false;
                }
            } catch (SmbException e) {
                Log.d(TAG, "sendRequest: smbfile Exception-"+e);
                return false;
            }
        }
    public void mkdir(Context context) {
            try {
                rootsmb.mkdirs();
            } catch (SmbException e) {
                Log.d(TAG, "sendRequest: smbfile Exception-"+e);
            }
    }
    public boolean delete(Context context, boolean rootmode)
            throws SmbException {
        try {
            rootsmb.delete();
            return true;
        } catch (SmbException e) {
            Log.d(TAG, "sendRequest: smbfile Exception-"+e);
            throw e;
        }

    }
}
