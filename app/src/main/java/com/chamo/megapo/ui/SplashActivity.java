package com.chamo.megapo.ui;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.chamo.megapo.R;
import com.chamo.megapo.adapter.GuidPagerAdapter;
import com.chamo.megapo.ui.base.ManageActivity;
import com.chamo.megapo.utils.GlobalFunction;
import com.chamo.megapo.utils.GlobalVariables;
import com.chamo.megapo.utils.OssService;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.x;
import java.io.File;

@ContentView(R.layout.splash_activity)
public class SplashActivity extends ManageActivity {
    ImageView btn;
    public static Bitmap btp;
    private ImageView mMImageView;
    int[] mGuidArray = {
            R.mipmap.banner1,
            R.mipmap.banner2,
            R.mipmap.banner3,
            R.mipmap.banner4
    };
    SharedPreferences preferences;
    private ViewPager mGuidViewPager;
    private Button jump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        try {
            x.view().inject(this);
            initData();
            initListener();
        }catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    public void downloadUpdate() {

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = GlobalVariables.APPAPK;
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);
        File file = new File(destination);
        if (file.exists())
            file.delete();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(GlobalVariables.APPOSS));
        request.setDestinationUri(uri);
        dm.enqueue(request);
        final String finalDestination = destination;
        final BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri contentUri = FileProvider.getUriForFile(ctxt, "com.chamo.megapo.fileprovider", new File(finalDestination));
                    Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                    openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    openFileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    openFileIntent.setData(contentUri);
                    startActivity(openFileIntent);
                    unregisterReceiver(this);
                    finish();
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri,
                            "application/vnd.android.package-archive");
                    startActivity(install);
                    unregisterReceiver(this);
                    finish();
                }
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private int get_ver_int(String ver_str){
        String[] vec_s = ver_str.split("\\.");
        String ver_s=vec_s[0]+vec_s[1]+vec_s[2];
        int ver_id=Integer.parseInt(ver_s);
        return ver_id;
    }

    private void initData() {
        RequestParams params = new RequestParams(GlobalVariables.CONFIGURl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String[] vec = result.split(",");
                String force_ver=vec[0];
                int force_ver_id  = get_ver_int(force_ver);
                if (force_ver_id>GlobalVariables.APPVER){
                    GlobalFunction.showToast("更新软件中");
                    btn.setVisibility(View.GONE);
                    jump.setVisibility(View.GONE);
                    downloadUpdate();
                    OssService.appendLog("start_download_app|"+GlobalVariables.APPVER+"|"+force_ver_id, false);
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) { }
            @Override
            public void onCancelled(CancelledException cex) { }
            @Override
            public void onFinished() { }
        });
        preferences = getSharedPreferences("state", MODE_PRIVATE);
        btn = (ImageView) findViewById(R.id.start);
        jump = (Button) findViewById(R.id.jump);
//        if (!preferences.getBoolean("isLogin", false)) {
//            btn.setVisibility(View.GONE);
//            jump.setVisibility(View.GONE);
//        }
        mGuidViewPager = findViewById(R.id.guid_viewPager);
        mGuidViewPager.setAdapter(new GuidPagerAdapter(mGuidArray,SplashActivity.this, mMImageView, btp));
    }

    private void initListener() {
        mGuidViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if (position == 0 && preferences.getBoolean("isLogin", false)) {
//                    jump.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onPageSelected(int position) {
//                if (position == mGuidViewPager.getAdapter().getCount() - 1) {
//                    btn.setVisibility(View.VISIBLE);
//                } else {
//                    btn.setVisibility(View.INVISIBLE);
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }

        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

    }
    private void start() {
        preferences.edit().putBoolean("isLogin",true).commit();
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btp != null) {
            btp.recycle();
            btp = null;
        }
        if (mMImageView != null) {
            try {
                mMImageView.setBackground(null);
            } catch (Exception e) {
            }
        }
        System.gc();
    }
}
