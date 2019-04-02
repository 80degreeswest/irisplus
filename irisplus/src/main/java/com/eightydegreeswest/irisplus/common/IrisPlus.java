package com.eightydegreeswest.irisplus.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * Created by Yuriy on 9/20/15.
 */
public class IrisPlus extends Application {

    private static Application mApplication;

    private static Activity mCurrentActivity;

    public static Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public static void setCurrentActivity(Activity currentActivity){
        mCurrentActivity = currentActivity;
    }

    public static Application getApplication() {
        return mApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }
}