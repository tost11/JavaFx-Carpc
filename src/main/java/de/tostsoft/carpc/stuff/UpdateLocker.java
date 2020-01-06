package de.tostsoft.carpc.stuff;

import de.tostsoft.mpdclient.tools.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by tost-holz on 19.03.2019.
 */
public class UpdateLocker {
    public enum UpdateType{
        OWNCLOUD,
        MPD,
        NAVIT,
        GPS
    }

    private boolean updateTypes[] = new boolean[UpdateType.values().length];
    private boolean statusOfFile = false;
    private String lockFile = "/tmp/JavaFxMusikPlayerUpdate";
    //private String lockFile = "test_log_file";

    public UpdateLocker(){
        for(int i = 0; i< updateTypes.length; i++){
            updateTypes[i]=false;
        }
    }

    public synchronized void setStatus(UpdateType type,boolean status){
        updateTypes[type.ordinal()] = status;
        checkStatus();
    }

    private void checkStatus(){
        boolean stat = false;
        for(int i = 0; i< updateTypes.length; i++){
            stat = stat || updateTypes[i];
        }
        if(stat != statusOfFile){
            statusOfFile = stat;
            writeToFile(statusOfFile);
        }
    }

    protected void finalize(){
        writeToFile(false);
    }

    private void writeToFile(boolean status){
        try {
            File file = new File(lockFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);

            // Writes the content to the file
            writer.write(status?"1":"0");
            writer.flush();
            writer.close();
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Wrote status "+status+" to Lockfle");
        }catch (IOException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Failed to write status to Lockfile: "+ex.getMessage());
        }
    }
}
