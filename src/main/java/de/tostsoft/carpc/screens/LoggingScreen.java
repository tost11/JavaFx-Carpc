package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.mpdclient.tools.Logger;
import de.tostsoft.mpdclient.tools.Pair;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoggingScreen extends BasicScreen<BorderPane>{

    private ListView messageView;
    private ObservableList<String> shownMessages;
    private int maxLoggedMessages = 5000;
    private boolean[] showMessageTypes = new boolean[Logger.Logtype.values().length];
    private SimpleDateFormat fileDateFormater = new SimpleDateFormat("HH:mm");
    private List<Pair<Logger.Logtype,String>> allMessages = new ArrayList<>();

    public LoggingScreen(PlayerMain main) {
        super(main, new BorderPane());

        Logger.getInstance().addListener(this::addMessage);

        showMessageTypes[Logger.Logtype.ERROR.ordinal()] = true;
        showMessageTypes[Logger.Logtype.FATAL_ERROR.ordinal()] = true;
        showMessageTypes[Logger.Logtype.WARNING.ordinal()] = true;
        showMessageTypes[Logger.Logtype.INFO.ordinal()] = true;

        shownMessages = FXCollections.observableList(new ArrayList<>());

        messageView = new ListView();
        messageView.setItems(shownMessages);

        root.setTop(messageView);

        HBox vBox = new HBox();

        Button button = new Button("Zurück");
        button.setPrefSize(150,50);
        button.setFont(new Font(20));
        button.setOnAction((e)->{
            this.main.setStageActive(MainmenueScreen.class);
        });

        vBox.getChildren().add(button);

        root.setBottom(vBox);
    }

    private void addMessage(Logger.Logtype type,String message){
        allMessages.add(new Pair<Logger.Logtype, String>(type,message));
        if(allMessages.size() > maxLoggedMessages){
            allMessages.remove(0);
        }
        updateShowMessages();
    }

    void updateShowMessages(){
        Platform.runLater(()-> {
            shownMessages.clear();
            allMessages.forEach(pair -> {
                if (showMessageTypes[pair.getKey().ordinal()]) {
                    String s = fileDateFormater.format(new Date()) + " [" + pair.getKey() + "]: " + pair.getValue();
                    shownMessages.add(s);
                }
            });
        });
    }
}
