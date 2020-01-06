package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.carpc.stuff.RadioChecker;
import de.tostsoft.carpc.stuff.RadioListener;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.stream.Collectors;

public class RadioScreen extends BasicDataScreen {
    private Button buttonPlay;
    private Button buttonAdd;

    private MpdClient mpdClient;

    private RadioChecker radioChecker;

    private RadioListener radioListener = new RadioListener() {
        @Override
        public void handleNewOne(RadioChecker.RadioInfo radioInfo) {
            addRadioInfo(radioInfo);
        }

        @Override
        public void handleUpdate(RadioChecker.RadioInfo radioInfo) {
        }

        @Override
        public void handleRemove(String sId) {
            removeRadioInfo(sId);
        }
    };

    private RadioChecker.RadioInfo getSelectedRadioInfo(){
        String sel = musikDataView.getSelectionModel().getSelectedItem();
        if(sel == null){
            return null;
        }
        String sid = sel.split("\\[")[1].split("\\]")[0];
        return radioChecker.findRadioInfo(sid);
    }

    public RadioScreen(PlayerMain main, MpdClient mpd,RadioChecker radioChecker) {
        super(main);

        mpdClient = mpd;
        this.radioChecker = radioChecker;

        header.setText("Gefundene Radio Sender");

        Font font = new Font(20);

        int h = 60;
        int w = 200;

        buttonPlay = new Button("Abspielen");
        buttonPlay.setPrefWidth(w);
        buttonPlay.setPrefHeight(h);
        buttonPlay.setFont(font);
        buttonPlay.setOnAction((ev)->{
            RadioChecker.RadioInfo radioInfo = getSelectedRadioInfo();
            if(radioInfo == null){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Could not find Radio station to play");
                return;
            }
            mpdClient.querry("clear");
            mpdClient.querry("add "+radioInfo.streamUrl);
            mpdClient.querry("play");
            main.setStageActive(PlayerScreen.class);
        });

        buttonAdd = new Button("Hinzufügen");
        buttonAdd.setFont(font);
        buttonAdd.setPrefWidth(w);
        buttonAdd.setPrefHeight(h);
        buttonAdd.setOnAction((ev)->{
            RadioChecker.RadioInfo radioInfo = getSelectedRadioInfo();
            if(radioInfo == null){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Could not find Radio station to play");
                return;
            }
            mpdClient.querry("add "+radioInfo.streamUrl);
            main.setStageActive(PlayerScreen.class);
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(buttonPlay, buttonAdd);
        vBox.getChildren().add(controlButtonBox);

        Button but = new Button("Zurück");
        but.setFont(new Font(25));
        but.setPrefSize(150,50);
        but.setOnAction((ev)->{
            this.main.setStageActive(PlayerScreen.class);
        });
        but.setPrefHeight(h);
        but.setFont(font);
        but.setPrefWidth(w);
        vBox.getChildren().addAll(new Label(""),but);

        root.setRight(vBox);
    }

    @Override
    public void setInactive() {
        super.setInactive();
        radioChecker.removeRadioListener(radioListener);

    }

    @Override
    public void setActive() {
        super.setActive();
        allObjects.clear();
        allObjects.addAll(radioChecker.getRadioInfo().stream().map(this::formatRadioInfo).collect(Collectors.toSet()));
        radioChecker.addRadioListener(radioListener);
    }

    private String formatRadioInfo(RadioChecker.RadioInfo radioInfo){
        return "["+radioInfo.sId+"] "+radioInfo.name;
    }

    public void removeRadioInfo(String sId){
        allObjects.removeIf(s->s.startsWith("["+sId+"]"));
        updateListView();
    }

    public void addRadioInfo(RadioChecker.RadioInfo radioInfo){
        allObjects.add(formatRadioInfo(radioInfo));
        updateListView();
    }
}
