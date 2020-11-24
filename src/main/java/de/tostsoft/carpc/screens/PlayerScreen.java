package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.carpc.stuff.RadioChecker;
import de.tostsoft.carpc.stuff.RadioListener;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.model.Cover;
import de.tostsoft.mpdclient.model.PlaylistSong;
import de.tostsoft.mpdclient.modules.*;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;


/**
 * Created by tost-holz on 13.07.2018.
 */
public class PlayerScreen extends BasicScreen<BorderPane> {
    private MpdClient mpdClient;
    private Button buttonPlay;
    private Button buttonStop;
    private Button nextButton;
    private Button prevButton;
    private Text songName;
    private Text songArtist;
    private Text songGenre;
    private Text songAlbum;
    private Text nextSong;
    private ProgressBar songProgress;
    private Text currentSongTime;
    private Text maxSongTime;
    private Button repeateButton;
    private Button randomButton;
    private Button singleButton;
    private ImageView coverImage;
    private Button radioButton;

    private Button buttonPlaylist;
    private Button[] datachangeButtons = new Button[DatabaseModule.EN_MusikDataType.values().length - 1];

    private VBox topInfoLabels;

    private HBox topBox = new HBox();
    private HBox playerBox = new HBox();
    private HBox optionBox = new HBox();
    private HBox lightingBox = new HBox();

    private final int timeWidth = 50;
    private Image defaulImage = null;
    private Image radioImage = null;
    private String lastImageUri = "";

    private int lightingStepValue = 10;

    private RadioChecker radioChecker;

    private RadioListener radioListener = new RadioListener() {
        @Override
        public void handleNewOne(RadioChecker.RadioInfo radioInfo) {
        }

        @Override
        public void handleUpdate(RadioChecker.RadioInfo radioInfo) {
            handleRadioUpdate(radioInfo);
        }

        @Override
        public void handleRemove(String sId) {
        }
    };

    public PlayerScreen(PlayerMain main, MpdClient mpd,RadioChecker radioChecker){
        super(main,new BorderPane());
        mpdClient = mpd;
        this.radioChecker = radioChecker;

        Integer value = Tools.loadFromPropertiesInt("lighting_step_value");
        if(value != null){
            lightingStepValue = value;
        }

        mpdClient.getPlayer().addListener((ev)->setStatus(ev));
        mpdClient.getPlayback().addListener((ev, obj)->{
            if(ev == PlaybackModule.PlaybackEvent.SONG_CHANGED){
                setSong((PlaylistSong)obj);
            }else if(ev == PlaybackModule.PlaybackEvent.SONGPOSITION_CHANGED){
                setSongPosition((float)obj);
            }else if(ev == PlaybackModule.PlaybackEvent.NEXTSONG_CHANGED){
                setNextSong((PlaylistSong)obj);
            }
        });

        mpdClient.getOptions().addListener((ev, stat)->{
            if(ev == OptionsModule.OptionStatus.RANDOM){
                setRandom(stat == 1);
            }
            if(ev == OptionsModule.OptionStatus.SINGLE){
                setSingle(stat == 1);
            }
            if(ev == OptionsModule.OptionStatus.REPEAT){
                setRepeat(stat == 1);
            }
        });

        mpdClient.getCover().addListener(cov->{
            PlaylistSong song = mpdClient.getPlayback().getCurrentSong();
            if(song != null){
                int index = song.filename.lastIndexOf("/");
                if(index > 0 && song.filename.substring(0,index+1).equals(cov.getUri())){
                    Image img = new Image(new File(cov.getFilename()).toURI().toString());
                    coverImage.setImage(img);
                    lastImageUri = cov.getUri();
                }
            }
        });

        String defaultImagefile = Tools.loadFromProperties("default_cover_image");
        defaulImage = new Image(new File(defaultImagefile == null ? "ressources/disc.jpg":defaultImagefile).toURI().toString());
        String defaultRadiofile = Tools.loadFromProperties("default_radio_image");
        radioImage = new Image(new File(defaultRadiofile == null ? "ressources/radio.png":defaultRadiofile).toURI().toString());



        int sizeCover = 200;
        mpdClient.getCover().setScaleSize(sizeCover,sizeCover);
        coverImage = new ImageView(defaulImage);
        coverImage.setFitWidth(sizeCover);
        coverImage.setFitHeight(sizeCover);
        topBox.getChildren().add(coverImage);
        //topBox.setMaxWidth(200);
        //root.setLeft(topBox);

        topInfoLabels = new VBox();
        songName = new Text("");
        songName.setFont(new Font(24));
        songArtist = new Text("");
        songArtist.setFont(new Font(20));
        songAlbum = new Text("");
        songAlbum.setFont(new Font(16));
        songGenre = new Text("");
        songGenre.setFont(new Font(16));
        nextSong = new Text("");
        nextSong.setFont(new Font(16));

        root.setMaxWidth(400);

        topInfoLabels.getChildren().addAll(songName, songArtist, songAlbum, songGenre, nextSong);
        topInfoLabels.setMinWidth(1000);

        //ScrollPane sp = new ScrollPane();
        //sp.setContent(topInfoLabels);
        //sp.setPrefSize(200,200);
        //sp.setFitToWidth(false);

        topBox.getChildren().add(topInfoLabels);
        //topBox.getChildren().add(sp);
        //topBox.setMaxWidth(200);

        //------------------next hbox----------------
        int fontsize = 16;
        HBox hBox = new HBox();
        //hBox.setAlignment(Pos.CENTER);
        currentSongTime = new Text("0:00");
        currentSongTime.setWrappingWidth(timeWidth);
        currentSongTime.setFont(new Font(fontsize));
        currentSongTime.setTextAlignment(TextAlignment.CENTER);
        songProgress = new ProgressBar();
        songProgress.setPrefWidth(PlayerMain.width - timeWidth *2);
        songProgress.setPrefHeight(25);
        songProgress.setOnMouseClicked((e)->{
            float pos = (float)(e.getX()/(PlayerMain.width - timeWidth *2));
            pos = mpdClient.getPlayback().getSongLength() * pos;
            mpdClient.getPlayback().setSongPosition((float)(pos+0.5));
        });
        maxSongTime = new Text("0:00");
        maxSongTime.setWrappingWidth(timeWidth);
        maxSongTime.setFont(new Font(fontsize));
        maxSongTime.setTextAlignment(TextAlignment.CENTER);

        hBox.getChildren().addAll(currentSongTime, songProgress, maxSongTime);
        root.setBottom(hBox);


        //------------------next hbox-----------------
        buttonPlay = new Button("Play");
        buttonPlay.setOnAction((e)->{
            if(mpdClient.getPlayer().getStatus() == PlayerModule.PlayerStatus.STOP ||
            mpdClient.getPlayer().getStatus() == PlayerModule.PlayerStatus.PAUSE){
                mpdClient.getPlayer().setStatus(PlayerModule.PlayerStatus.PLAY);
            }else{
                mpdClient.getPlayer().setStatus(PlayerModule.PlayerStatus.PAUSE);
            }
        });

        buttonStop = new Button("Stop");
        buttonStop.setOnAction((e)->{
            mpdClient.getPlayer().setStatus(PlayerModule.PlayerStatus.STOP);
        });

        prevButton = new Button("<-");
        prevButton.setOnAction((e)->{
            mpdClient.getPlayer().playPrevious();
        });

        nextButton = new Button("->");
        nextButton.setOnAction((e)->{
            mpdClient.getPlayer().playNext();
        });

        playerBox.getChildren().addAll(prevButton, buttonStop, buttonPlay, nextButton);

        repeateButton = new Button();
        repeateButton.setOnAction((e)->{
            mpdClient.getOptions().querryRepeat(!mpdClient.getOptions().isRepeat());
        });

        randomButton = new Button();
        randomButton.setOnAction((e)->{
            mpdClient.getOptions().querryRandom(!mpdClient.getOptions().isRandom());
        });

        singleButton = new Button();
        singleButton.setOnAction((e)->{
            mpdClient.getOptions().querrySingle(!mpdClient.getOptions().isSingle());
        });

        optionBox.getChildren().addAll(singleButton, randomButton, repeateButton);

        if(radioChecker != null) {
            radioButton = new Button("Radio");
            radioButton.setOnAction((e) -> {
                main.setStageActive(RadioScreen.class);
            });
            optionBox.getChildren().addAll(radioButton);
        }

        Font buttonFont = new Font(18);
        for(Node it: playerBox.getChildren()){
            Button b = (Button)it;
            b.setFont(buttonFont);
            b.setPrefWidth(150);
	    b.setPrefHeight(70);
        }

        for(Node it: optionBox.getChildren()){
            Button b = (Button)it;
            b.setFont(buttonFont);
            b.setPrefWidth(150);
	        b.setPrefHeight(70);
        }

        int size = 70;
        int lightFontSize = 30;
        Button b = new Button("-");
        b.setFont(new Font(lightFontSize));
        b.setOnAction(e->{
            PlayerMain.lighting.addLight(lightingStepValue*-1);
        });
        b.setPrefWidth(size);
        b.setPrefHeight(size);
        lightingBox.getChildren().add(b);
        b = new Button("+");
        b.setFont(new Font(lightFontSize));
        b.setPrefWidth(size);
        b.setPrefHeight(size);
        b.setOnAction(e->{
            PlayerMain.lighting.addLight(lightingStepValue);
        });
        lightingBox.getChildren().add(b);

        ScrollPane sp = new ScrollPane();
        sp.setContent(topBox);
        sp.setMaxWidth(600);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:transparent;");

        VBox vBox = new VBox();
        vBox.getChildren().addAll(sp, playerBox, optionBox, lightingBox);
        //vBox.getChildren().addAll(sp,playerBox,optionBox,lightingBox);
        vBox.setAlignment(Pos.TOP_LEFT);
        root.setLeft(vBox);

        vBox = new VBox();

        int h = 40;
        int w = 170;

        Font font = new Font(18);

        buttonPlaylist = new Button("Akt. Widg. Liste");
        buttonPlaylist.setPrefWidth(w);
        buttonPlaylist.setPrefHeight(h);
        buttonPlaylist.setFont(font);
        buttonPlaylist.setOnAction((ev)->{
            this.main.getScreen(MusikdataScreen.class).setShownType(DatabaseModule.EN_MusikDataType.ALBUM);
            this.main.setStageActive(PlaylistScreen.class);
        });

        for(DatabaseModule.EN_MusikDataType type: DatabaseModule.EN_MusikDataType.values()){
            if(type == DatabaseModule.EN_MusikDataType.SONG){
                continue;
            }
            Button but = new Button(""+(type == DatabaseModule.EN_MusikDataType.PLAYLIST ? "Widg. Listen":type));
            but.setPrefWidth(w);
            but.setPrefHeight(h);
            but.setFont(font);
            but.setOnAction((ev)->{
                this.main.getScreen(MusikdataScreen.class).setShownType(type);
                this.main.setStageActive(MusikdataScreen.class);
            });
            datachangeButtons[type.ordinal()] = but;
        }

        vBox.getChildren().add(buttonPlaylist);
        vBox.getChildren().addAll(datachangeButtons);
        vBox.getChildren().add(new Label(""));

        b = new Button("Alles nei :P");
        b.setFont(buttonFont);
        b.setPrefWidth(w);
        b.setPrefHeight(40);
        b.setOnAction((e)->{
            addEverythingToPlaylist();
        });
        vBox.getChildren().add(b);

        vBox.getChildren().add(new Label(""));

        b = new Button("Zurück");
        b.setFont(buttonFont);
        b.setPrefWidth(w);
        b.setPrefHeight(40);
        b.setOnAction((e)->{
            this.main.setStageActive(MainmenueScreen.class);
        });

        vBox.getChildren().add(b);
        vBox.setAlignment(Pos.CENTER_RIGHT);

        root.setRight(vBox);
    }

    private void addEverythingToPlaylist(){
        if(!mpdClient.isVersionAboveOrSame(0,21,0)){
            Logger.getInstance().log(Logger.Logtype.WARNING, "Adding of all songs not implemented for this mpd version");
            return;
        }
        mpdClient.querry("clear");
        mpdClient.querry("searchadd \"(file contains '')\"");
        mpdClient.querry("play");
    }

    @Override
    public void setActive() {
        super.setActive();
        mpdClient.setCallListenerByInit(false);
        setSongPosition(mpdClient.getPlayback().getSongPosition());
        setSong(mpdClient.getPlayback().getCurrentSong());
        setNextSong(mpdClient.getPlayback().getNextSong());

        setRandom(mpdClient.getOptions().isRandom());
        setSingle(mpdClient.getOptions().isSingle());
        setRepeat(mpdClient.getOptions().isRepeat());
        setStatus(mpdClient.getPlayer().getStatus());

        if(radioChecker != null) {
            radioChecker.addRadioListener(radioListener);
        }
    }

    @Override
    public void setInactive() {
        super.setInactive();
        if(radioChecker != null) {
            radioChecker.removeRadioListener(radioListener);
        }
    }

    void setRandom(boolean status){
        if(status){
            randomButton.setText("Random On");
        }else{
            randomButton.setText("Random Off");
        }
        nextSong.setVisible(!status);
    }

    void setSingle(boolean status){
        if(status){
            singleButton.setText("Single On");
        }else{
            singleButton.setText("Single Off");
        }
    }

    void setRepeat(boolean status){
        if(status){
            repeateButton.setText("Repeat On");
        }else{
            repeateButton.setText("Repeat Off");
        }
    }


    void setStatus(PlayerModule.PlayerStatus status){
        if(status == PlayerModule.PlayerStatus.PLAY){
            buttonPlay.setText("Pause");
        }else if(status == PlayerModule.PlayerStatus.STOP){
            setSongPosition(0);
            buttonPlay.setText("Play");
        }else if(status == PlayerModule.PlayerStatus.PAUSE){
            buttonPlay.setText("Play");
        }
    }

    private RadioChecker.RadioInfo checkIfRadioinfo(PlaylistSong song){
        if(radioChecker == null){
            return null;
        }
        if(song.filename != null && song.filename.startsWith(radioChecker.getBaseurl())){
            String[] splits = song.filename.split("/");
            String sId = splits[splits.length-1];
            return radioChecker.findRadioInfo(sId);
        }
        return null;
    }

    private String getTitleFromRadioTitle(String radioName){
        String [] split = radioName.split("-",2);
        if(split.length > 1){
            return split[1].trim();
        }
        return split[0].trim();
    }

    private String getArtistFromRadioTitle(String radioName){
        return radioName.split("-",2)[0].trim();
    }


    private void setSong(PlaylistSong song){
        songName.setText("Title: ");
        songArtist.setText("Interpret: ");
        songAlbum.setText("Album: ");
        songGenre.setText("Genre: ");
        maxSongTime.setText("0:00");
        if(song != null){
            RadioChecker.RadioInfo radioInfo = checkIfRadioinfo(song);
            topBox.getChildren().set(1, topInfoLabels);
            songName.setText("Title: " + (radioInfo == null ? song.getName() : getTitleFromRadioTitle(radioInfo.currentPlayingSong)));
            songArtist.setText("Interpret: "+ (radioInfo == null ? song.artist : getArtistFromRadioTitle(radioInfo.currentPlayingSong)));
            songAlbum.setText(radioInfo == null ? "Album: " + song.album : "Sender: " + radioInfo.name);
            songGenre.setText("Genre: " + (radioInfo == null ? song.genre : radioInfo.radioType));
            maxSongTime.setText(Tools.formatTime(mpdClient.getPlayback().getSongLength()));
            if(!lastImageUri.equals(song.filename)){
                if(radioInfo != null){
                    coverImage.setImage(radioImage);
                    return;
                }else {
                    int index = song.filename.lastIndexOf("/");
                    if (index > 0) {
                        Cover cover = mpdClient.getCover().getCover(song.filename.substring(0, index + 1));
                        if (cover != null) {
                            Image img = new Image(new File(cover.getFilename()).toURI().toString());
                            coverImage.setImage(img);
                            lastImageUri = cover.getUri();
                            return;
                        }
                    }
                }
            }
        }else{
            Label l = new Label("Kein Lied/Playlist ausgewählt");
            l.setFont(new Font(20));
            topBox.getChildren().set(1,l);
        }
        coverImage.setImage(defaulImage);
        lastImageUri ="";
    }

    private void setNextSong(PlaylistSong song){
        nextSong.setText("Nächster Song: ");
        if(song != null) {
            RadioChecker.RadioInfo radioInfo = checkIfRadioinfo(song);
            nextSong.setText(radioInfo == null ? "Nächster Song: " + song.getName() : "Nächster Sender: " + radioInfo.name);
        }
    }

    private void setSongPosition(float pos){
        currentSongTime.setText(Tools.formatTime((int)pos));
        songProgress.setProgress(pos / mpdClient.getPlayback().getSongLength());
    }

    private void handleRadioUpdate(RadioChecker.RadioInfo radioInfo){
        PlaylistSong song = mpdClient.getPlayback().getCurrentSong();
        if(song.filename.startsWith(radioChecker.getBaseurl()+"/mp3/"+radioInfo.sId)){
            songName.setText("Title: " +  getTitleFromRadioTitle(radioInfo.currentPlayingSong));
            songArtist.setText("Interpret: "+  getArtistFromRadioTitle(radioInfo.currentPlayingSong));
        }
    }
}
