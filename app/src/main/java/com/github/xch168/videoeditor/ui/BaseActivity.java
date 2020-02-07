package com.github.xch168.videoeditor.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initProgressDialog();
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("正在处理");
    }

    public void showProgressDialog() {
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

    public void updateProgress(float percent) {
        mProgressDialog.setProgress((int) (percent * 100));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    protected void setBackBtnVisible(boolean visible) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(visible);
        }
    }
}
