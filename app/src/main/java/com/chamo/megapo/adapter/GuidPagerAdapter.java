package com.chamo.megapo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chamo.megapo.R;
import com.yanzhenjie.nohttp.Logger;

import java.io.InputStream;

public class GuidPagerAdapter extends PagerAdapter {
    private int mGuidArray[];
    private Context mContext;
    private ImageView mMImageView;
    private Bitmap btp;

    public GuidPagerAdapter(int[] mGuidArray, Context mContext, ImageView mMImageView, Bitmap btp) {
        this.mGuidArray = mGuidArray;
        this.mContext = mContext;
        this.mMImageView = mMImageView;
        this.btp = btp;
    }

    @Override
    public int getCount() {
        if (mGuidArray != null) {
            return mGuidArray.length;
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //view
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        //data
        int resId = mGuidArray[position];
        try {
            getBitmapForImgResourse(mContext, resId, imageView);
        } catch (Exception e) {
        }

        //加入容器
        container.addView(imageView);

        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    /**
     * 大图片处理机制
     * 利用Bitmap 转存 R图片
     */

    public void getBitmapForImgResourse(Context mContext, int imgId, ImageView mImageView) {
        try {
            mMImageView = mImageView;
            InputStream is = mContext.getResources().openRawResource(imgId);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inSampleSize = 1;
            btp = BitmapFactory.decodeStream(is, null, options);
            if (btp != null && mImageView != null) {//防止使用的时候,该图片已经被回收了.
                mImageView.setImageBitmap(btp);
            }
            is.close();
        } catch (Exception e) {
//            Logger.e("TAG", "E------" + e.getMessage());
        }
    }


}

