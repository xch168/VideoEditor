package com.github.xch168.videoeditor.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.util.AppUtil;

public class MainActivity extends BaseActivity {
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
