package de.tostsoft.carpc.stuff;

import de.tostsoft.mpdclient.Tools;
import de.tostsoft.mpdclient.tools.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by tost-holz on 21.11.2018.
 */
public class Lighting {

    private String filename = "/sys/class/backlight/rpi_backlight/brightness";
    private int minLightValue = 15;

    public Lighting(){
        String lighting = Tools.loadFromProperties("lighting_file");
        if(lighting != null){
            filename = lighting;
        }
        Integer value = Tools.loadFromPropertiesInt("lighting_min_value");
        if(value != null && value >= 0){
            minLightValue = value;
        }
    }

    public int checkLight(){
        try{
            FileReader f = new FileReader(new File(filename));
            char buff[] = new char[10];
            int read = f.read(buff,0,10);
            f.close();
            String s = new String(buff,0,read);
            s = s.trim();
            Integer num = Integer.parseInt(s);
            return num;
            //m_ProgressBar_Light.setProgress(num/255.f);
            //m_Text_Light.setText(""+num);
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not load Lighting File: "+ex.getMessage());
        }
        return -1;
    }

    public void setLight(int val){
        try{
            PrintWriter f = new PrintWriter(filename);
            f.write(""+val);
            f.close();
        }catch(IOException ex){
            Logger.getInstance().log(Logger.Logtype.ERROR,"Could not load Lighting file: "+ex.getMessage());
        }
    }

    public void addLight(int value){
        int var=checkLight();
        var+=value;
        if(var >= 255){
            var = 255;
        }
        if(var < minLightValue){
            var = minLightValue;
        }
        setLight(var);
    }
}
