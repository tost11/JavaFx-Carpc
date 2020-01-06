package de.tostsoft.carpc;

import de.tostsoft.carpc.screens.*;
import de.tostsoft.carpc.stuff.*;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.modules.StatModule;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.*;

public class PlayerMain extends Application {
    static public Stage primaryStage;
    static public int width;
    static public int height;
    static public Lighting lighting = new Lighting();
    private MpdClient mpdClient;


    private HashMap<String,BasicScreen> screens = new HashMap<>();
    private BasicScreen activeScreen = null;
    private WLan wLan = new WLan();
    private UpdateLocker updateLocker = new UpdateLocker();
    private Owncloud cloud = null;
    private GpsUploader gpsUploader = null;
    private RadioChecker radioChecker = null;

    static private String saveGetIp(){
        String ip = Tools.loadFromProperties("ip");
        if(ip == null){
            ip = "localhost";
        }
        return ip;
    }

    static private void loadScreenSize(){
        try {
            String width = Tools.loadFromProperties("width");
            if (width != null) {
                PlayerMain.width = Integer.parseInt(width);
            }
        }catch (Exception ex){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Could parse Screen width");
        }

        try {
            String height = Tools.loadFromProperties("height");
            if (height != null) {
                PlayerMain.height = Integer.parseInt(height);
            }
        }catch (Exception ex){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Could parse Screen height");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        Logger.getInstance().init();

        Boolean isEnabled = Tools.loadFromPropertiesBoolean("enable_owncloud");
        if(isEnabled != null && isEnabled){
            cloud = new Owncloud(updateLocker);
        }
        isEnabled = Tools.loadFromPropertiesBoolean("enable_gps");
        if(isEnabled != null && isEnabled){
            gpsUploader = new GpsUploader(updateLocker);
        }
        isEnabled = Tools.loadFromPropertiesBoolean("enable_radio");
        if(isEnabled != null && isEnabled){
            radioChecker = new RadioChecker();
        }

        width = 800;
        height = 480;
        loadScreenSize();
        PlayerMain.primaryStage = primaryStage;

        mpdClient = new MpdClient(saveGetIp());

        mpdClient.addDisconnectListener(()->{
            if(activeScreen.getClass() == PlayerScreen.class) {
                setStageActive(MainmenueScreen.class);
            }
        });

        mpdClient.connect(true);

        //mpdClient.querry("albumart");

        Thread t = new Thread(()->{
            try {
                while (true) {
                    if(!mpdClient.isConnected() && !mpdClient.byConnect()){
                        Platform.runLater(()->{
                            mpdClient.connect(true);
                        });
                    }
                    Platform.runLater(() -> mpdClient.update());
                    Thread.sleep(1000);
                }
            }catch(InterruptedException ex){
                Logger.getInstance().log(Logger.Logtype.FATAL_ERROR,"Applictation termited because of: "+ex);
            }
        });
        t.start();

        if(radioChecker !=  null) {
            radioChecker.startChecking();
        }

        wLan.startUpdate();

        if(gpsUploader != null) {
            gpsUploader.startChecking();
        }

        PlayerMain.primaryStage.setScene(new Scene(new GridPane(), width, height));

        screens.put(PlayerScreen.class.toString(),new PlayerScreen(this, mpdClient,radioChecker));
        screens.put(MainmenueScreen.class.toString(),new MainmenueScreen(this));
        //screens.put(SettingsScreen.class.toString(),new SettingsScreen(this,mpdClient));
        screens.put(SettingsScreen.class.toString(),new SettingsScreen(this, mpdClient, wLan));
        screens.put(updateScreen.class.toString(),new updateScreen(this, cloud, mpdClient, gpsUploader));
        screens.put(MusikdataScreen.class.toString(),new MusikdataScreen(this, mpdClient));
        screens.put(PlaylistScreen.class.toString(),new PlaylistScreen(this, mpdClient));
        screens.put(LoggingScreen.class.toString(),new LoggingScreen(this));
        screens.put(RadioScreen.class.toString(),new RadioScreen(this,mpdClient,radioChecker));
        setStageActive(MainmenueScreen.class);

        PlayerMain.primaryStage.show();
    }

    public <T extends BasicScreen> void setStageActive(Class<T> c){
        if(c == PlayerScreen.class && !mpdClient.isConnected()){
            return;
        }
        if(activeScreen != null){
            activeScreen.setInactive();
        }
        activeScreen = screens.get(c.toString());
        primaryStage.getScene().setRoot(activeScreen.getPane());
        //primaryStage.setMaxWidth(600);
        activeScreen.setActive();
    }

    public <T extends BasicScreen> T getScreen(Class<T> c){
        BasicScreen it = screens.get(c.toString());
        return (it == null ? null : (T)it);
    }

    public void stopApp(int ret){
        if(cloud != null){
            cloud.stop();
        }
        wLan.stop();
        if(gpsUploader != null) {
            gpsUploader.stopChecking();
        }
        if(radioChecker != null) {
            radioChecker.stop();
        }
        Platform.exit();
        System.exit(ret);
    }

    public static void main(String[] args) {
        boolean onlyUpdate = false;
        for (String param : args) {
            if (param.equals("update")) {
                onlyUpdate = true;
            }
            if (param.equals("debug")) {
                Logger.getInstance().setLogLevel(Logger.Logtype.DEBUG,true);
            }
            if(param.startsWith("config=")){
                String [] arr = param.split("=",2);
                if(arr.length > 1){
                    Tools.configFileName = arr[1];
                }
            }
        }
        if(onlyUpdate){
            UpdateLocker updateLocker = new UpdateLocker();
            try {
                boolean chagnes = false;
                Logger.getInstance().log(Logger.Logtype.INFO,"Start checking for new Owncloud Data");
                Owncloud owncloud = new Owncloud(updateLocker);
                long size = owncloud.getSizeCloud();
                owncloud.startUpdate();
                while (owncloud.byUpdate()) {
                    Thread.sleep(1000);
                }
                size = owncloud.getSizeCloud() - size;
                if(size > 0){
                    Logger.getInstance().log(Logger.Logtype.INFO,"Owncloud update complete, change "+size+" Bytes bzw. "+ (int)(((double)(size)/1048576.f)+0.5f)+" MB");
                    chagnes = true;
                    checkMpdUpdate();
                }else{
                    Logger.getInstance().log(Logger.Logtype.INFO,"No Owncloud data has changed");
                }
                GpsUploader gpsUploader = new GpsUploader(updateLocker);
                Logger.getInstance().log(Logger.Logtype.INFO,"Start uploading new GPS Routes");
                int uploadet = gpsUploader.uploadFiles();//add while wenn threding is implemeted
                if(uploadet > 0){
                    chagnes = true;
                    Logger.getInstance().log(Logger.Logtype.INFO,"" + uploadet + " new Route" + (uploadet>1?"s":"")+ "have been Uploadet");
                }else{
                    Logger.getInstance().log(Logger.Logtype.INFO,"No new Routes have been Uploadet");
                }
                if(chagnes){
                    System.exit(1);
                }
            }catch (Exception ex){
                Logger.getInstance().log(Logger.Logtype.FATAL_ERROR,"Error by Updating on commandline: "+ex.getMessage());
                System.exit(-1);
            }
            System.exit(0);
        }
        launch(args);
    }

    static private void checkMpdUpdate() throws Exception{
        Logger.getInstance().log(Logger.Logtype.INFO,"Start updating MPD Database");
        MpdClient player = new MpdClient(saveGetIp());
        if(player.connect()){
            int[] numMpdData = new int[StatModule.StatStatus.values().length];
            while(player.getStats().getNumberSongs() == null){
                player.update();
                if(player.isConnected() == false){//todo also timeout
                    Logger.getInstance().log(Logger.Logtype.ERROR,"Lost connection while checking MPD Database");
                    return;
                }
            }
            for(StatModule.StatStatus status:StatModule.StatStatus.values()){
                numMpdData[status.ordinal()] = player.getStats().getStatus(status);
            }
            List<Boolean> waiting = new ArrayList<>();
            player.addListener(comm->{
                if(comm.getCommand().startsWith("stats")){
                    waiting.add(true);
                }
            });
            player.querry("update");
            while(waiting.isEmpty()){
                Thread.sleep(1000);
                player.update();
                if(player.isConnected() == false){//todo also timeout
                    Logger.getInstance().log(Logger.Logtype.ERROR,"Lost connection while checking MPD Database");
                    return;
                }
            }
            Logger.getInstance().log(Logger.Logtype.INFO,"Updated mpd Database");
            for(StatModule.StatStatus status:StatModule.StatStatus.values()){
                int num = player.getStats().getStatus(status) - numMpdData[status.ordinal()];
                Logger.getInstance().log(Logger.Logtype.INFO,"-> " + (num < 0 ? "removed ":" added ") + num + " " + status);
            }
            player.disconnect();
        }else{
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not connect to MPD");
        }
    }
}
