package com.chamo.megapo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import android.util.Log;
import com.chamo.megapo.cache.DoubleLruCache;
import com.chamo.megapo.utils.CrashHandler;
import com.chamo.megapo.utils.GlobalVariables;
import com.chamo.megapo.utils.OssService;
import com.richard.tool.database.BaseModelManager;
import com.yanzhenjie.nohttp.NoHttp;
import org.xutils.x;
import java.util.UUID;

public class MyApp extends MultiDexApplication {
    private static final String TAG = "MyApp";
    public static String PROCESS_NAME_XXXX = "process_name_xxxx";
    private static MyApp mInstance;
    public static Context mContext;
    public SharedPreferences trackConf = null;
    public static int screenWidth;
    public static int screenHeight;
    public static float screenDensity;
    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        mContext = getApplicationContext();
        mInstance = this;
        initScreenSize();
        NoHttp.initialize(this); // NoHttp默认初始化。
        BaseModelManager.currentUser = "megapo";
        DoubleLruCache.getInstance(this);
        SharedPreferences user_data = getSharedPreferences("state", MODE_PRIVATE);
        String uuid=user_data.getString("uuid","");
        if (uuid!=""){
            OssService.appendLog("login|"+ GlobalVariables.APPVER,false);
        }else{
            String id = UUID.randomUUID().toString();
            user_data.edit().putString("uuid",id).commit();
            OssService.appendLog("first_login|"+ GlobalVariables.APPVER,false);
        }
    }

    public static Context getInstance() {
        return mInstance;
    }

    private void initScreenSize() {
        DisplayMetrics curMetrics = getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = curMetrics.widthPixels;
        screenHeight = curMetrics.heightPixels;
        screenDensity = curMetrics.density;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
