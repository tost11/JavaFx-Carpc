package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.model.PlaylistSong;
import de.tostsoft.mpdclient.modules.interfaces.PlaylistListener;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;


/**
 * Created by tost-holz on 14.09.2018.
 */
public class PlaylistScreen extends BasicDataScreen {


    private Button buttonPlay;
    private Button buttonRemove;
    private Button buttonSavePlaylist;

    private MpdClient mpdClient;

    private PlaylistListener m_PlaylitsListener = new PlaylistListener() {
        @Override
        public void changed() {
            updatePlaylistView();
        }
    };

    public PlaylistScreen(PlayerMain main, MpdClient mpd) {
        super(main);

        mpdClient = mpd;

        header.setText("Aktuelle Wiedergabeliste");

        Font font = new Font(20);

        int h = 60;
        int w = 200;

        VBox vBox = new VBox();

        buttonPlay = new Button("Abspielen");
        buttonPlay.setPrefWidth(w);
        buttonPlay.setPrefHeight(h);
        buttonPlay.setFont(font);
        buttonPlay.setOnAction((ev)->{
            int index = musikDataView.getSelectionModel().getSelectedIndex();
            if(index>=0){
                mpdClient.querry("play "+(index + scrollIndex));
            }
        });

        buttonRemove = new Button("Entfernen");
        buttonRemove.setFont(font);
        buttonRemove.setPrefWidth(w);
        buttonRemove.setPrefHeight(h);
        buttonRemove.setOnAction((ev)->{
            int index = musikDataView.getSelectionModel().getSelectedIndex();
            if(index>=0){
                mpdClient.querry("delete "+(scrollIndex +index));
            }
        });

        vBox.getChildren().addAll(buttonPlay, buttonRemove);

        vBox.getChildren().add(controlButtonBox);

        buttonSavePlaylist = new Button("Speichern");
        buttonSavePlaylist.setFont(font);
        buttonSavePlaylist.setPrefWidth(w);
        buttonSavePlaylist.setPrefHeight(h);
        buttonSavePlaylist.setOnAction((ev)->{
            String pattern = "dd-MM-yyyy:mm-HH";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            mpdClient.querry("save "+(simpleDateFormat.format(new Date())));
        });

        vBox.getChildren().add(buttonSavePlaylist);


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
    public void setActive() {
        super.setActive();

        mpdClient.getPlaylist().addListener(m_PlaylitsListener);

        updatePlaylistView();
    }

    void updatePlaylistView(){
        Collection<PlaylistSong> songs = mpdClient.getPlaylist().getPlaylist();

        allObjects.clear();
        for(PlaylistSong it:songs){
            allObjects.add(it.getName());
        }

        increaseIndex(0);//check if not some entmpy line on end because deleteation(0);
        updateListView();
    }

    @Override
    public void setInactive() {
        super.setInactive();

        mpdClient.getPlaylist().removeListener(m_PlaylitsListener);
    }
}
