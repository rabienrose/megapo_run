package com.chamo.megapo.cache;

import android.content.Context;
import android.graphics.Bitmap;
import com.chamo.megapo.request.BitmapRequest;

public class DoubleLruCache implements BitmapCache {

    private MemoryLruCache lruCache;
    private DiskBitmapCache bitmapCache;
    private static volatile DoubleLruCache instance;

    private DoubleLruCache(Context context){
        bitmapCache = DiskBitmapCache.getInstance(context);
        lruCache = MemoryLruCache.getInstance();
    }

    public static DoubleLruCache getInstance(Context context) {
        DoubleLruCache cache = instance;
        if (cache == null) {
            synchronized (DoubleLruCache.class) {
                if (cache == null) {
                    cache = new DoubleLruCache(context);
                }
                instance = cache;
            }
        }
        return cache;
    }

    public static DoubleLruCache getInstance() {
        if (instance == null) {
            throw new RuntimeException("请在application初始化");
        }
        return instance;
    }

    @Override
    public void put(BitmapRequest request, Bitmap bitmap) {
        lruCache.put(request, bitmap);
        bitmapCache.put(request, bitmap);
    }

    @Override
    public Bitmap get(BitmapRequest request) {
        Bitmap bitmap = lruCache.get(request);
        if (bitmap == null){
            bitmap = bitmapCache.get(request);
            lruCache.put(request,bitmap);
        }
        return bitmap;
    }

    @Override
    public void remove(BitmapRequest request) {
        lruCache.remove(request);
        bitmapCache.remove(request);
    }

    @Override
    public void remove(int activityCode) {
        lruCache.remove(activityCode);
        bitmapCache.remove(activityCode);
    }
}
