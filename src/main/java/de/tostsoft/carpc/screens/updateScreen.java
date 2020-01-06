package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.carpc.stuff.*;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.modules.OptionsModule;
import de.tostsoft.mpdclient.modules.StatModule;
import de.tostsoft.mpdclient.modules.interfaces.OptionListener;
import de.tostsoft.mpdclient.modules.interfaces.StatListener;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

/**
 * Created by tost-holz on 02.09.2018.
 */
public class updateScreen extends BasicScreen<BorderPane> {

    private Owncloud cloud;
    private MpdClient mpdClient;
    private GpsUploader gpsUploader;
    private ProgressIndicator indicatorCloud;
    private Label textCloudSize;
    private ProgressIndicator indicatorNavi;
    private ProgressIndicator indicatorDb;
    private ProgressIndicator indicatorGps;
    private Label textSongNumber;
    private Label textGpsDatasize;
    private OwncloudUpdateFinishedListener musikUpdateListener = new OwncloudUpdateFinishedListener() {
        @Override
        public void change(boolean succes) {
            indicatorCloud.setProgress(1);
        }
    };

    private OptionListener dBListener = new OptionListener() {
        @Override
        public void changed(OptionsModule.OptionStatus status, int value) {
            if(status == OptionsModule.OptionStatus.UPDATE){
                setDBUpdateStatus(value);
            }
        }
    };

    private OwncloudSizeChangeListener sizeListener = new OwncloudSizeChangeListener() {
        @Override
        public void change(long size) {
            setCloudSize(size);
        }
    };

    private StatListener statListener = new StatListener() {
        @Override
        public void changed(StatModule.StatStatus status, int value) {
            if(status == StatModule.StatStatus.SONGS) {
                setSongNumber(value);
            }
        }
    };

    private GpsSizeChangeListener gpsSizeListener = new GpsSizeChangeListener(){
        @Override
        public void change(long all,long cur){
            setGpsSize(all,cur);
        }
    };

    public updateScreen(PlayerMain main, Owncloud cloud, MpdClient mpd, GpsUploader gpsUploader) {
        super(main, new BorderPane());
        mpdClient = mpd;
        this.cloud = cloud;
        this.gpsUploader = gpsUploader;

        Button but = new Button("Zurück");
        but.setFont(new Font(25));
        but.setPrefSize(150,50);
        but.setOnAction((ev)->{
            this.main.setStageActive(MainmenueScreen.class);
        });
        root.setBottom(but);

        GridPane grid = new GridPane();

        Label l = new Label("Update Übersicht");
        l.setFont(new Font(25));

        int but_h = 50;
        int but_w = 170;
        Font font = new Font(20);
        int lab_w = 250;

        int i = 0;
        grid.add(l,0,i);
        if(cloud!= null) {
            i++;
            l = new Label("Musik und Wiedergabeliste: ");
            l.setFont(font);
            grid.add(l, 0, i);
            indicatorCloud = new ProgressIndicator();
            indicatorCloud.setProgress(100);
            indicatorCloud.setPrefWidth(70);
            grid.add(indicatorCloud, 1, i);
            textCloudSize = new Label("");
            textCloudSize.setFont(font);
            textCloudSize.setPrefWidth(lab_w);
            grid.add(textCloudSize, 2, i);
            but = new Button("Update");
            but.setPrefWidth(but_w);
            but.setPrefHeight(but_h);
            but.setFont(font);
            but.setOnAction((e) -> {
                if (!this.cloud.byUpdate()) {
                    this.cloud.startUpdate();
                    indicatorCloud.setProgress(-1);
                }
            });
            grid.add(but, 3, i);
        }

        i++;
        l = new Label("Navigations Karten: ");
        l.setFont(font);
        grid.add(l,0,i);
        indicatorNavi = new ProgressIndicator();
        indicatorNavi.setProgress(100);
        indicatorNavi.setPrefWidth(70);
        grid.add(indicatorNavi,1,i);
        but = new Button("Update");
        but.setPrefWidth(but_w);
        but.setPrefHeight(but_h);
        but.setFont(font);
        but.setOnAction((e)->{
        });
        grid.add(but,3,i);

        i++;
        l = new Label("Musik Datenbank: ");
        l.setFont(font);
        grid.add(l,0,i);
        indicatorDb = new ProgressIndicator();
        indicatorDb.setProgress(100);
        indicatorDb.setPrefWidth(70);
        grid.add(indicatorDb,1,i);
        textSongNumber = new Label("");
        textSongNumber.setFont(font);
        textSongNumber.setPrefWidth(lab_w);
        grid.add(textSongNumber,2,i);
        but = new Button("Update");
        but.setPrefWidth(but_w);
        but.setPrefHeight(but_h);
        but.setFont(font);
        but.setOnAction((e)->{
            if(mpdClient.isConnected()){
                mpdClient.querry("update");
            }
        });
        grid.add(but,3,i);

        if(gpsUploader != null) {
            i++;
            l = new Label("Gesammelte GPS Daten: ");
            l.setFont(font);
            grid.add(l, 0, i);
            indicatorGps = new ProgressIndicator();
            indicatorGps.setProgress(100);
            indicatorGps.setPrefWidth(70);
            grid.add(indicatorGps, 1, i);
            textGpsDatasize = new Label("");
            textGpsDatasize.setFont(font);
            textGpsDatasize.setPrefWidth(lab_w);
            grid.add(textGpsDatasize, 2, i);
            but = new Button("Upload");
            but.setPrefWidth(but_w);
            but.setPrefHeight(but_h);
            but.setFont(font);
            but.setOnAction((e) -> {
                this.gpsUploader.uploadFiles();
            });
            grid.add(but, 3, i);
        }

        i++;
        l = new Label("Firmware: ");
        l.setFont(font);
        grid.add(l,0,i);
        but = new Button("Update");
        but.setPrefWidth(but_w);
        but.setPrefHeight(but_h);
        but.setFont(font);
        but.setOnAction((e)->{
            this.main.stopApp(3);
        });
        grid.add(but,3,i);

        root.setTop(grid);
    }

    @Override
    public void setActive(){
        mpdClient.setCallListenerByInit(true);
        if(cloud != null){
            cloud.registerListener(musikUpdateListener);
            if(cloud.byUpdate()){
                indicatorCloud.setProgress(-1);
            }else{
                indicatorCloud.setProgress(1);
            }
            setCloudSize(cloud.getSizeCloud());
            cloud.registerListener(sizeListener);
        }
        mpdClient.getOptions().addListener(dBListener);
        mpdClient.getStats().addListener(statListener);
        if(mpdClient.isConnected()) {
            setDBUpdateStatus(mpdClient.getOptions().getStatus(OptionsModule.OptionStatus.UPDATE));
            setSongNumber(mpdClient.getStats().getNumberSongs());
        }

        if(gpsUploader != null) {
            gpsUploader.registerListener(gpsSizeListener);
            setGpsSize(gpsUploader.getLastAllFolderSize(), gpsUploader.getLastCurrentFileSize());
        }
    }

    private void setCloudSize(long size){
        textCloudSize.setText(""+ String.format("%.02f",(float)((double)(size)/1073741824.f))+" GB");
    }

    private void setDBUpdateStatus(int status){
        indicatorDb.setProgress(status < 0 ? -1 : 1);
    }

    private void setSongNumber(int number){
        textSongNumber.setText(""+number+" Songs");
    }

    private void setGpsSize(long all,long cur){
        String gp_all = "" + (int)(((double)(all)/1048576.f)+0.5f);
        String gp_cur = "" +(int)(((double)(cur)/1048576.f)+0.5f);
        textGpsDatasize.setText("Akt: "+ gp_cur+"MB, All: "+gp_all+"MB");
    }

    @Override
    public void setInactive() {
        if(cloud != null) {
            cloud.unregisterListener(musikUpdateListener);
            cloud.unregisterListener(sizeListener);
        }
        mpdClient.getOptions().removeListener(dBListener);
        if(gpsUploader != null) {
            gpsUploader.unregisterListener(gpsSizeListener);
        }
    }
}
