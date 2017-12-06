package com.jiding.filemanage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by lyq on 2017/11/30.
 */

class FileAdapter extends BaseAdapter {
    private Bitmap mBackRoot;
    private Bitmap mBackUp;
    private Bitmap mImage;
    private Bitmap mAudio;
    private Bitmap mRar;
    private Bitmap mVideo;
    private Bitmap mFolder;
    private Bitmap mApk;
    private Bitmap mOthers;
    private Bitmap mTxt;
    private Bitmap mWeb;
    private Context mContext;
    //文件名列表
    private  List<String> mFileNameList;
    private List<String> mFilePathList;

    public FileAdapter(Context context, List<String> filename, List<String> filePath) {
        mContext = context;
        mFileNameList = filename;
        mFilePathList = filePath;
        //初始化图片资源
        mBackRoot = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.root);
        mBackUp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.back);
        mImage = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.image);
        mAudio = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.audio);
        mVideo = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.audio);
        mApk = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.apk);
        mTxt = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.txt);
        mOthers = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.others);
        mFolder = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.folder);
        mRar = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.zip);
        mWeb = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.web);
    }

    @Override
    public int getCount() {
        return mFilePathList.size();
    }

    //获得当前位置对应的文件名
    @Override
    public Object getItem(int position) {
        return mFileNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_child,null);
//            LayoutInflater mLI = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = mLI.inflate(R.layout.list_child,null);
            //获取列表布局界面元素
            viewHolder.mIV = (ImageView) convertView.findViewById(R.id.item_image);
            viewHolder.mTV = (TextView) convertView.findViewById(R.id.item_text);
            convertView.setTag(viewHolder);
        }else {
            //获取视图标签
            viewHolder = (ViewHolder) convertView.getTag();
        }
        File mFile = new File(mFileNameList.get(position).toString());
        if (mFileNameList.get(position).toString().equals("BacktoRoot")){
            viewHolder.mIV.setImageBitmap(mBackRoot);
            viewHolder.mTV.setText("返回根目录");
        }else if (mFileNameList.get(position).toString().equals("BacktoUp")){
            viewHolder.mIV.setImageBitmap(mBackUp);
            viewHolder.mTV.setText("返回上一级");
        }else if (mFileNameList.get(position).toString().equals("BacktoSearchBefore")){
            viewHolder.mIV.setImageBitmap(mBackRoot);
            viewHolder.mTV.setText("返回搜索之前目录");
        }else {
            String fileName = mFile.getName();
            viewHolder.mTV.setText(fileName);
            if (mFile.isDirectory()){
                viewHolder.mIV.setImageBitmap(mFolder);
            }else {
                String fileEnds = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()).toLowerCase();
                if (fileEnds.equals("m4a")|| fileEnds.equals("mp3")||fileEnds.equals("mid")||fileEnds.equals("xmf")
                        ||fileEnds.equals("ogg")||fileEnds.equals("wav")){
                    viewHolder.mIV.setImageBitmap(mVideo);
                }else if (fileEnds.equals("3gp")||fileEnds.equals("mp4")){
                    viewHolder.mIV.setImageBitmap(mAudio);
                }else if (fileEnds.equals("jpg")||fileEnds.equals("gif")||fileEnds.equals("png")||fileEnds.equals("jpeg")
                        ||fileEnds.equals("bmp")){
                    viewHolder.mIV.setImageBitmap(mImage);
                }else if (fileEnds.equals("apk")){
                    viewHolder.mIV.setImageBitmap(mApk);
                }else if (fileEnds.equals("txt")){
                    viewHolder.mIV.setImageBitmap(mTxt);
                }else if (fileEnds.equals("zip")||fileEnds.equals("rar")){
                    viewHolder.mIV.setImageBitmap(mRar);
                }else if (fileEnds.equals("html")||fileEnds.equals("htm")){
                    viewHolder.mIV.setImageBitmap(mWeb);
                }else {
                    viewHolder.mIV.setImageBitmap(mOthers);
                }
            }
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView mIV;
        TextView mTV;
    }
}
