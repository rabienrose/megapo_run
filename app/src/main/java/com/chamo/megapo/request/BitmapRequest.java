package com.chamo.megapo.request;

import android.content.Context;
import android.widget.ImageView;
import com.chamo.megapo.listener.RequestListener;
import com.chamo.megapo.utils.MD5Util;

import java.lang.ref.SoftReference;

public class BitmapRequest {

    private String url;
    private SoftReference<ImageView> mReference;
    private String uriMD5;
    private int loadingResId;
    private Context mContext;
    private RequestListener mListener;

    public BitmapRequest(Context mContext) {
        this.mContext = mContext;
    }

    public BitmapRequest loading(int resId) {
        this.loadingResId = resId;
        return this;
    }

    public void into(ImageView view) {
        mReference = new SoftReference<>(view);
        view.setTag(uriMD5);
        RequestManager.getInstance().addBitmapRequest(this);
    }

    public BitmapRequest requestListener(RequestListener listener) {
        this.mListener = listener;
        return this;
    }

    public BitmapRequest load(String url) {
        this.url = url;
        uriMD5 = MD5Util.string2MD5(url);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUriMD5() {
        return uriMD5;
    }

    public void setUriMD5(String uriMD5) {
        this.uriMD5 = uriMD5;
    }

    public int getLoadingResId() {
        return loadingResId;
    }

    public void setLoadingResId(int loadingResId) {
        this.loadingResId = loadingResId;
    }

    public ImageView getImageView() {
        return mReference.get();
    }

    public RequestListener getmListener() {
        return mListener;
    }

    public Context getmContext() {
        return mContext;
    }
}
