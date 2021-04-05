package com.chamo.megapo.cache;

import android.graphics.Bitmap;
import android.util.LruCache;
import com.chamo.megapo.request.BitmapRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryLruCache implements BitmapCache {

    private static volatile MemoryLruCache instance;
    private LruCache<String, Bitmap> lruCache;
    private HashMap<String, Integer> activityCache;
    private static final byte[] lock = new byte[0];

    public static MemoryLruCache getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new MemoryLruCache();
                }
            }
        }
        return instance;
    }

    private MemoryLruCache() {
        final int maxMemorySize = 1024 * 1024 * 1024;
        lruCache = new LruCache<String, Bitmap>(maxMemorySize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //一张图片的大小
                return value.getRowBytes() * value.getHeight();
            }
        };
        activityCache = new HashMap<>();
    }


    @Override
    public void put(BitmapRequest request, Bitmap bitmap) {
        if (bitmap != null) {
            String uriMD5 = request.getUriMD5();
            lruCache.put(uriMD5, bitmap);
            activityCache.put(uriMD5, request.getmContext().hashCode());
        }
    }

    @Override
    public Bitmap get(BitmapRequest request) {
        Bitmap bitmap = lruCache.get(request.getUriMD5());
        return bitmap;
    }

    @Override
    public void remove(BitmapRequest request) {
        lruCache.remove(request.getUriMD5());
    }

    @Override
    public void remove(int activityCode) {
        ArrayList<String> tempUriMD5List = new ArrayList<>();
        for (String uriMD5 : activityCache.keySet()) {
            if (activityCache.get(uriMD5).intValue() == activityCode) {
                tempUriMD5List.add(uriMD5);
            }
        }
        //移除
        for (String uriMd5 : tempUriMD5List) {
            activityCache.remove(uriMd5);
            Bitmap bitmap = lruCache.get(uriMd5);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            lruCache.remove(uriMd5);
            bitmap = null;
        }
        if (!tempUriMD5List.isEmpty()) {
            System.gc();
        }
    }
}
