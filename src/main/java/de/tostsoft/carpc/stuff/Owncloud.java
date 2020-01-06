package de.tostsoft.carpc.stuff;

import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tost-holz on 02.09.2018.
 */
public class Owncloud {

    private String host = "https://owncloud.domain.xyz";
    private String owncloudDir = "/home/user/owncloud";

    private Process process = null;
    private BufferedReader reader = null;
    private BufferedReader errorReader = null;
    private Thread thread = null;

    private UpdateLocker updateLocker;

    private long cloudSize = 0;

    private ArrayList<OwncloudUpdateFinishedListener> listener = new ArrayList<>();
    private ArrayList<OwncloudSizeChangeListener> sizeListener = new ArrayList<>();

    public Owncloud(UpdateLocker updateLocker){
        this.host = Tools.loadFromProperties("owncloud_host");
        this.owncloudDir = Tools.loadFromProperties("owncloud_dir");

        if(this.host == null || this.owncloudDir == null){
            throw new RuntimeException("Config paramter(s) for Owncloud-Sync missing.");
        }

        this.updateLocker = updateLocker;
        updateCloudSize();
    }

    public void updateCloudSize(){
        long size = Tools.getFoldersize(owncloudDir);
        List<String> owncloudDbFiles = Tools.getFilesByExtention(owncloudDir, Arrays.asList("db","db-shm"));
        for(String file:owncloudDbFiles){
            size -= Tools.getFileSize(owncloudDir+"/"+file);
        }
        cloudSize = size;
    }

    public void startUpdate(){
        if(process != null){
            return;
        }
        updateLocker.setStatus(UpdateLocker.UpdateType.OWNCLOUD,true);
        //owncloudcmd -n ~/ownCloud https://owncloud.tost-soft.de
        try {
            //String toRun = "owncloudcmd -n ~/ownCloud "+host;
            //toRun = "su pi -c 'ls -la'";
            //toRun = "ls";

            //ProcessBuilder pb = new ProcessBuilder("bash",s+"/owncloud.sh");
            //process = Runtime.getRuntime().exec(new String[]{"sh","owncloud.sh",s});

            ProcessBuilder pb = new ProcessBuilder("owncloudcmd","-n","-s","--max-sync-retries","100",owncloudDir,host);

            pb.inheritIO();
            process = pb.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        }catch (IOException ex){
            if(process != null) {
                process.destroy();
            }
            reader = null;
            process = null;
            updateLocker.setStatus(UpdateLocker.UpdateType.OWNCLOUD,false);
            callListeners(false);
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not create Owncloud process: "+ex.getMessage());
            return;
        }

        thread = new Thread(()->{
            boolean succ = false;
            try {
                //process.waitFor();

                while(process.isAlive()){
                    long size = Tools.getFoldersize(owncloudDir);
                    if(size != cloudSize){
                        updateCloudSize();
                        callCloudListeners(size);
                    }
                    Thread.sleep(200);
                }

                String s = reader.readLine();
                while (s != null){
                    Logger.getInstance().log(Logger.Logtype.DEBUG,"Output of Owncloud command was: "+s);
                    s = reader.readLine();
                }
                succ=true;
            }catch (InterruptedException | IOException ex){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Owncloud Update interrupted: "+ex.getMessage());
            }
            Logger.getInstance().log(Logger.Logtype.INFO,"Musik update process finished");
            updateLocker.setStatus(UpdateLocker.UpdateType.OWNCLOUD,false);
            callListeners(succ);
            process.destroy();
            reader = null;
            process = null;
        });
        thread.start();
    }

    private void callListeners(boolean success){
        for(OwncloudUpdateFinishedListener it: listener){
            Platform.runLater(()->it.change(success));
        }
    }

    private void callCloudListeners(long size){
        for(OwncloudSizeChangeListener it: sizeListener){
            Platform.runLater(()->it.change(size));
        }
    }

    public void registerListener(OwncloudUpdateFinishedListener lis){
        listener.add(lis);
    }
    public void unregisterListener(OwncloudUpdateFinishedListener lis){
        listener.remove(lis);
    }
    public void registerListener(OwncloudSizeChangeListener lis){
        sizeListener.add(lis);
    }
    public void unregisterListener(OwncloudSizeChangeListener lis){
        sizeListener.remove(lis);
    }

    public void stop(){
        if(byUpdate()){
            thread.interrupt();
            try {
                thread.join();
            }catch (InterruptedException ex){
            }
        }
    }

    public boolean byUpdate(){
        return process != null;
    }

    public long getSizeCloud(){
        return cloudSize;
    }
}
