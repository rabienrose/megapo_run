package com.chamo.megapo.utils;


import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import com.chamo.megapo.lifecycle.RequestManagerFragment;
import com.chamo.megapo.request.BitmapRequest;

public class Glide {

    public static BitmapRequest with(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        RequestManagerFragment current = (RequestManagerFragment) fm.findFragmentByTag("com.zsd.glide");
        if (current == null) {
            current = new RequestManagerFragment();
            fm.beginTransaction().add(current, "com.zsd.glide").commitAllowingStateLoss();
        }
        return new BitmapRequest(activity);
    }
}
