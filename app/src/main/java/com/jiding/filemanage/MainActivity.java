package com.jiding.filemanage;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

    public static boolean isComBackFromNotification = false;
    private List<String> mFilename = null;
    private List<String> mFilePaths = null;
    private String mRootPath = File.separator;//起始目录"/"
    private String mSDCard = Environment.getExternalStorageDirectory().toString();//SD卡根目录
    private String mOldFilePath = "";
    private String mNewFilePath = "";
    private String keyWords;
    //用于显示当前路径
    private TextView mPath;
    //用于放置工具栏
    private GridView mGridViewToolbar;
    private int[] gridview_menu_image = {R.drawable.mobile,R.drawable.sd_card,R.drawable.search,
            R.drawable.create,R.drawable.paste,R.drawable.exit};
    private String[] gridview_menu_title = {"手机","SD卡","搜索","创建","粘贴","退出"};
    private static int menuPosition = 1;//代表手机或SD卡，1.手机 2.代表SD卡

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        initGridViewMenu();//初始化菜单视图
        initMenuListener();//为列表绑定长按监听器
        getListView().setOnItemLongClickListener(this);
        mPath = (TextView) findViewById(R.id.mPath);
        //程序一开始的时候加载手机目录下的文件
        initFileListInfo(mRootPath);
    }

    private void initGridViewMenu() {
        mGridViewToolbar = (GridView) findViewById(R.id.file_gridview_toolbar);
        mGridViewToolbar.setSelector(R.drawable.pre_background);
        mGridViewToolbar.setBackgroundResource(R.drawable.back_background);
        mGridViewToolbar.setNumColumns(6);
        mGridViewToolbar.setGravity(Gravity.CENTER);
        mGridViewToolbar.setVerticalSpacing(5);
        mGridViewToolbar.setHorizontalSpacing(10);
        //设置适配器
        mGridViewToolbar.setAdapter(getMenuAdapter(gridview_menu_title,gridview_menu_image));
    }

    /**
     *菜单适配器
     */
    private SimpleAdapter getMenuAdapter(String[] menuNameArray, int[] imageResourceArray) {
        //数组列表用于存放映射表
        ArrayList<HashMap<String,Object>> mData = new ArrayList<HashMap<String,Object>>();
        for (int i = 0; i < menuNameArray.length; i++){
            HashMap<String,Object> mMap = new HashMap<String,Object>();
            //将image映射成图片资源
            mMap.put("image",imageResourceArray[i]);
            //将title映射成标题
            mMap.put("title",menuNameArray[i]);
            mData.add(mMap);
        }
        //新建简单适配器，设置适配器的布局文件和映射关系
        SimpleAdapter mAdapter = new SimpleAdapter(this,mData,R.layout.item_menu,
                new String[]{"image","title"},new int[]{R.id.item_image,R.id.item_text});
        return mAdapter;
    }

    /**
     * 菜单项的监听
     */
    private void initMenuListener() {
        mGridViewToolbar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        menuPosition = 1;
                        initFileListInfo(mRootPath);
                    break;
                    case 1:
                        menuPosition = 2;
                        initFileListInfo(mSDCard);//回到SD卡根目录
                        break;
                    case 2:
                        searchDialog();//显示搜索对话框
                        break;
                    case 3:
                        createFolder();//创建文件夹
                        break;
                    case 4:
                        palseFie();//粘贴文件
                        break;
                    case 5:
                        MainActivity.this.finish();//退出
                        break;
                }
            }
        });
    }

    //长按列表项的事件监听
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (isAddBackUp == true){ //说明存在在返回根目录和返回上一级两列，接下来要对这两列进行屏蔽
            if (position != 0 && position !=1)
            {
                initItemLongClickListener(new File(mFilePaths.get(position)));
            }
        }
        if (mCurrentFilePath.equals(mRootPath)||mCurrentFilePath.equals(mSDCard)){
            initItemLongClickListener(new File(mFilePaths.get(position)));
        }
        return false;
    }

    private String mCopyFileName;
    private boolean isCopy = false;
    /**
     *长按文件或文件夹时弹出的带ListView效果的功能菜单
     */
    private void initItemLongClickListener(final File file){
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
            //item的值就是从0开始的索引值(从列表第一项开始)
            public void onClick(DialogInterface dialog, int item){
               if (file.canRead()){//注意,所有对对文件的操作必须是在该文件可读的情况下才可以,否则报错
                   if (item == 0){
                       //substring(beginIndex,endIndex)是java中截取字符串的一个方法
                       if (file.isFile()&&"txt".equals((file.getName().substring(file.getName().lastIndexOf(".")+1,file.getName().length())).toLowerCase())){
                           Toast.makeText(MainActivity.this, "以复制", Toast.LENGTH_SHORT).show();
                           isCopy = true;//复制标志位，表明已经复制文件
                           mCopyFileName = file.getName();//取得复制文件的名字
                           mOldFilePath = mCurrentFilePath + File.separator + mCopyFileName;
                       }else {
                           Toast.makeText(MainActivity.this, "对不起，目前只支持复制文本文件", Toast.LENGTH_SHORT).show();
                       }
                   } else if (item == 1){
                       initRenameDialog(file);//重命名
                   } else if (item == 2){
                       initDeleteDialog(file);//删除
                   }
               }else {
                   Toast.makeText(MainActivity.this, "对不起，您的访问权限不足!", Toast.LENGTH_SHORT).show();
               }
            }
        };
        //列表项名称
        String[] mMenu = {"复制","重命名","删除"};
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("请选择操作!")
                .setItems(mMenu,listener)
                .setPositiveButton("取消",null).show();
    }

    //用静态变量存储当前目录路径信息
    public static String mCurrentFilePath = "";
    private void initFileListInfo(String filePath) {
//        Log.d("1", "onCreate: " + mRootPath);
        isAddBackUp = false;
        mCurrentFilePath = filePath;
        //显示当前的路径
        mPath.setText(filePath);
        mFilename = new ArrayList<String>();
        mFilePaths = new ArrayList<String>();
        File mFile = new File(filePath);
        //遍历出该文佳佳路径下的所有文件/文件夹
        File[] mFiles = mFile.listFiles();
//        Log.d("1", "initFileListInfo: " + mFilePaths.size());
        if (menuPosition == 1 && !mCurrentFilePath.equals(mRootPath))
            {
            //只要当前路径不是手机根目录或者sd卡根目录，则显示"返回根目录"和"返回上一级"
            initAddBackUp(filePath, mRootPath);
        }else if (menuPosition == 2 && !mCurrentFilePath.equals(mSDCard)){
            initAddBackUp(filePath,mSDCard);
        }
        //将所有文件信息添加到集合中
        for (File mCurrentFile : mFiles){
            mFilename.add(mCurrentFile.getName());
            mFilePaths.add(mCurrentFile.getPath());
        }
        //适配数据
        setListAdapter(new FileAdapter(MainActivity.this,mFilename,mFilePaths));
    }

    private boolean isAddBackUp = false;
    private void initAddBackUp(String filePath,String phone_sdcard){
        if (!filePath.equals(phone_sdcard)){
            mFilename.add("BacktoRoot");//为列表的第一项设置返回根目录
            mFilePaths.add(phone_sdcard);
            mFilename.add("BacktoUp");
            mFilePaths.add(new File(filePath).getParent());
            isAddBackUp = true;
        }
    }

    private String mNewFolderName = "";
    private File mCreateFile;
    private RadioGroup mCreateRadioGroup;
    private static int mChecked;
    private void createFolder(){
        mChecked = 2;
        LayoutInflater mLI = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //初始化对话框布局
        final LinearLayout mLL = (LinearLayout) mLI.inflate(R.layout.create_dialog,null);
        mCreateRadioGroup = (RadioGroup) mLL.findViewById(R.id.radiogroup_create);
        final RadioButton mCreateFileButton = (RadioButton)mLL.findViewById(R.id.create_file);
        final RadioButton mCreateFolderButton = (RadioButton)mLL.findViewById(R.id.create_folder);
        mCreateFolderButton.setChecked(true);//设置默认为创建文件夹
        mCreateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mCreateFileButton.getId()){
                    mChecked = 1;
                }else if (checkedId == mCreateFolderButton.getId()){
                    mChecked = 2;
                }
            }
        });
        //显示对话框
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("新建")
                .setView(mLL)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获得用户输入的名称
                        mNewFolderName = ((EditText)mLL.findViewById(R.id.new_filename)).getText().toString();
                        if (mChecked == 1){
                            mCreateFile = new File(mCurrentFilePath + File.separator + mNewFolderName +".txt");
                            try {
                                mCreateFile.createNewFile();
                                //刷新当前目录文件列表
                                initFileListInfo(mCurrentFilePath);
                            } catch (IOException e) {
                                Toast.makeText(MainActivity.this, "文件名拼接出错", Toast.LENGTH_SHORT).show();
                            }
                        }else if(mChecked == 2){
                            mCreateFile = new File(mCurrentFilePath + File.separator + mNewFolderName);
                            if (!mCreateFile.exists()&&!mCreateFile.isDirectory()&&mNewFolderName.length()!=0){
                                if (mCreateFile.mkdirs()){
                                    //刷新当前目录问加你列表
                                    initFileListInfo(mCurrentFilePath);
                                }else {
                                    Toast.makeText(MainActivity.this, "创建失败，可能是系统权限不够，root一下?", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(MainActivity.this, "文件名为空还是重命名了呢？", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNeutralButton("取消",null);
        mBuilder.show();

    }

    EditText mET;
    //显示重命名对话框
    private void initRenameDialog(final File file) {
//        LayoutInflater mLI = LayoutInflater.from(MainActivity.this);
//        LinearLayout mLL = (LinearLayout) mLI.inflate(R.layout.rename_dialog,null);
        LinearLayout mLL = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.rename_dialog,null);
        mET = (EditText) mLL.findViewById(R.id.new_filename);
        //显示当前的文件名
        mET.setText(file.getName());
        //设置监听器
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String modifyName = mET.getText().toString();
                final String modifyFilePath = file.getParentFile().getPath()+File.separator;
                final String newFilePath = modifyFilePath + modifyName;
                //判断该新的文件名时候已经在当前目录下存在
                if (new File(newFilePath).exists()){
                    if (!modifyName.equals(file.getName())){//把"重命名"操作时没做任何修改的情况过滤；"!"表示非
                        //新命名后的文件已经存在的提示
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("提示")
                                .setMessage("文件名已存在，是否要覆盖?")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        file.renameTo(new File(newFilePath));
                                        Toast.makeText(MainActivity.this, "the file path is" + new File(newFilePath), Toast.LENGTH_SHORT).show();
                                        //更新当前目录信息
                                        initFileListInfo(file.getParentFile().getPath());
                                    }
                                }).setNegativeButton("取消",null).show();
                    }
                }else {
                    //文件名不重复是直接修改文件名后再次刷新列表
                    file.renameTo(new File(newFilePath));
                    initFileListInfo(file.getParentFile().getPath());
                }
            }
        };
        //现实对话框
        AlertDialog renameDialog = new AlertDialog.Builder(this).create();
        renameDialog.setView(mLL);
        renameDialog.setButton("确定",listener);
        renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //什么都不做，关闭当前对话框
            }
        });
        renameDialog.show();
    }

    private void initDeleteDialog(final File file) {
        new AlertDialog.Builder(this)
                .setTitle("提示!")
                .setMessage("您确定要删除该"+(file.isDirectory()?"文件夹":"文件")+"吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.isFile()){
                            file.delete();
                        }else {
                            //是文件夹则用这个方法删除
                            deleteFolder(file);
                        }
                        //重新遍历改文件的父目录
                        initFileListInfo(file.getParent());
                    }
                })
                .setNegativeButton("取消",null).show();
    }

    //删除文价夹的方法
    private void deleteFolder(File folder) {
        File[] fileArray = folder.listFiles();
        if (fileArray.length == 0){
            folder.delete();
        }else {
            //遍历该目录
            for (File currentFile:fileArray){
                if (currentFile.exists()&&currentFile.isFile()){
                    //文件则直接删除
                    currentFile.delete();
                }else {
                    //递归删除
                    deleteFolder(currentFile);
                }
            }
            folder.delete();
        }
    }

    //粘贴
    private void palseFie() {
        mNewFilePath = mCurrentFilePath + File.separator + mCopyFileName;
        Log.d("copy", "mOldFilePath is" + mOldFilePath + "| mNewFilePath is" + mNewFilePath + "| isCopy is" + isCopy);
        if (!mOldFilePath.equals(mNewFilePath)&&isCopy == true){
            //在不同的路径下复制才有效
            if (!new File(mNewFilePath).exists()){
                copyFile(mOldFilePath,mNewFilePath);
                Toast.makeText(this, "执行了粘贴", Toast.LENGTH_SHORT).show();
                initFileListInfo(mCurrentFilePath);
            }else {
                new AlertDialog.Builder(this)
                        .setTitle("提示!")
                        .setMessage("改文件名已存在,是否要覆盖?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyFile(mOldFilePath,mNewFilePath);
                                initFileListInfo(mCurrentFilePath);
                            }
                        })
                        .setNegativeButton("取消",null).show();
            }
        }else {
            Toast.makeText(this, "问复制文件!", Toast.LENGTH_SHORT).show();
        }
    }

    private int i;
    FileInputStream fis;
    FileOutputStream fos;
    //复制文件
    private void copyFile(String oldFile, String newFile) {
        try {
            fis = new FileInputStream(oldFile);
            fos = new FileOutputStream(newFile);
            do {
                //逐个byte读取文件,并写入另一个文件中
                if ((i = fis.read()) != -1){
                    fos.write(i);
                }
            }while (i != -1);
            //关闭输入流文件
            if (fis != null){
                fis.close();
            }
            //关闭文件输出流
            if (fos != null){
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示搜索对话框
    public static String KEYWORD_BROADCAST = "keyword_broadcast";
    private RadioGroup mRadioGroup;
    private static int mRadioChecked;
    Intent serviceIntent;
    private void searchDialog() {
        mRadioChecked = 1;//搜索目录是当前目录还是整个目录
//        LayoutInflater mLI = LayoutInflater.from(this);
//        final View mLL = (View) mLI.inflate(R.layout.search_dialog,null);
        final View mLL = LayoutInflater.from(this).inflate(R.layout.search_dialog,null);
        mRadioGroup = (RadioGroup) mLL.findViewById(R.id.radiogroup_search);
        final RadioButton mCurrentPathButton = mLL.findViewById(R.id.radio_currentpath);
        final RadioButton mWholePathButton = (RadioButton) mLL.findViewById(R.id.radio_wholepath);
        mCurrentPathButton.setChecked(true);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //当前路径的标志为1
                if (checkedId == mCurrentPathButton.getId()){
                    mRadioChecked = 1;
                }else if (checkedId == mWholePathButton.getId()){
                    mRadioChecked = 2;
                }
            }
        });

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this)
                .setTitle("搜索")
                .setView(mLL)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        keyWords = ((EditText) mLL.findViewById(R.id.edit_search)).getText().toString();
                        if (keyWords.length() == 0){
                            Toast.makeText(MainActivity.this, "关键字不能为空!", Toast.LENGTH_SHORT).show();
                            searchDialog();
                        }else {
                            if (menuPosition == 1){
                                mPath.setText(mRootPath);
                            }else {
                                mPath.setText(mSDCard);
                            }
                            //获取用户输入的关键字并发送广播-开始
                            Intent keywordIntent = new Intent();
                            keywordIntent.setAction(KEYWORD_BROADCAST);
                            //传递搜索的范围区间
                            if (mRadioChecked == 1){
                                keywordIntent.putExtra("searchPath",mCurrentFilePath);
                            }else {
                                keywordIntent.putExtra("searchPath",mSDCard);
                            }
                            //传递关键字
                            keywordIntent.putExtra("keyword",keyWords);
                            getApplicationContext().sendBroadcast(keywordIntent);
                            //获取用户输入的关键字并发送广播-结束
                            serviceIntent = new Intent("com.android.service.FILE_SEARCH_START");
                            MainActivity.this.startService(serviceIntent);
                            //开启服务,启动搜索
                            isComBackFromNotification = false;
                        }
                    }
                })
                .setNegativeButton("取消",null);
        mBuilder.create().show();
    }

    private IntentFilter mFilter;
    private FileBroadCast mFileBroadcast;
    private IntentFilter mIntentFilter;
    private SearchBroadCast mSearchBroadCast;

    @Override
    protected void onStart() {
        super.onStart();
        mFilter = new IntentFilter();
        mFilter.addAction(FileService.FILE_SEARCH_COMPLETED);
        mFilter.addAction(FileService.FILE_NOTIFICATION);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(KEYWORD_BROADCAST);
        if (mFileBroadcast == null){
            mFileBroadcast = new FileBroadCast();
        }
        if (mSearchBroadCast == null){
            mSearchBroadCast = new SearchBroadCast();
        }
        this.registerReceiver(mFileBroadcast,mFilter);
        this.registerReceiver(mSearchBroadCast,mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFilename.clear();
        mFilePaths.clear();
        this.unregisterReceiver(mFileBroadcast);
        this.unregisterReceiver(mSearchBroadCast);
    }

    private String mAction;
    /**
     * 内部广播类
     */
    private class FileBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAction = intent.getAction();
            if (FileService.FILE_SEARCH_COMPLETED.equals(mAction)){
                mFilename = intent.getStringArrayListExtra("mFileNameList");
                mFilePaths = intent.getStringArrayListExtra("mFilePathsList");
                Toast.makeText(context, "搜索完毕", Toast.LENGTH_SHORT).show();
                //这里搜索完毕之后应该弹出一个弹出框提示用户要不要显示数据
                searchCompletedDialog("搜索完毕，是否马上显示结果?");
                getApplicationContext().stopService(serviceIntent);
            }else if (FileService.FILE_NOTIFICATION.equals(mAction)){
                String mNotification = intent.getStringExtra("notification");
                Toast.makeText(context, "mNotification", Toast.LENGTH_SHORT).show();
                searchCompletedDialog("你确定要取消搜索吗？");
            }
        }
    }

    private void searchCompletedDialog(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //1.搜索完毕
                        if (FileService.FILE_SEARCH_COMPLETED.equals(mAction)){
                            if (mFilename.size()==0){
                                Toast.makeText(MainActivity.this, "无相关文件/文件夹", Toast.LENGTH_SHORT).show();
                                setListAdapter(new FileAdapter(MainActivity.this,mFilename,mFilePaths));
                            }else {
                                //显示文件列表
                                setListAdapter(new FileAdapter(MainActivity.this,mFilename,mFilePaths));
                            }
                        }else {
                            isComBackFromNotification = true;
                            getApplicationContext().stopService(serviceIntent);
                        }
                    }
                })
                .setNegativeButton("取消",null)
                .create()
                .show();
    }

    boolean isTxtData = false;
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        final File mFile = new File(mFilePaths.get(position));
        //如果文件是可读的，我们进去查看文件
        if (mFile.canRead()){
            if (mFile.isDirectory()){//如果是文件夹，查看文件夹目录
                initFileListInfo(mFilePaths.get(position));
            }else {
                //如果是文件，则用相应的打开方式打开
                String fileName = mFile.getName();
                String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1,fileName.length()).toLowerCase();
                if (fileEnds.equals("txt")){
                    //显示进度条，表示正在读取
                    initProgressDialig(ProgressDialog.STYLE_HORIZONTAL);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //打开文本文件
                            openTextFile(mFile.getPath());
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true){
                                if (isTxtData == true){
                                    //关闭进度条
                                    mProgressDialog.dismiss();
                                    executeIntent(txtData.toString(),mFile.getPath());
                                    break;
                                }
                            }
                        }
                    }).start();
                }else if (fileEnds.equals("html")||fileEnds.equals("mht")||fileEnds.equals("htm")){
                    Intent intent = new Intent(MainActivity.this,WebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("filePath",mFile.getPath());
                    startActivity(intent);
                }else {
                    openFile(mFile);
                }
            }
        }else {
            Toast.makeText(this, "对不起，您的访问权限不足", Toast.LENGTH_SHORT).show();
        }

    }

    //进度条
    ProgressDialog mProgressDialog;
    boolean isCancleProgressDialog = false;
    private void initProgressDialig(int style) {
        isCancleProgressDialog = false;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("正在为你解析文本数据,请稍后...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isCancleProgressDialog = true;
                mProgressDialog.dismiss();
            }
        });
        mProgressDialog.show();
    }

    /**
     *调用系统的方法，来打开文件的方法
     */
    private void openFile(File file) {
        if (file.isDirectory()){
            initFileListInfo(file.getPath());
        }else {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            //设置当前文件类型
            intent.setDataAndType(Uri.fromFile(file),getMIMEType(file));
            startActivity(intent);
        }
    }

    /**
     *获得MIME类型的方法
     */
    private String getMIMEType(File file) {
        String type = "";
        String fileName = file.getName();
        //取出文件后缀名并转成小写
        String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1,fileName.length()).toLowerCase();
        if (fileEnds.equals("m4a")||fileEnds.equals("mp3")||fileEnds.equals("mid")||fileEnds.equals("xmf")||
                fileEnds.equals("ogg")||fileEnds.equals("wav")){
            type = "audio/*";
        }else if (fileEnds.equals("3gp")||fileEnds.equals("mp4")){
            type = "video/*";
        }else if (fileEnds.equals("jpg")||fileEnds.equals("gif")||fileEnds.equals("png")||fileEnds.equals("jpeg")||fileEnds.equals("bmp")){
            type = "image/*";
        }else {
            type = "*/*";
        }
        return type;
    }

    String txtData = "";
    Boolean isTxtDataOK = false;
    //打开文本文件的方法,用于读取文件数据
    private void openTextFile(String file) {
        isTxtDataOK = false;
        try {
            fis = new FileInputStream(new File(file));
            StringBuilder mSb = new StringBuilder();
            int m;
            while ((m = fis.read()) != -1){
                mSb.append((char)m);
            }
            fis.close();
            txtData = mSb.toString();
            //读取完毕
            isTxtDataOK = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //执行Intent跳转的方法
    private void executeIntent(String data, String file){
        Intent intent = new Intent(MainActivity.this,EditTxtActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //传递文件的路径、标题和内容
        intent.putExtra("path",file);
        intent.putExtra("title",new File(file).getName());
        intent.putExtra("data",data.toString());
        startActivity(intent);
    }

}
