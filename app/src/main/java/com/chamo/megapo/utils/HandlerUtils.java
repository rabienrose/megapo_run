package com.chamo.megapo.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import com.chamo.megapo.ui.MainActivity;

public class HandlerUtils {
    public MainActivity mainActivity = null;
    private long time;

    public HandlerUtils(MainActivity activity) {
        mainActivity = activity;
    }

    private Handler handler = new Handler() {

        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            time--;
            if (time == 0) {
                mainActivity.Event(0);
                handler.removeMessages(0);
            }
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    public Handler startHandlerTiming(int i) {
        time = 4;
        if (i != 1) {
            handler.removeMessages(0);

        } else {
//            handler.sendEmptyMessageDelayed(0, 4000);
        }
            handler.sendEmptyMessageDelayed(0, 1000);
        return handler;
    }


}
