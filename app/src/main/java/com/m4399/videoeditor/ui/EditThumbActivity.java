package com.m4399.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.m4399.videoeditor.R;

public class EditThumbActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_thumb);

        setTitle("编辑封面");
    }

    public static void open(Context context, String videoPath)
    {
        Intent intent = new Intent(context, EditThumbActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }
}
