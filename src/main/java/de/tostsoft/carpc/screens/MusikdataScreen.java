package de.tostsoft.carpc.screens;

import de.tostsoft.carpc.PlayerMain;
import de.tostsoft.mpdclient.MpdClient;
import de.tostsoft.mpdclient.modules.DatabaseModule;
import de.tostsoft.mpdclient.modules.PlayerModule;
import de.tostsoft.mpdclient.modules.interfaces.CustomCommandListener;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * Created by tost-holz on 11.09.2018.
 */
public class MusikdataScreen extends BasicDataScreen{

    /*public class MusikData{
        public String m_Name;
        public Integer m_Id;
        DatabaseModule.EN_MusikDataType m_Type;
    }*/

    private Button buttonAddToPlayqueue;
    private Button buttonAddToPlayqueueAndPlay;
    private Button buttonReplacePlayqueue;
    private Button buttonInspect;
    private Button buttonBack;

    private HashSet<String> isDirectorys = new HashSet<>();

    private int inspectId = -1;

    private MpdClient mpdClient;
    private LinkedList<ViewData> dataTypeStack = new LinkedList<>();

    private class ViewData{
        public final DatabaseModule.EN_MusikDataType Type;
        public final String Name;
        public int ScrollPosition;
        public final String CustomCommand;

        public ViewData(DatabaseModule.EN_MusikDataType type, String name) {
            CustomCommand = null;
            Type = type;
            Name = name;
            ScrollPosition = 0;
        }

        public ViewData(DatabaseModule.EN_MusikDataType type, String name,String customCommand) {
            CustomCommand = customCommand;
            Type = type;
            Name = name;
            ScrollPosition = 0;
        }

    }

    private CustomCommandListener commandListener = new CustomCommandListener() {
        @Override
        public void call(ArrayList<String> result, int id) {
            if(inspectId != -1 && inspectId == id){
                handleResultForInspect(result);
                inspectId = -1;
            }
        }
    };

    public MusikdataScreen(PlayerMain main, MpdClient mpd) {
        super(main);

        mpdClient = mpd;


        VBox vBox = new VBox();

        buttonAddToPlayqueue = new Button("Zur Wiedergabeliste\nhinzufügen");
        setButtonSize(buttonAddToPlayqueue);
        buttonAddToPlayqueue.setOnAction((ev)->{
            addToPlayQueue();
        });
        vBox.getChildren().add(buttonAddToPlayqueue);

        buttonAddToPlayqueueAndPlay = new Button("Zur Wiedergabeliste\nhinzufügen und abspielen");
        setButtonSize(buttonAddToPlayqueueAndPlay);
        buttonAddToPlayqueueAndPlay.setOnAction((ev)->{
            addToPlayQueueAndPlay();
        });
        vBox.getChildren().add(buttonAddToPlayqueueAndPlay);

        buttonReplacePlayqueue = new Button("Als Wiedergabeliste\n ersetzen");
        setButtonSize(buttonReplacePlayqueue);
        buttonReplacePlayqueue.setOnAction((ev)->{
            replacePlayQueue();
        });
        vBox.getChildren().add(buttonReplacePlayqueue);

        buttonInspect = new Button("Genauer Betrachten");
        setButtonSize(buttonInspect);
        buttonInspect.setOnAction((ev)->{
            inspect();
        });
        vBox.getChildren().add(buttonInspect);

        buttonBack = new Button("Zurück");
        setButtonSize(buttonBack);
        buttonBack.setOnAction((ev)->{
            popFromStack();
        });
        vBox.getChildren().add(buttonBack);

        vBox.getChildren().add(controlButtonBox);

        Button but = new Button("Zurück zum MpdClient");
        but.setOnAction((ev)->{
            this.main.setStageActive(PlayerScreen.class);
        });
        vBox.getChildren().add(but);

        root.setRight(vBox);
    }

    private void setButtonSize(Button but){
        but.setPrefWidth(210);
        but.setPrefHeight(60);
        but.textAlignmentProperty().set(TextAlignment.CENTER);
        but.setFont(new Font(16));
    }

    void setShownType(DatabaseModule.EN_MusikDataType type){
        dataTypeStack.clear();
        dataTypeStack.add(new ViewData(type,""));
    }

    private DatabaseModule.EN_MusikDataType getCurrentType(){
        return  dataTypeStack.getLast().Type;
    }

    private void updateTitle(){
        ViewData p = dataTypeStack.getLast();
        String text = "["+p.Type+"]: " + p.Name;
        header.setText(text);
    }


    private void fillListView(){
        shownObjects.clear();
        //musikDataView.getItems().clear();

        DatabaseModule.EN_MusikDataType type = getCurrentType();
        //musikDataView.getItems().addAll(mpdClient.getDatabase().getData(type));
        allObjects = new ArrayList<>(mpdClient.getDatabase().getData(type));
        /*allObjects.sort((l,r)->{
            return l.compareTo(r);
        });*/
        //Node node = musikDataView.lookup(".scroll-bar");
        //node.setStyle("-fx-font-size: 100px;");
        //System.out.println(node);
        updateListView();
    }


    @Override
    public void setActive() {
        super.setActive();
        mpdClient.getCustomCommand().addListener(commandListener);
        updateTitle();
        fillListView();
    }

    @Override
    public void setInactive() {
        super.setInactive();
        mpdClient.getCustomCommand().removeListener(commandListener);
        dataTypeStack.clear();
    }

    private void addToPlayQueue(){
        String sel = musikDataView.getSelectionModel().getSelectedItem();
        if(sel == null){
            return;
        }
        DatabaseModule.EN_MusikDataType type = getCurrentType();
        if(type == DatabaseModule.EN_MusikDataType.PLAYLIST){
            mpdClient.querry("load \""+sel+"\"");
        }else if(type == DatabaseModule.EN_MusikDataType.ARTIST){
            mpdClient.querry("findadd Artist \""+sel+"\"");
        }else if(type == DatabaseModule.EN_MusikDataType.ALBUM){
            mpdClient.querry("findadd Album \""+sel+"\"");
        }else if(type == DatabaseModule.EN_MusikDataType.FILE && mpdClient.isVersionAboveOrSame(0,21,0)){
            String file = makeFullFileName(musikDataView.getSelectionModel().getSelectedItem());
            if(isDirectory(sel)){//musst be not full path look in last view result
                mpdClient.querry("searchadd \"(base '" + MpdClient.excapeQuerryString(file,true) + "')\"");
            }else {
                mpdClient.querry("searchadd \"(file == '" + MpdClient.excapeQuerryString(file) + "')\"");
            }
        }
    }

    private void addToPlayQueueAndPlay(){

    }

    private void replacePlayQueue(){
        String sel = musikDataView.getSelectionModel().getSelectedItem();
        if(sel == null){
            return;
        }
        DatabaseModule.EN_MusikDataType type = getCurrentType();
        if(type == DatabaseModule.EN_MusikDataType.PLAYLIST){
            boolean playing = mpdClient.getPlayer().getStatus() == PlayerModule.PlayerStatus.PLAY;
            mpdClient.querry("clear");
            mpdClient.querry("load \""+sel+"\"");
            if(playing) {
                mpdClient.querry("play 0");
            }
        }if(type == DatabaseModule.EN_MusikDataType.ARTIST){
            boolean playing = mpdClient.getPlayer().getStatus() == PlayerModule.PlayerStatus.PLAY;
            mpdClient.querry("clear");
            mpdClient.querry("findadd Artist \""+sel+"\"");
            if(playing) {
                mpdClient.querry("play 0");
            }
        }if(type == DatabaseModule.EN_MusikDataType.ALBUM){
            boolean playing = mpdClient.getPlayer().getStatus() == PlayerModule.PlayerStatus.PLAY;
            mpdClient.querry("clear");
            mpdClient.querry("findadd Album \""+sel+"\"");
            if(playing) {
                mpdClient.querry("play 0");
            }
        }else if(type == DatabaseModule.EN_MusikDataType.FILE && mpdClient.isVersionAboveOrSame(0,21,0)){
            String file = makeFullFileName(musikDataView.getSelectionModel().getSelectedItem());
            boolean playing = mpdClient.getPlayer().getStatus() == PlayerModule.PlayerStatus.PLAY;
            mpdClient.querry("clear");
            if(isDirectory(sel)){
                mpdClient.querry("searchadd \"(base '"+ MpdClient.excapeQuerryString(file,true) + "')\"");
            }else {
                mpdClient.querry("searchadd \"(file == '" + MpdClient.excapeQuerryString(file) + "')\"");
            }
            if(playing) {
                mpdClient.querry("play 0");
            }
        }
    }

    private void inspect(){
        if(musikDataView.getSelectionModel().getSelectedItem() == null){
            return;
        }
        ViewData data = dataTypeStack.getLast();

        if(data.Type == DatabaseModule.EN_MusikDataType.ARTIST){
            String artist = musikDataView.getSelectionModel().getSelectedItem();
            inspectId = mpdClient.getCustomCommand().addCustomQuerry("list Title Artist \""+artist+"\"");
            data.ScrollPosition = scrollIndex;
            scrollIndex = 0;
            dataTypeStack.add(new ViewData(DatabaseModule.EN_MusikDataType.SONG,artist));
            shownObjects.clear();
            updateTitle();
        }else if(data.Type == DatabaseModule.EN_MusikDataType.ALBUM){
            String album = musikDataView.getSelectionModel().getSelectedItem();
            inspectId = mpdClient.getCustomCommand().addCustomQuerry("list Title Album \""+album+"\"");
            data.ScrollPosition = scrollIndex;
            scrollIndex = 0;
            dataTypeStack.add(new ViewData(DatabaseModule.EN_MusikDataType.SONG,album));
            shownObjects.clear();
            updateTitle();
        }else if(data.Type == DatabaseModule.EN_MusikDataType.FILE){
            if(isDirectory(musikDataView.getSelectionModel().getSelectedItem())){
                String file = makeFullFileName(musikDataView.getSelectionModel().getSelectedItem());
                String customCommand = "lsinfo \""+ MpdClient.excapeQuerryString(file)+"\"";
                inspectId = mpdClient.getCustomCommand().addCustomQuerry(customCommand);
                data.ScrollPosition = scrollIndex;
                scrollIndex = 0;
                dataTypeStack.add(new ViewData(DatabaseModule.EN_MusikDataType.FILE, file,customCommand));
                shownObjects.clear();
                updateTitle();
            }
        }
    }

    private boolean isDirectory(final String dir){
        ViewData data = dataTypeStack.getLast();
        if(data.Name.isEmpty()){
            return mpdClient.getDatabase().isDirectory(dir);
        }
        return isDirectorys.contains(dir);
    }

    private String makeFullFileName(final String file){
        ViewData data = dataTypeStack.getLast();
        if(data.Name.isEmpty()){
            return file;
        }
        return data.Name+"/"+file;
    }

    private void handleResultForInspect(ArrayList<String> res){
        ViewData data = dataTypeStack.getLast();
        if(data.Type == DatabaseModule.EN_MusikDataType.SONG){
            allObjects.clear();
            for(String it:res){
                String[] spl = it.split("Title: ");
                if(spl.length > 1) {
                    allObjects.add(spl[1]);
                }
            }
            scrollIndex = 0;
            updateListView();
        }else if(data.Type == DatabaseModule.EN_MusikDataType.FILE){
            allObjects.clear();
            List<String> dirs = new ArrayList<>();
            List<String> files = new ArrayList<>();
            isDirectorys.clear();
            for(String it:res){
                if(it.startsWith("directory:")) {
                    String name = it.split(":")[1].trim();
                    if(name.contains("/")){
                        name = name.substring(name.lastIndexOf('/')+1,name.length());
                    }
                    isDirectorys.add(name);
                    dirs.add(name);
                }else if(it.startsWith("file:")){
                    String name = it.split(":")[1].trim();
                    if(name.contains("/")){
                        name = name.substring(name.lastIndexOf('/')+1,name.length());
                    }
                    files.add(name);
                }
            }
            dirs.sort(String::compareTo);
            files.sort(String::compareTo);
            allObjects.addAll(dirs);
            allObjects.addAll(files);
            scrollIndex = 0;
            updateListView();
        }
    }

    private void popFromStack(){
        if(dataTypeStack.size() > 1){
            dataTypeStack.removeLast();
            inspectId = -1;//icnore if query is in progress
            ViewData data = dataTypeStack.getLast();
            scrollIndex = data.ScrollPosition;
            updateTitle();
            if(data.CustomCommand == null){
                fillListView();
                updateListView();
            }else{
                allObjects.clear();
                inspectId = mpdClient.getCustomCommand().addCustomQuerry(data.CustomCommand);
            }
            updateTitle();
        }else{
            main.setStageActive(PlayerScreen.class);
        }
    }


}
