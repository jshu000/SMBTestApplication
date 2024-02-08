package com.jio.smbtestapplication;

import static java.net.URLEncoder.encode;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Arrays;

import jcifs.CIFSContext;
import jcifs.Config;
import jcifs.context.SingletonContext;
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
        SmbFile smbFile = createSMBPath(new String[] {"192.168.29.159", "smbtest", "smbtest", "", "tarun"}, false, false);

        try {
            smbFile.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
            Log.d(TAG, "func: SMBFILE is directory-"+smbFile.getShare());

        } catch (SmbException e) {
            Log.d(TAG, "func: smbexception-"+e);
            e.printStackTrace();
        }
        try {
            Log.d(TAG, "func: before send request");
            sendRequest();
        } catch (Exception e) {
            Log.d(TAG, "func: smbexception-"+e);
            e.printStackTrace();
        }
        SmbHelper smbHelper=new SmbHelper();
        smbHelper.connectAndReadWrite("192.168.29.159",
                "smbtest", "smbtest","tarun");


    }
    public static void sendRequest() throws Exception {
        CIFSContext base = SingletonContext.getInstance();
        CIFSContext authed1 = base.withCredentials(new NtlmPasswordAuthentication(base, "192.168.29.159",
                "smbtest", "smbtest"));
        try (SmbFile f = new SmbFile("smb://192.168.29.159/tarun/", authed1)) {
            Log.d(TAG, "func: SMBFILE is filename-"+f.getPath());

        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // Update UI or handle results after the background operation completes
    }
}
public class SmbHelper {

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
}
