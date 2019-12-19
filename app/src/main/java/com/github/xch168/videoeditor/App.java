package com.github.xch168.videoeditor;

import android.app.Application;
import android.content.Context;

import com.kk.taurus.playerbase.config.PlayerLibrary;

public class App extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = this;

        //初始化库
        PlayerLibrary.init(this);
    }

    public static Context getContext() {
        return sContext;
    }
}
