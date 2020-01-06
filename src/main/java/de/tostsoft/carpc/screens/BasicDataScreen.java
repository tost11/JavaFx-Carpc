package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.mpdclient.Tools;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by tost-holz on 14.09.2018.
 */
public class BasicDataScreen extends BasicScreen<BorderPane> {

    protected ListView<String> musikDataView;
    protected ObservableList<String> shownObjects = FXCollections.observableArrayList();
    protected ArrayList<String> allObjects = new ArrayList<>();
    protected int scrollIndex = 0;
    private boolean firstShown = true;
    private boolean thradFinished = false;

    private int numInView = 11;

    protected Button buttonDown;
    protected Button buttonUp;
    protected Label counterLabel;
    protected HBox controlButtonBox;

    protected Label header;

    public BasicDataScreen(PlayerMain main) {
        super(main, new BorderPane());

        Integer dataViewSize = Tools.loadFromPropertiesInt("data_view_size");
        if(dataViewSize != null){
            numInView = dataViewSize;
        }

        musikDataView = new ListView<>();
        musikDataView.setPrefWidth(600);
        musikDataView.setPrefHeight(400);
        musikDataView.setItems(shownObjects);

        root.setLeft(musikDataView);
        musikDataView.setStyle("-fx-font-size: 22px;");


        header = new Label("Data Screen");
        header.setFont(new Font(24));
        header.setPrefHeight(50);
        root.setTop(header);

        controlButtonBox = new HBox();

        int h = 140;
        int w = 67;

        counterLabel = new Label("");
        counterLabel.setFont(new Font(18));
        counterLabel.setPrefHeight(h);
        counterLabel.setPrefWidth(w);
        counterLabel.setAlignment(Pos.CENTER);
        setCountLabelText();

        buttonUp = new Button("^\n|\n|");
        buttonUp.setPrefWidth(60);
        buttonUp.setFont(new Font(18));
        buttonUp.setPrefHeight(h);
        buttonUp.setPrefWidth(w);
        buttonUp.setOnAction((ev)->{
            decreaseIndex(numInView);
        });

        buttonDown = new Button("|\n|\nV");
        buttonDown.setPrefWidth(60);
        buttonDown.setFont(new Font(18));
        buttonDown.setPrefHeight(h);
        buttonDown.setPrefWidth(w);
        buttonDown.setOnAction((ev)->{
            increaseIndex(numInView);
        });
        controlButtonBox.getChildren().addAll(counterLabel, buttonDown, buttonUp);
    }

    private void setCountLabelText(){
        counterLabel.setText(""+ scrollIndex +"\n/\n"+ allObjects.size());
    }

    @Override
    public void setActive() {
        scrollIndex = 0;
        if(firstShown){//needet because java sucks
            shownObjects.add(0,"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
            Thread t = new Thread(){//totaly stupid but workds because Javafx sucks hard here
                @Override
                public void run() {
                    try {
                        while(musikDataView.lookupAll(".scroll-bar").size() < 2){
                            Thread.sleep(1);
                        }
                        Platform.runLater(()-> {
                            Set<Node> nods = musikDataView.lookupAll(".scroll-bar");
                            // ((ScrollBar) nods.toArray()[0]).setStyle("-fx-font-size: 60px;");
                            ((ScrollBar) nods.toArray()[1]).setStyle("-fx-opacity: 0; -fx-padding:-70;");
                            musikDataView.getItems().remove(0);
                            thradFinished =true;
                            updateListView();
                        });
                    }catch (InterruptedException ex){
                    }
                }
            };
            t.start();
            firstShown = false;
        }
    }

    protected void updateListView(){
        shownObjects.clear();
        if(!thradFinished){
            shownObjects.add(0,"MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
        }
        for(int i = scrollIndex; i< scrollIndex +numInView && i < allObjects.size(); i++){
            shownObjects.add(allObjects.get(i));
        }
        setCountLabelText();
    }

    protected void decreaseIndex(int num){
        scrollIndex -=num;
        if(scrollIndex < 0){
            scrollIndex = 0;
        }
        updateListView();
    };

    protected void increaseIndex(int num){
        scrollIndex +=num;
        int max = allObjects.size() - numInView;// -10 importetnt because 10 objects could fit in view
        if(scrollIndex > max){
            scrollIndex = max;
        }
        if(scrollIndex < 0){
            scrollIndex = 0;
        }
        updateListView();
    };
}
