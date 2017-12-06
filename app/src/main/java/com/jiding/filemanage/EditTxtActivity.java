package com.jiding.filemanage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class EditTxtActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtEditText;
    private TextView txtTextTitle;
    private Button txtSaveButton;
    private Button txtCancleButton;
    private String txtTitle;
    private String txtData;
    private String txtPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_txt);
        initContentView();
        txtPath = getIntent().getStringExtra("path");
        txtTitle = getIntent().getStringExtra("title");
        txtData = getIntent().getStringExtra("data");
        try {
            txtData = new String(txtData.getBytes("ISO-8859-1"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        txtTextTitle.setText(txtTitle);
        txtEditText.setText(txtData);
    }

    /**组件初始化*/
    private void initContentView() {
        txtEditText = (EditText) findViewById(R.id.EditTextDetail);
        txtTextTitle = (TextView) findViewById(R.id.TextViewTitle);
        txtSaveButton = (Button) findViewById(R.id.ButtonRefer);
        txtCancleButton = (Button) findViewById(R.id.ButtonBack);
        txtSaveButton.setOnClickListener(this);
        txtCancleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == txtSaveButton.getId()){
            saveTxt();
        }else if (v.getId() == txtCancleButton.getId()){
            EditTxtActivity.this.finish();
        }
    }

    private void saveTxt() {
        String newData = txtEditText.getText().toString();
        try {
            BufferedWriter mBW = new BufferedWriter(new FileWriter(new File(txtPath)));
            mBW.write(newData,0,newData.length());
            mBW.newLine();
            mBW.close();
            Toast.makeText(this, "成功保存!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "存储文件时出现了异常!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.finish();
    }
}
