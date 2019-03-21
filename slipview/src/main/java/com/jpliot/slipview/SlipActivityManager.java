package com.jpliot.slipview;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;

public class SlipActivityManager implements Application.ActivityLifecycleCallbacks {
    private static SlipActivityManager mSlipActivityManager;
    private Stack<Activity> mStackActivity = new Stack<>();

    public static SlipActivityManager getInstance() {
        if (mSlipActivityManager == null) {
            mSlipActivityManager = new SlipActivityManager();
        }

        return mSlipActivityManager;
    }


    public void registerActivityLifecycleMonitor(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mStackActivity.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mStackActivity.remove(activity);
    }


    public Activity getPrevActivity() {
        if (mStackActivity.size() >= 2) {
            return mStackActivity.get(mStackActivity.size()-2);
        } else {
            return null;
        }
    }

    public Activity getCurActivity() {
        if (mStackActivity.size() >= 1) {
            return mStackActivity.get(mStackActivity.size()-1);
        } else {
            return null;
        }
    }

    public int getActivityNum() {
        return mStackActivity.size();
    }
}
