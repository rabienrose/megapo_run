package com.chamo.megapo.cache;

import android.graphics.Bitmap;
import com.chamo.megapo.request.BitmapRequest;

public interface BitmapCache {

    /**
     * 存入内存
     * @param request
     * @param bitmap
     */
    void put(BitmapRequest request, Bitmap bitmap);

    /**
     * 读取内存
     * @param request
     * @return
     */
    Bitmap get(BitmapRequest request);

    /**
     * 清除内存图片
     * @param request
     */
    void remove(BitmapRequest request);

    void remove(int activityCode);
}
