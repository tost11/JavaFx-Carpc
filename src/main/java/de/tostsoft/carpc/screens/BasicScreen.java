package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import javafx.scene.layout.Pane;

public abstract class BasicScreen<T extends Pane>{

    protected T root;
    protected PlayerMain main;

    BasicScreen(PlayerMain main, T root){
        this.main = main;
        this.root = root;
    }

    public void setActive(){}
    public void setInactive(){}

    public void update(){

    }

    public T getPane(){
        return root;
    }
}
