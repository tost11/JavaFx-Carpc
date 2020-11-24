package de.tostsoft.carpc.stuff;

import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.application.Platform;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tost-holz on 17.03.2019.
 */
public class WLan {
    public enum ConnectionStatus{
        CONNECTED,
        NOT_CONNECTED,
        DISABLD,
        UNKNOWN;


        @Override
        public String toString() {
            switch(this){
                case CONNECTED:
                    return "Verbunden";
                case NOT_CONNECTED:
                    return "Nicht Verbunden";
                case DISABLD:
                    return "Ausgeschaltet";
            }
            return "Unbekannt";
        }
    }

    private ConnectionStatus status = ConnectionStatus.UNKNOWN;
    private Thread thread = null;
    private boolean run = false;

    private ArrayList<UpdateWLanStatusListener> listeners = new ArrayList<>();

    private String statusFile ="/sys/class/net/wlan0/carrier";
    private String wLanInterfaceName ="wlan0";

    public void init(){
        checkStatus();
        Boolean enabled = Tools.loadFromPropertiesBoolean("wlan_status");
        if(enabled != null){
            Logger.getInstance().log(Logger.Logtype.INFO,"Set wlan on start: "+(enabled?"Enabled":"Disabled"));
            if(enabled){
                enable();
            }else{
                disable();
            }
        }
    }

    public WLan(){
        String value = Tools.loadFromProperties("wlan_file");
        if(value != null){
            statusFile = value;
        }

        value = Tools.loadFromProperties("wlan_interface");
        if(value != null){
            wLanInterfaceName = value;
        }
    }

    public void startUpdate(){
        if(thread != null){
            return;
        }
        run = true;
        thread = new Thread(()->{
            int needetRuns = 10;
            int runs = 0;
            while(run){
                try {
                    Thread.sleep(100);
                }catch(InterruptedException ex){
                }
                runs++;
                if(needetRuns == runs){
                    checkStatus();
                    runs = 0;
                }
            }
        });
        thread.start();
    }

    private Character checkFile(){
        try {
            FileReader stream = new FileReader(new File(statusFile));
            char c[] = new char[1];
            c[0] = '0';
            stream.read(c, 0, 1);
            stream.close();
            return c[0];
        }catch (IOException ex){
            return null;
        }
    }

    public void checkStatus(){
        Character c = checkFile();
        if(c == null){
            updateStatus(ConnectionStatus.DISABLD);
        }else if(c=='1'){
            updateStatus(ConnectionStatus.CONNECTED);
            //TODO check connect wlan name
        }else if(c == '0'){
            updateStatus(ConnectionStatus.NOT_CONNECTED);
        }else{
            updateStatus(ConnectionStatus.UNKNOWN);
        }
    }

    private void updateStatus(ConnectionStatus status){
        if(this.status == status){
            return;
        }
        this.status = status;
        for(UpdateWLanStatusListener listener: listeners){
            Platform.runLater(()->listener.handleNewStatus(status));
        }
    }


    public void stop(){
        if(thread == null){
            return;
        }
        run = false;
        try {
            thread.join();
        }catch (InterruptedException ex){
        }
        thread = null;
    }

    public void addListener(UpdateWLanStatusListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void removeListener(UpdateWLanStatusListener listener){
        listeners.remove(listener);
    }

    public ConnectionStatus getStatus(){
        return status;
    }

    public void enable(){
        if(status == ConnectionStatus.DISABLD){
            ProcessBuilder pb = new ProcessBuilder("ifconfig", wLanInterfaceName,"up");
            pb.inheritIO();
            try {
                Process process = pb.start();
                process.waitFor();
                Logger.getInstance().log(Logger.Logtype.INFO,"WLan enabled");
            }catch(IOException | InterruptedException ex){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Failed to enable WLan: "+ex.getMessage());
            }
        }
    }

    public void disable(){
        if(status == ConnectionStatus.CONNECTED || status == ConnectionStatus.NOT_CONNECTED){
            ProcessBuilder pb = new ProcessBuilder("ifconfig", wLanInterfaceName,"down");
            pb.inheritIO();
            try {
                Process process = pb.start();
                process.waitFor();
                Logger.getInstance().log(Logger.Logtype.INFO,"WLan disabled");
            }catch(IOException | InterruptedException ex){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Failed to disable WLan: "+ex.getMessage());
            }
        }
    }
}
