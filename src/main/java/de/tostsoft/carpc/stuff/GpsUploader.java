package de.tostsoft.carpc.stuff;

import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.application.Platform;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class GpsUploader {

    private String gpsLogdir = null;
    private String keqstorePW;
    private String keystorePW2;
    private String keystoreFilename;
    //private static final String GPS_LOG_DIR = "ressources/";
    private static final String CURRENT_FILE_LOG = "actual.log";
    private static final String [] needetJsonParams = {"time","lat","lon","alt","epx","epy","epv","track","speed","climb","eps","epc"};

    private String uploadUrl = null;
    private static int NUM_PER_SEND = 1000;

    private ArrayList<GpsSizeChangeListener> sizeListener = new ArrayList<>();
    private Thread checkGpsSizeThread = null;
    private long lastAllFolderSize = 0;
    private long lastCurrentFileSize = 0;

    private UpdateLocker updateLocker;

    private Boolean delteGpsLoggs = true;

    private String uploadUserId = null;

    public GpsUploader(UpdateLocker updateLocker){
        this.updateLocker = updateLocker;
        HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);

        gpsLogdir = Tools.loadFromProperties("gps_dir");
        if(gpsLogdir != null && gpsLogdir.length() > 0 && !gpsLogdir.endsWith("/")){
            gpsLogdir+="/";
        }
        uploadUrl = Tools.loadFromProperties("gps_upload_url");
        keqstorePW = Tools.loadFromProperties("gps_upload_keystore_pw");
        keystorePW2 = Tools.loadFromProperties("gps_upload_keystore_pw_2");
        keystoreFilename = Tools.loadFromProperties("gps_upload_keystore");

        if(gpsLogdir == null || uploadUrl == null || keqstorePW == null || keystorePW2 == null || keystoreFilename == null){
            throw new RuntimeException("Config paramter(s) for GpsUploader missing.");
        }
    }

    private String getValueFromJsonString(final String line, final String value){
        String[] arr = line.split("\""+value+"\":");
        if(arr.length <2){
            return null;
        }
        return arr[1].split(",")[0].split("}")[0];//not good but works
    }

    private Long finishAndSend(StringBuilder stringBuilder,boolean finished,Long key){
        stringBuilder.append("\n  ],\n  \"finished\":");
        stringBuilder.append(finished);
        stringBuilder.append(",\n  \"counter\":");
        stringBuilder.append(key);
        stringBuilder.append(",\n  \"user\":");
        if(uploadUserId!=null) {
            stringBuilder.append("\"");
            stringBuilder.append(uploadUserId);
            stringBuilder.append("\"");
        }else{
            stringBuilder.append("null");
        }
        stringBuilder.append("\n}");

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(keystoreFilename);
            ks.load(fis, keqstorePW.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keystorePW2.toCharArray());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);

            URL url = new URL(uploadUrl);
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setSSLSocketFactory(sc.getSocketFactory());
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(stringBuilder.toString());
            wr.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = rd.readLine();
            wr.close();
            rd.close();
            if(line == null){
                return null;
            }
            return Long.parseLong(line);
        }catch(Exception ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Upload gps data failed:"+ ex.getMessage());
            return null;
        }
    }

    StringBuilder createStartResult(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"data\":[\n");
        return sb;
    }

    private boolean parseAndUploadFile(String pathToFile){
        try(BufferedReader bf = new BufferedReader(new FileReader(gpsLogdir+pathToFile))){
            StringBuilder stringBuilder = createStartResult();
            String line = bf.readLine();
            int count = 0;
            Long key = null;
            boolean outerFirst = true;
            boolean firstSend = true;
            while (line != null){
                if(!line.contains("\"mode\":3") || !line.contains("\"track\":")){
                    line = bf.readLine();
                    continue;//not valid value
                }
                if(outerFirst){
                    outerFirst = false;
                }else{
                    stringBuilder.append(",\n");
                }

                stringBuilder.append("    {");
                boolean first = true;
                for(String jsonValue: needetJsonParams){
                    String val = getValueFromJsonString(line,jsonValue);
                    if(val != null){
                        if(first){
                            first = false;
                        }else{
                            stringBuilder.append(",");
                        }
                        stringBuilder.append("\"");
                        stringBuilder.append(jsonValue);
                        stringBuilder.append("\"");
                        stringBuilder.append(":");
                        stringBuilder.append(val);
                    }
                }
                stringBuilder.append("}");
                line = bf.readLine();
                if(line != null && ++count == NUM_PER_SEND){
                    key = finishAndSend(stringBuilder,false,key);
                    if(key == null){
                        Logger.getInstance().log(Logger.Logtype.ERROR,"GPS HTTP Reqeust failed");
                        return false;//send failed
                    }
                    Logger.getInstance().log(Logger.Logtype.DEBUG,"Send finished with conter: "+key);
                    //reset all stats
                    stringBuilder = createStartResult();//reset result
                    outerFirst=true;
                    count = 0;
                    firstSend = false;
                }
            }
            if(firstSend && count == 0){
                Logger.getInstance().log(Logger.Logtype.DEBUG, "Skped GPS File with no valid Data");
                bf.close();
                delteGpsFile(gpsLogdir+pathToFile);
                return false;
            }else {
                if (finishAndSend(stringBuilder, true, key) == null) {
                    Logger.getInstance().log(Logger.Logtype.ERROR, "GPS HTTP Reqeust failed");
                    bf.close();
                    return false;//send failed
                }
                Logger.getInstance().log(Logger.Logtype.DEBUG, "GPS HTTP Route upload finished with id: " + key);
            }
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not open Gps-Rote: "+ex.getMessage());
            return false;
        }
        delteGpsFile(gpsLogdir+pathToFile);
        return true;
    }

    private void delteGpsFile(String file){
        if(!delteGpsLoggs){
            return;
        }
        File f = new File(file);
        if(!f.delete()){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not delete Json file");
        }
    }

    public int uploadFiles() {
        //todo make in thread
        //find ignoore file while still file in use
        updateLocker.setStatus(UpdateLocker.UpdateType.GPS,true);
        int numUploadet = 0;
        try {
            String skipFiles = "";
            File f = new File(gpsLogdir + CURRENT_FILE_LOG);
            if (f.exists()) {
                try {
                    BufferedReader brTest = new BufferedReader(new FileReader(f));
                    skipFiles = brTest.readLine();
                } catch (IOException ex) {
                    Logger.getInstance().log(Logger.Logtype.WARNING,"Could not find local gps file: "+ex.getMessage());
                }
            }

            List<String> files = Tools.getFilesByExtention(gpsLogdir, "json");
            for (String file : files) {
                if (file.equals(skipFiles)) {
                    continue;
                }
                if(parseAndUploadFile(file)) {
                    numUploadet++;
                }
            }
        } catch (Exception ex){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Error by uploading GPS-Routes: "+ex.getMessage());
        }
        updateLocker.setStatus(UpdateLocker.UpdateType.GPS,false);
        return numUploadet;
    }

    public synchronized void startChecking(){
        if(checkGpsSizeThread != null){
            return;
        }
        checkGpsSizeThread = new Thread(()->{
            try {
                while (true) {
                    checkGpsFileSize();
                    Thread.sleep(5000);
                }
            }catch (InterruptedException ex){
                Logger.getInstance().log(Logger.Logtype.DEBUG,"Check gps file thread was interruped");
            }
        });
        checkGpsSizeThread.start();
    }

    public synchronized void stopChecking(){
        if(checkGpsSizeThread != null){
            checkGpsSizeThread.interrupt();//interrupt needed because thread sleeps for 5 sec
        }
        checkGpsSizeThread = null;
    }

    @Override
    public void finalize(){
        stopChecking();
    }

    private void checkGpsFileSize(){
        long all = Tools.getFoldersize(gpsLogdir);
        if(all == -1){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Could not find or open Gps Log Folder: "+ gpsLogdir);
            return;//folder not found
        }
        long cur = 0;
        File f = new File(gpsLogdir+CURRENT_FILE_LOG);
        if(f.exists()) {
            all -= Tools.getFileSize(gpsLogdir + CURRENT_FILE_LOG);
            try {
                BufferedReader brTest = new BufferedReader(new FileReader(f));
                String currentLogFilePath = brTest.readLine();
                if(currentLogFilePath != null){
                    cur = Tools.getFileSize(gpsLogdir + currentLogFilePath);
                }
                brTest.close();
            }catch (IOException ex){
            }
        }

        if(all != lastAllFolderSize || cur != lastCurrentFileSize){
            callCloudListeners(all,cur);
            lastAllFolderSize = all;
            lastCurrentFileSize = cur;
        }
    }

    private void callCloudListeners(long all,long current){
        Platform.runLater(()-> {
            for (GpsSizeChangeListener it : sizeListener) {
                it.change(all, current);
            }
        });
    }

    public void registerListener(GpsSizeChangeListener lis){
        sizeListener.add(lis);
    }
    public void unregisterListener(GpsSizeChangeListener lis){
        sizeListener.remove(lis);
    }

    public long getLastAllFolderSize() {
        return lastAllFolderSize;
    }

    public long getLastCurrentFileSize() {
        return lastCurrentFileSize;
    }
}
