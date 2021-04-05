package com.chamo.megapo.request;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import com.chamo.megapo.MyApp;
import com.chamo.megapo.cache.DoubleLruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

public class BitmapDispatcher extends Thread {

    private DoubleLruCache lruCache;
    private LinkedBlockingQueue<BitmapRequest> requestQueue;
    private Handler handler = new Handler(Looper.getMainLooper());

    public BitmapDispatcher(LinkedBlockingQueue<BitmapRequest> requestQueue) {
        this.requestQueue = requestQueue;
        lruCache = DoubleLruCache.getInstance(MyApp.getInstance());
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                BitmapRequest request = requestQueue.take();
                showLoadingImage(request);
                Bitmap bitmap = findBitmap(request);
                deliveryUIThread(request, bitmap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置图片
     * @param request
     * @param bitmap
     */
    private void deliveryUIThread(BitmapRequest request, Bitmap bitmap) {
        handler.post(() -> {
            ImageView imageView = request.getImageView();
            if (imageView != null && bitmap != null && imageView.getTag().equals(request.getUriMD5())) {
                imageView.setImageBitmap(bitmap);
            }
        });

        //监听
        if (request.getmListener() != null) {
            if (bitmap != null) {
                handler.post(() -> request.getmListener().onResourceReady(bitmap));
            } else {
                handler.post(() -> request.getmListener().onException());
            }
        }
    }

    /**
     * 获取图片
     * 先从本地获取，如果本地没有再去网络获取
     * @param request
     * @return
     */
    private Bitmap findBitmap(BitmapRequest request) {
        Bitmap bitmap = lruCache.get(request);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = downloadImage(request.getUrl());
        if (bitmap != null) {
            lruCache.put(request, bitmap);
        }
        return bitmap;
    }

    /**
     * 下载图片
     * @param url
     * @return
     */
    private Bitmap downloadImage(String url) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            URL mUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
            is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
        }
        return bitmap;
    }

    /**
     * 显示预览图片
     * @param request
     */
    private void showLoadingImage(BitmapRequest request) {
        final int loadingResId = request.getLoadingResId();
        if (loadingResId > 0) {
            ImageView imageView = request.getImageView();
            if (imageView != null) {
                handler.post(() -> {
                    imageView.setImageResource(loadingResId);
                });
            }
        }
    }
}
