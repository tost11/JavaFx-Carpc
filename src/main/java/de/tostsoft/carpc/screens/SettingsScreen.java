package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.carpc.stuff.UpdateWLanStatusListener;
import de.tostsoft.carpc.stuff.WLan;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.modules.OptionsModule;
import de.tostsoft.mpdclient.modules.interfaces.OptionListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class SettingsScreen extends BasicScreen<BorderPane>{

    private ProgressBar progressBarVolume;
    private Text textVolume;
    private ProgressBar progressBarLight;
    private Text textLight;
    private Button buttonXFadeDown;
    private Button buttonXFadeUp;
    private Label textXFadeValue;
    private Label textXFade;
    private MpdClient mpdClient;
    private ComboBox<String> ipComboBox;
    private Label wlanStatus;
    private Button wLanChangeButton;
    private boolean byInit = false;
    private WLan wlan;

    private final int MAXXFADE = 10;

    private final OptionListener optionListener = new OptionListener(){
        @Override
        public void changed(OptionsModule.OptionStatus status, int value){
            if(status == OptionsModule.OptionStatus.VOLUME){
                handleVolume(value);
            }else if(status == OptionsModule.OptionStatus.XFADE){
                handleXFade(value);
            }
        }
    };

    private final UpdateWLanStatusListener wLanListener = new UpdateWLanStatusListener(){
        @Override
        public void handleNewStatus(WLan.ConnectionStatus connectionStatus) {
            wlanStatus.setText(""+connectionStatus);
            wLanChangeButton.setText(connectionStatus == WLan.ConnectionStatus.CONNECTED || connectionStatus == WLan.ConnectionStatus.NOT_CONNECTED ? "Deaktivieren":"Aktivieren");
        }
    };

    public SettingsScreen(PlayerMain main, MpdClient mpd, WLan wlan){
        super(main, new BorderPane());

        int w = 600;
        int h = 50;

        mpdClient = mpd;
        this.wlan = wlan;

        GridPane grid = new GridPane();

        int i=0;
        Font font = new Font(18);

        Text t = new Text("Einstellungen ");
        t.setFont(new Font(24));
        grid.add(t,0,i++);
        GridPane.setColumnSpan(t,2);

        StackPane p = new StackPane();

        progressBarVolume = new ProgressBar();
        progressBarVolume.setPrefWidth(w);
        progressBarVolume.setPrefHeight(h);
        progressBarVolume.setOnMouseClicked((e)->{
            float pos = (float)(e.getX() - progressBarVolume.getLayoutX());
            pos += 5;
            mpdClient.getOptions().querryVolume((int)(pos/600.f*100.f));
        });

        textVolume = new Text();
        textVolume.setFont(new Font(18));

        p.setAlignment(Pos.CENTER);
        p.getChildren().addAll(progressBarVolume, textVolume);

        t = new Text("MpdClient Lautstärke: ");
        t.setFont(font);
        grid.add(t,0,i);
        grid.add(p,1,i++);

        p = new StackPane();
        progressBarLight = new ProgressBar();
        progressBarLight.setPrefWidth(w);
        progressBarLight.setPrefHeight(h);
        progressBarLight.setOnMouseClicked((e)->{
            float pos = (float)(e.getX() - progressBarLight.getLayoutX());
            pos+=2;
            int val = (int)(pos/600.f*255.f);
            PlayerMain.lighting.setLight(val);
            updateLighting();
        });
        textLight = new Text();
        textLight.setFont(font);
        p.setAlignment(Pos.CENTER);
        p.getChildren().addAll(progressBarLight, textLight);

        t = new Text("Bildschirm Helligkeit: ");
        t.setFont(font);
        grid.add(t,0,i);
        grid.add(p,1,i++);

        ipComboBox = new ComboBox<>();
        ipComboBox.valueProperty().addListener((obs, old_v, new_v)->{
            if(!byInit) {
                mpdClient.setIp(new_v);
                mpdClient.disconnect();
            }
        });
        ipComboBox.setPrefWidth(w);
        ipComboBox.setPrefHeight(h);
        ipComboBox.setStyle("-fx-font: 18px \"Serif\";");
        t = new Text("Connection Ip: ");
        t.setFont(new Font(18));
        grid.add(t,0,i);
        grid.add(ipComboBox,1,i++);

        HBox hbox = new HBox();
        w = 70;
        buttonXFadeDown = new Button("<-");
        buttonXFadeDown.setPrefWidth(w);
        buttonXFadeDown.setPrefHeight(h);
        buttonXFadeDown.setOnAction(e->{
            if(textXFadeValue.getText().equals("...")){
                return;
            }
            int val = Integer.parseInt(textXFadeValue.getText());
            val --;
            if(val >= 0){
                mpdClient.getOptions().querryCrossfade(val);
            }
        });
        textXFadeValue = new Label("0");
        textXFadeValue.setPrefWidth(30);
        textXFadeValue.setPrefHeight(h);
        textXFadeValue.setFont(font);
        textXFadeValue.setAlignment(Pos.CENTER);
        buttonXFadeUp = new Button("->");
        buttonXFadeUp.setPrefWidth(w);
        buttonXFadeUp.setPrefHeight(h);
        buttonXFadeUp.setOnAction(e->{
            if(textXFadeValue.getText().equals("...")){
                return;
            }
            int val = Integer.parseInt(textXFadeValue.getText());
            val ++;
            if(val > MAXXFADE){
                val = MAXXFADE;
            }
            mpdClient.getOptions().querryCrossfade(val);
        });
        hbox.getChildren().addAll(buttonXFadeDown, textXFadeValue, buttonXFadeUp);
        textXFade = new Label("Crossfade");
        textXFade.setPrefHeight(h);
        textXFade.setFont(font);
        grid.add(textXFade,0,i);
        grid.add(hbox,1,i++);

        hbox = new HBox();
        Label l = new Label("WLan Status: ");
        l.setFont(font);
        grid.add(l,0,i);
        GridPane.setColumnSpan(l,1);
        wlanStatus = new Label("");
        wlanStatus.setFont(font);
        wlanStatus.setPrefWidth(200);
        wlanStatus.setPrefHeight(h);
        wlanStatus.setAlignment(Pos.CENTER_LEFT);
        wLanChangeButton = new Button("Ändern");
        wLanChangeButton.setPrefHeight(h);

        wLanChangeButton.setPrefWidth(200);
        wLanChangeButton.setOnAction((ev)->{
            if(this.wlan.getStatus() == WLan.ConnectionStatus.CONNECTED || this.wlan.getStatus() == WLan.ConnectionStatus.NOT_CONNECTED){
                this.wlan.disable();
            }else{
                this.wlan.enable();
            }
        });
        wLanChangeButton.setFont(font);
        hbox.getChildren().addAll(wlanStatus, wLanChangeButton);
        grid.add(hbox,1,i++);
        root.setTop(grid);

        Button b = new Button("Zurück");
        b.setFont(new Font(25));
        b.setPrefSize(150,50);
        b.setOnAction((e)->{
            this.main.setStageActive(MainmenueScreen.class);
        });

        root.setBottom(b);
    }

    @Override
    public void setActive(){
        byInit = true;
        mpdClient.setCallListenerByInit(true);
        super.setActive();
        mpdClient.getOptions().addListener(optionListener);
        if(mpdClient.isConnected()) {
            handleVolume(mpdClient.getOptions().getVolume());
            handleXFade(mpdClient.getOptions().getCrossfade());
        }else{
            handleVolume(null);
            handleXFade(null);
        }
        updateLighting();
        ipComboBox.getItems().clear();
        ipComboBox.getItems().addAll(Tools.loadFromPropertiesIncreasing("ip_"));
        ipComboBox.getSelectionModel().select(mpdClient.getIp());
        byInit =false;
        wlan.addListener(wLanListener);
        WLan.ConnectionStatus wlanStatus = wlan.getStatus();
        this.wlanStatus.setText(""+wlanStatus);
        wLanChangeButton.setText(wlanStatus == WLan.ConnectionStatus.CONNECTED || wlanStatus == WLan.ConnectionStatus.NOT_CONNECTED ? "Deaktivieren":"Aktivieren");
    }

    @Override
    public void setInactive(){
        super.setInactive();
        mpdClient.getOptions().removeListener(optionListener);
        wlan.removeListener(wLanListener);
    }

    private void handleVolume(Integer vol){
        if(vol != null) {
            progressBarVolume.setProgress((float) vol / 100.f);
            textVolume.setText("" + vol);
        }else{
            progressBarVolume.setProgress(-1);
        }
    }

    private void updateLighting(){
        int val = PlayerMain.lighting.checkLight();
        progressBarLight.setProgress(val/255.f);
        textLight.setText(""+val);
    }

    private void handleXFade(Integer value){
        if(value != null) {
            textXFadeValue.setText(""+value);
        }else{
            textXFadeValue.setText("...");
        }
    }
}
