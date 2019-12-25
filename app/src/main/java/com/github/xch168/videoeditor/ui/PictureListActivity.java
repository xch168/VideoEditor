package com.github.xch168.videoeditor.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.adapter.PictureAdapter;
import com.github.xch168.videoeditor.entity.Picture;
import com.github.xch168.videoeditor.helper.PictureManger;
import com.github.xch168.videoeditor.util.SpacingDecoration;

import java.util.List;

public class PictureListActivity extends BaseActivity implements PictureAdapter.OnItemClickListener {

    private RecyclerView mPictureRecyclerView;

    private PictureAdapter mPictureAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_list);

        setTitle("图片列表");
        setBackBtnVisible(true);

        initPictureListView();

        loadData();
    }

    private void initPictureListView() {
        mPictureAdapter = new PictureAdapter();

        mPictureRecyclerView = findViewById(R.id.recycler_view);
        mPictureRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mPictureRecyclerView.addItemDecoration(new SpacingDecoration(this, 10, 10, true));
        mPictureRecyclerView.setAdapter(mPictureAdapter);
    }

    @Override
    public void onItemClick(int position, Picture picture) {

    }

    private void loadData() {
        PictureManger.getInstance().getAllPicture(new PictureManger.OnLoadCompletionListener() {
            @Override
            public void onLoadCompletion(List<Picture> pictureList) {
                mPictureAdapter.setPictureList(pictureList);
            }
        });
    }

    public static void open(Context context) {
        Intent intent = new Intent(context, PictureListActivity.class);
        context.startActivity(intent);
    }
}
