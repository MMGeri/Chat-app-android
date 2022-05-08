package com.geri.chat.data;

import android.content.Context;
import android.util.Log;

public class console {
    //simpleton
    private static console instance;
    private console(){}
    public static console getInstance(){
        if(instance == null){
            instance = new console();
        }
        return instance;
    }

    public static void log(Context c, String msg){
        if(msg != null) {
            Log.i("KozosDebug", c.getClass().getName() +": "+ msg);
            return;
        }
        Log.i("KozosDebug", c.getClass().getName() +":null");
    }

    public void print(String message){
        System.out.println(message);
    }
}
