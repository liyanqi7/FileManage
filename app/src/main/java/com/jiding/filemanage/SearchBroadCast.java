package com.jiding.filemanage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SearchBroadCast extends BroadcastReceiver {
    String TAG = "";
    public static String mServiceKeyword = "";//接收搜索关键字的静态变量
    public static String mServiceSearchPath = "";//接收搜索路径的静态变量

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();
        if (MainActivity.KEYWORD_BROADCAST.equals(mAction)){
            mServiceKeyword = intent.getStringExtra("keyword");
            mServiceSearchPath = intent.getStringExtra("searchPath");
            Log.d(TAG, "onReceive: ");
        }
    }
}
