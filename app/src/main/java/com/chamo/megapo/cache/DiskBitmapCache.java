package com.chamo.megapo.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import com.chamo.megapo.cache.disk.DiskLruCache;
import com.chamo.megapo.cache.disk.IOUtil;
import com.chamo.megapo.request.BitmapRequest;


import java.io.*;

public class DiskBitmapCache implements BitmapCache {

    private static volatile DiskBitmapCache instance;
    private DiskLruCache diskLruCache;
    private static final byte[] lock = new byte[0];
    private String imageCachePath = "Image";
    private int MB = 1024 * 1024;
    private int maxDiskSize = 50 * MB;

    private DiskBitmapCache(Context mContext) {
        File cacheFile = getImageCacheFile(mContext, imageCachePath);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        try {
            diskLruCache = DiskLruCache.open(cacheFile, getAppVersion(mContext), 1, maxDiskSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DiskBitmapCache getInstance(Context mContext) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DiskBitmapCache(mContext);
                }
            }
        }
        return instance;
    }

    private File getImageCacheFile(Context mContext, String imageCachePath) {
        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = mContext.getExternalCacheDir().getPath();
        } else {
            path = mContext.getCacheDir().getPath();
        }
        return new File(path + File.separator + imageCachePath);
    }

    private int getAppVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private boolean presetBitmap2Disk(OutputStream outputStream, Bitmap bitmap) {
        BufferedOutputStream bufferedOutputStream = null;
        try {

            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bufferedOutputStream);
            bufferedOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtil.closeQuietly(bufferedOutputStream);
        }
        return false;
    }

    @Override
    public void put(BitmapRequest request, Bitmap bitmap) {
        DiskLruCache.Editor editor;
        OutputStream os = null;
        try {
            editor = diskLruCache.edit(request.getUriMD5());
            os = editor.newOutputStream(0);
            if (presetBitmap2Disk(os, bitmap)) {
                editor.commit();
            } else {
                editor.abort();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap get(BitmapRequest request) {
        InputStream stream = null;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(request.getUriMD5());
            if (snapshot != null){
                stream = snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtil.closeQuietly(stream);
        }
        return null;
    }

    @Override
    public void remove(BitmapRequest request) {
        try {
            diskLruCache.remove(request.getUriMD5());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(int activityCode) {

    }
}
