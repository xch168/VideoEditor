package com.m4399.videoeditor;

import android.app.Application;

import com.kk.taurus.playerbase.config.PlayerLibrary;

/**
 * Project Name: VideoEditor
 * File Name:    App.java
 * ClassName:    App
 *
 * Description: TODO.
 *
 * @author XuCanHui
 * @date 2018年09月26日 20:26
 *
 * Copyright (c) 2018年, 4399 Network CO.ltd. All Rights Reserved.
 */
public class App extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化库
        PlayerLibrary.init(this);
    }
}
