package com.github.xch168.videoeditor.helper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import com.github.xch168.videoeditor.App;
import com.github.xch168.videoeditor.entity.Picture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XuCanHui on 2019/12/25.
 */
public class PictureManger {
    private static PictureManger sInstance;

    private ContentResolver mContentResolver;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    public static PictureManger getInstance() {
        if (sInstance == null) {
            sInstance = new PictureManger();
        }
        return sInstance;
    }

    private PictureManger() {
        mContentResolver = App.getContext().getContentResolver();

        mHandlerThread = new HandlerThread("picture-manger");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public List<Picture> getAllPicture() {
        List<Picture> picList = new ArrayList<>();
        String[] mediaColumns = new String[] {
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DESCRIPTION
        };
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);
        if (cursor == null) {
            return picList;
        }
        while (cursor.moveToNext()) {
            Picture pic = new Picture();
            pic.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            File file = new File(pic.getFilePath());
            boolean canRead = file.canRead();
            long length = file.length();
            if (!canRead || length == 0) {
                continue;
            }
            pic.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
            picList.add(pic);
        }
        cursor.close();
        return picList;
    }

    public void getAllPicture(final PictureManger.OnLoadCompletionListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    List<Picture> picList = getAllPicture();
                    listener.onLoadCompletion(picList);
                }
            }
        });
    }

    public interface OnLoadCompletionListener {
        void onLoadCompletion(List<Picture> pictureList);
    }

}
