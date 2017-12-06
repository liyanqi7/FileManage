package com.jiding.filemanage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SearchBroadCast extends BroadcastReceiver {
    public static String mServiceKeyword = "";//接收搜索关键字的静态变量
    public static String mServiceSearchPath = "";//接收搜索路径的静态变量

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();
        if (MainActivity.KEYWORD_BROADCAST.equals(mAction)){
            mServiceKeyword = intent.getStringExtra("keyword");
            mServiceSearchPath = intent.getStringExtra("searchPath");
        }
    }
}
