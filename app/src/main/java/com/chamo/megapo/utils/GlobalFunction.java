package com.chamo.megapo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.chamo.megapo.MyApp;
import com.chamo.megapo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalFunction {
	public static void showToast(String content) {
		try {
			Context context = MyApp.getInstance();
			LayoutInflater inflater = LayoutInflater.from(context);
			View layout = inflater.inflate(R.layout.toast, null);
			TextView text = (TextView) layout.findViewById(R.id.toast_text);
			text.setText(content);
			layout.getBackground().setAlpha(125);// 0~255透明度值
			Toast toast = new Toast(context);
			toast.setGravity(Gravity.CENTER, 0, -70);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	public static void showToast(Context context, String content) {
		try {
			LayoutInflater inflater = LayoutInflater.from(context);
			View layout = inflater.inflate(R.layout.toast, null);
			TextView text = (TextView) layout.findViewById(R.id.toast_text);
			text.setText(content);
			layout.getBackground().setAlpha(125);// 0~255透明度值
			Toast toast = new Toast(context);
			toast.setGravity(Gravity.CENTER, 0, -70);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(layout);
			toast.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}
}
