package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;


public class MainmenueScreen extends BasicScreen<GridPane>{
    public MainmenueScreen(PlayerMain main){
        super(main,new GridPane());
        Font font = new Font(40);

        Button b = new Button("Musikplayer");
        b.setOnAction((ev)->{
            this.main.setStageActive(PlayerScreen.class);
        });
        b.setPrefWidth(800);
        b.setPrefHeight(100);
        b.setFont(font);
        root.add(b,0,0);
        GridPane.setColumnSpan(b,4);

        b = new Button("Navigationssystem");
        b.setOnAction((ev)->{
            this.main.stopApp(2);
        });
        b.setPrefWidth(800);
        b.setPrefHeight(100);
        b.setFont(font);
        root.add(b,0,1);
        GridPane.setColumnSpan(b,4);

        b = new Button("Einstellungen");
        b.setPrefWidth(800);
        b.setPrefHeight(100);
        b.setFont(font);
        b.setOnAction((ev)->{
            this.main.setStageActive(SettingsScreen.class);
        });
        root.add(b,0,2);
        GridPane.setColumnSpan(b,4);

        b = new Button("Updates / Upload");
        b.setFont(font);
        b.setPrefWidth(800);
        b.setPrefHeight(100);
        b.setOnAction((ev)->{
            this.main.setStageActive(updateScreen.class);
        });
        root.add(b,0,3);
        GridPane.setColumnSpan(b,4);

        b = new Button("Beenden");
        b.setFont(new Font(30));
        b.setPrefHeight(80);
        b.setPrefWidth(400);
        b.setOnAction((ev)->{
            this.main.stopApp(0);
        });
        root.add(b,0,4);

        b = new Button("Logs");
        b.setFont(new Font(30));
        b.setPrefHeight(80);
        b.setPrefWidth(400);
        b.setOnAction((ev)->{
            this.main.setStageActive(LoggingScreen.class);
        });
        root.add(b,1,4);
    }
}
