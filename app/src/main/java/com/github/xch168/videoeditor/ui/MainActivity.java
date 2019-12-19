package com.github.xch168.videoeditor.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.util.AppUtil;

public class MainActivity extends BaseActivity {
    public static final int TYPE_VIDEO_CLIP = 0;
    public static final int TYPE_VIDEO_COVER = 1;
    public static final int TYPE_VIDEO_CLIP_COMPOSE = 2;

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
        VideoListActivity.open(this, TYPE_VIDEO_CLIP);
    }

    public void toChooseVideoCover(View view)
    {
        VideoListActivity.open(this, TYPE_VIDEO_COVER);
    }

    public void videoClipAndCompose(View view) {
        VideoListActivity.open(this, TYPE_VIDEO_CLIP_COMPOSE);
    }

}
