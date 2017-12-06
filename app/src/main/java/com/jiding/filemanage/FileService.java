package com.jiding.filemanage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.util.ArrayList;

public class FileService extends Service {
    private Looper mLooper;
    private FileHandler mFileHandler;
    private ArrayList<String> mFileName = null;
    private ArrayList<String> mFilePaths = null;
    public static final String FILE_SEARCH_COMPLETED = "com.supermario.file.FILE_NOTIFICATION";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //新建处理线程
        HandlerThread mHT = new HandlerThread("FileService",HandlerThread.NORM_PRIORITY);
        mHT.start();
        mLooper = mHT.getLooper();
        mFileHandler = new FileHandler(mLooper);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mFileName = new ArrayList<String>();
        mFilePaths = new ArrayList<String>();
        mFileHandler.sendEmptyMessage(0);
        fileSarachNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNF.cancel(R.string.app_name);
    }

    class FileHandler extends Handler{
        public FileHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //在指定范围搜索
            initFileArray(new File(SearchBroadCast.mServiceSearchPath));
            //当用户单击了取消搜索则不发送广播
            if (!MainActivity.isComBackFromNotification == true){
                Intent intent = new Intent(FILE_SEARCH_COMPLETED);
                intent.putStringArrayListExtra("mFileNameList",mFileName);
                intent.putStringArrayListExtra("mFilePathsList",mFilePaths);
                //搜索完毕之后携带数据并发送广播
                sendBroadcast(intent);
            }
        }
    }

    private int m = -1;
    private void initFileArray(File file) {
        //只能遍历可读文件夹
        if (file.canRead()){
            File[] mFileArray = file.listFiles();
            for (File currentArray:mFileArray){
                if (currentArray.getName().indexOf(SearchBroadCast.mServiceKeyword) != -1){
                    if (m == -1){
                        m++;
                        //返回搜索之前的目录
                        mFileName.add("BacktoSearchBefore");
                        mFilePaths.add(MainActivity.mCurrentFilePath);
                    }
                    mFileName.add(currentArray.getName());
                    mFilePaths.add(currentArray.getPath());
                }
                //如果是文件夹
                if (currentArray.exists()&&currentArray.isDirectory()){
                    //如果用户取消了搜索，应该停止搜索的过程
                    if (MainActivity.isComBackFromNotification == true){
                        return;
                    }
                    initFileArray(currentArray);
                }
            }
        }
    }

    NotificationManager mNF;
    public static String FILE_NOTIFICATION = "file_notification";
    /**
     * 通知栏
     */
    private void fileSarachNotification() {
//        Notification mNotification = new Notification.Builder(getBaseContext())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentText("后台搜索中....")
//                .setWhen(System.currentTimeMillis())
//                .build();
        Notification mNotification = new Notification(R.mipmap.ic_launcher,"后台搜索中....",System.currentTimeMillis());
        Intent intent = new Intent(FILE_NOTIFICATION);
        intent.putExtra("notification","当通知还存在，说明搜索未完成");
        PendingIntent mPI = PendingIntent.getBroadcast(this,0,intent,0);
//        mNotification.setLatestEventInfo();
        if (mNF == null){
            mNF = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        mNF.notify(R.string.app_name,mNotification);
    }

}
