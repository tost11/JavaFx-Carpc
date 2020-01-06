package de.tostsoft.carpc.stuff;

import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.tools.Logger;
import javafx.application.Platform;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class RadioChecker {

    public class RadioInfo{
        public String name;
        public String sId;
        public String currentPlayingSong;
        public String streamUrl;
        public String radioType;
    }

    private List<RadioListener> radioListeners = new ArrayList<>();

    public void addRadioListener(RadioListener radioListener){
        radioListeners.add(radioListener);
    }

    public void removeRadioListener(RadioListener radioListener){
        radioListeners.remove(radioListener);
    }

    private Map<String,RadioInfo> radioInfoMap = new HashMap<>();

    private String baseUrl = "http://localhost:7979";

    private Thread runThread = null;
    private boolean running = false;

    public synchronized void startChecking(){
        if(runThread != null){
            return;
        }
        String s = Tools.loadFromProperties("radio_url");
        if(s != null){
            baseUrl = s;
        }
        runThread = new Thread(()->run());
        running = true;
        runThread.start();
    }

    public Collection<RadioInfo> getRadioInfo(){
        return radioInfoMap.values();
    }

    public String getBaseurl(){
        return baseUrl;
    }

    private void clearRadioInfo(){
        List<String> radioInfos = radioInfoMap.keySet().stream().collect(Collectors.toList());
        radioInfoMap.clear();
        for (RadioListener radioListener : radioListeners) {
            for (String sid : radioInfos) {
                Platform.runLater(() -> radioListener.handleRemove(sid));
            }
        }
    }

    private void run(){
        while(running){
            try {
                URL url = new URL(baseUrl+"/mux.json");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                JSONParser parser = new JSONParser();
                Object jsonObj = parser.parse(in);
                JSONObject jsonObject = (JSONObject) jsonObj;
                JSONArray services = (JSONArray) jsonObject.get("services");

                List<RadioInfo> newOnes = new ArrayList<>();
                List<RadioInfo> updated = new ArrayList<>();
                Set<String> deleted =radioInfoMap.keySet().stream().collect(Collectors.toSet());

                for (Object obj : services) {
                    JSONObject service = (JSONObject) obj;
                    handleService(service,newOnes,updated);
                    deleted.remove(service.get("sid").toString());
                }

                for (String sId : deleted) {
                    Logger.getInstance().log(Logger.Logtype.DEBUG,"Removed new Radio Station: "+radioInfoMap.get(sId).name);
                    radioInfoMap.remove(sId);
                }

                callListeners(newOnes,updated,deleted);

            }catch (IOException | ParseException ex){
                Logger.getInstance().log(Logger.Logtype.ERROR,"Error by fetching Radio info: "+ex.getMessage());
                System.out.println(ex);
                clearRadioInfo();
            }
            try {
                Thread.sleep(1000);
            }catch (InterruptedException ex){

            }
        }
    }

    public RadioInfo findRadioInfo(String sId){
        return radioInfoMap.get(sId);
    }

    private void handleService(JSONObject service, List<RadioInfo> newOnes, List<RadioInfo> updated){
        String sId = service.get("sid").toString();
        RadioInfo radioInfo = radioInfoMap.get(sId);
        if(radioInfo == null){
            radioInfo = new RadioInfo();
            radioInfo.sId = sId;
            radioInfo.name = service.get("label").toString();
            radioInfo.currentPlayingSong = extractCurrentPlaying(service);
            radioInfo.streamUrl = baseUrl + service.get("url_mp3");
            radioInfo.radioType = service.get("ptystring").toString();
            radioInfoMap.put(sId,radioInfo);
            newOnes.add(radioInfo);
            Logger.getInstance().log(Logger.Logtype.DEBUG,"Added new Radio Station: "+radioInfo.name);
        }else {
            String name = service.get("label").toString();
            String song = extractCurrentPlaying(service);
            String streamUrl = baseUrl + service.get("url_mp3");
            String radioType = service.get("ptystring").toString();

            if (!radioInfo.name.equals(name) || !radioInfo.currentPlayingSong.equals(song) || !radioInfo.streamUrl.equals(streamUrl) || !radioInfo.radioType.equals(radioType) ) {
                radioInfo.name = name;
                radioInfo.currentPlayingSong = song;
                radioInfo.streamUrl = streamUrl;
                radioInfo.radioType = radioType;
                updated.add(radioInfo);
                Logger.getInstance().log(Logger.Logtype.DEBUG, "Update Radio Station: " + radioInfo.name);
            }
        }
    }

    private String extractCurrentPlaying(JSONObject service){
        if(!service.containsKey("dls")){
            return "";
        }
        JSONObject dls = (JSONObject)service.get("dls");
        return dls.get("label").toString();
    }

    private void callListeners(List<RadioInfo> newOnes,List<RadioInfo> updates,Set<String> deleted){
        for (RadioListener radioListener : radioListeners) {
            for (String sid : deleted) {
                Platform.runLater(()->radioListener.handleRemove(sid));
            }
            for (RadioInfo update : updates) {
                Platform.runLater(()->radioListener.handleUpdate(update));
            }
            for (RadioInfo newOne : updates) {
                Platform.runLater(()->radioListener.handleNewOne(newOne));
            }
        }
    }

    public synchronized void stop(){
        if(runThread == null){
            return;
        }
        running = false;
        try {
            runThread.join();
        }catch (InterruptedException ex){
            Logger.getInstance().log(Logger.Logtype.WARNING,"Error by Terminating Radio Thread: "+ex.getMessage());
        }
        runThread = null;
    }

}
