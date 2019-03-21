package com.jpliot.slipactivitytest;

import android.app.Application;

import com.jpliot.slipview.SlipActivityManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        SlipActivityManager.getInstance()
                .registerActivityLifecycleMonitor(this);
    }
}
