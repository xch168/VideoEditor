package com.m4399.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.m4399.videoeditor.R;

public class VideoClipActivity extends AppCompatActivity
{
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clip);

        setTitle("剪辑视频");

        videoPath = getIntent().getStringExtra("video_path");
    }

    public static void open(Context context, String videoPath)
    {
        Intent intent = new Intent(context, VideoClipActivity.class);
        intent.putExtra("video_path", videoPath);
        context.startActivity(intent);
    }
}
