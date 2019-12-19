package com.m4399.videoeditor.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.m4399.videoeditor.R;
import com.m4399.videoeditor.util.AppUtil;

public class MainActivity extends AppCompatActivity {
    public static final int TYPE_CLIP = 0;
    public static final int TYPE_CLIP_COMPOSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVersionView();
    }

    private void initVersionView() {
        TextView versionView = findViewById(R.id.tv_version);
        versionView.setText("v" + AppUtil.getVersionName());
    }

    public void videoClip(View view) {
        VideoListActivity.open(this, TYPE_CLIP);
    }

    public void videoClipAndCompose(View view) {
        VideoListActivity.open(this, TYPE_CLIP_COMPOSE);
    }
}
