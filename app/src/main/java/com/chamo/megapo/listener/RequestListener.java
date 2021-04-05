package com.chamo.megapo.listener;

import android.graphics.Bitmap;

public interface RequestListener {

    void onResourceReady(Bitmap bitmap);

    void onException();
}
