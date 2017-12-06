package com.jiding.filemanage;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static java.net.Proxy.Type.HTTP;

public class WebActivity extends AppCompatActivity {

    private WebView webView;
    private RelativeLayout webLayout,loadingLayout;
    private ZoomControls zoomControls;
    private WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_web);
        webView = (WebView) findViewById(R.id.webkit);
        loadingLayout = (RelativeLayout) findViewById(R.id.lodingLayout);
        webLayout = (RelativeLayout) findViewById(R.id.lodingLayout);
        zoomControls = (ZoomControls) findViewById(R.id.zoomControls);
        webSettings = webView.getSettings();
        //设置可以使用js脚本
        webSettings.setJavaScriptEnabled(true);
        //执行异步进程
        new MyAsyncTask().execute("");
    }

    private void reading(){
        String filePath = getIntent().getStringExtra("filePath");
        if (filePath != null){
            //读取文件
            webView.loadData(readWebDataToStriingFromPath(filePath,new FileReadOverBack(){

                @Override
                public void fileReadOver() {

                }
            }),"text/html","utf-8");
        }else {
            new AlertDialog.Builder(WebActivity.this).setTitle("出错了")
                    .setMessage("获取文件路径出错!")
                    .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WebActivity.this.finish();
                        }
                    });
        }
    }

    //将网页数据读取到一个字符串变量中
    private String readWebDataToStriingFromPath(String path, final FileReadOverBack fileReadOverBack){
        File file = new File(path);
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int readCount = 0;
            while ((readCount = inputStream.read(bytes)) > 0){
                stringBuffer.append(new String(bytes,0,readCount));
            }
            fileReadOverBack.fileReadOver();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    interface FileReadOverBack{
        void fileReadOver();
    }

    private class MyAsyncTask extends AsyncTask<String, String, String>{
        //首先执行的函数
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingLayout.setVisibility(View.VISIBLE);
            webLayout.setVisibility(View.GONE);
        }

        //后台执行
        @Override
        protected String doInBackground(String... strings) {
            reading();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadingLayout.setVisibility(View.GONE);
            webLayout.setVisibility(View.VISIBLE);
            //放大按钮
            zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                //将网页内容放大
                @Override
                public void onClick(View v) {
                    webView.zoomIn();
                }
            });
            //缩小按钮
            zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.zoomOut();
                }
            });
        }
    }
}
