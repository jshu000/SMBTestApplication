package com.jio.smbtestapplication;

import static java.net.URLEncoder.encode;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


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
    public static final String ip="192.168.1.33";
    public static final String smbpath= "smb://192.168.1.33/tarun";

    @Override
    protected Void doInBackground(Void... voids) {
        Config.registerSmbURLHandler();
        func();
        // Perform your network operation here
        return null;
    }

    private SmbFile createSMBPath(String[] auth, boolean anonymous, boolean disableIpcSignCheck) {
        Log.d(TAG, "createSMBPath: ");
        try {
            String yourPeerIP = auth[0];
            String domain = auth[3];
            String share = auth[4];

            StringBuilder sb = new StringBuilder(SMB_URI_PREFIX);
            if (!TextUtils.isEmpty(domain)) sb.append(encode(domain + ';', Charsets.UTF_8.name()));
            if (!anonymous)
                sb.append(encode(auth[1], Charsets.UTF_8.name()))
                        .append(COLON)
                        .append(encode(auth[2], Charsets.UTF_8.name()))
                        .append(AT);
            sb.append(yourPeerIP).append(SLASH);
            if (!TextUtils.isEmpty(share)) {
                sb.append(share).append(SLASH);
            }
            Log.d(TAG, "createSMBPath: sb-"+sb.toString());

            return new SmbFile(
                    sb.toString(),
                    CifsContexts.createWithDisableIpcSigningCheck(sb.toString(), disableIpcSignCheck));
        } catch (MalformedURLException e) {
            Log.d(TAG, "createSMBPath: ERROR-"+e);
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.d(TAG, "createSMBPath: ERROR2-"+e);
        }
        return null;
    }

    void func(){

        Log.d(TAG, "func: ");
        String smbpath= "smb://smbtest:smbtest@192.168.29.159/";
        SmbFile smbFile = createSMBPath(new String[] {"192.168.1.33", "smbtest", "smbtest", "", "tarun"}, false, false);



        Log.d(TAG, "func: SMBFILEcreated-"+smbFile.toString());

        try {
            Log.d(TAG, "func: SMBFILE is file-"+smbFile.isFile());
        } catch (SmbException e) {
            e.printStackTrace();
        }
        try {
            Log.d(TAG, "func: SMBFILE is directory-"+smbFile.isDirectory());
            Log.d(TAG, "func: SMBFILE is directory-"+smbFile.getLastModified());
            Log.d(TAG, "func: SMBFILE is directory-"+smbFile.getDate());

        } catch (SmbException e) {
            Log.d(TAG, "func: smbexception-"+e);
            e.printStackTrace();
        }
        try {
            Log.d(TAG, "func: before send request");
            Thread thread2 = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        sendRequest();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread2.start();

        } catch (Exception e) {
            Log.d(TAG, "func: smbexception-"+e);
            e.printStackTrace();
        }
        /*SmbHelper smbHelper=new SmbHelper();
        smbHelper.connectAndReadWrite("192.168.29.159",
                "smbtest", "smbtest","tarun");*/


    }
    public static void sendRequest() throws Exception {
        CIFSContext base = SingletonContext.getInstance();
        CIFSContext authed2 = base.withCredentials(new NtlmPasswordAuthenticator("",
                "smbtest", "smbtest"));
        /*CIFSContext authed1 = base.withCredentials(new NtlmPasswordAuthentication(base, "",
                "smbtest", "smbtest"));*/
        try {
            //CIFSContext baseContext = new BaseContext(base.getConfig());
            //NtlmPasswordAuthenticator authenticator = new NtlmPasswordAuthenticator(null, "smbtest", "password");

            SmbFile smbFile = new SmbFile(smbpath, authed2);

            Log.d(TAG, "sendRequest: smbfile created");
            if (smbFile.exists()) {
                if (smbFile.isDirectory()) {

                    Log.d(TAG, "sendRequest: Listing contents of the directory:");
                    System.out.println("Listing contents of the directory:");
                    for (SmbFile file : smbFile.listFiles()) {
                        Log.d(TAG, "sendRequest: file name -"+file.getName());
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
        /*try  {
            SmbFile f = new SmbFile(smbpath, authed1);
            Log.d(TAG, "sendRequest: auth1-"+f.getPath());
            Log.d(TAG, "sendRequest: auth1-"+f.getName());
            Log.d(TAG, "sendRequest: auth1-"+f.getLastModified());
            Log.d(TAG, "func: SMBFILE is auth1 filename-"+f.getPath());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try  {
            SmbFile f = new SmbFile(smbpath, authed2);
            Log.d(TAG, "sendRequest: auth1-"+f.getPath());
            Log.d(TAG, "sendRequest: auth1-"+f.getName());
            Log.d(TAG, "sendRequest: auth1-"+f.getLastModified());
            Log.d(TAG, "func: SMBFILE is auth1 filename-"+f.getPath());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try (SmbFile f = new SmbFile("smb://192.168.1.33/tarun/", authed1)) {
            Log.d(TAG, "sendRequest: auth1-"+f.getPath());
            Log.d(TAG, "sendRequest: auth1-"+f.getName());
            f.createNewFile();
            Log.d(TAG, "sendRequest: auth1 getLastModified()-");
            Log.d(TAG, "func: SMBFILE is auth1 filename-"+f.getPath());

        }
        try {
            SmbFile f = new SmbFile("smb://192.168.1.33/tarun/", authed2);
            Log.d(TAG, "sendRequest: auth2-"+f.getPath());
            Log.d(TAG, "sendRequest: auth2-"+f.getName());
            Log.d(TAG, "func: SMBFILE is auth2 filename-"+f.getPath());
            Log.d(TAG, "sendRequest: auth2-"+f.getLastModified());
            *//*SmbFile[] files = f.listFiles();

            // Print the names of the files
            for (SmbFile file : files) {
                System.out.println(file.getName());
            }*//*
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // Update UI or handle results after the background operation completes
    }
}
/*public class SmbHelper {

    public static void connectAndReadWrite(String ipAddress, String username, String password, String filePath) {
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null,"smbtest", "smbtest");
        SmbFile smbFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            smbFile = new SmbFile("smb://" +"192.168.29.159" + "/" + "tarun", (CIFSContext) auth);

            // Read from SMB file
            inputStream = new SmbFileInputStream(smbFile);
            // Here you can read from inputStream as needed

            // Write to SMB file
            outputStream = new SmbFileOutputStream(smbFile);
            // Here you can write to outputStream as needed
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams and disconnect from SMB share
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (smbFile != null) smbFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}*/
