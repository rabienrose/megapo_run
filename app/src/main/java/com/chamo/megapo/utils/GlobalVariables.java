package com.chamo.megapo.utils;

public class GlobalVariables {
	public final static int APPVER = 10000;
	public final static String APPAPK ="megapo.apk";
	public final static String APPOSS ="https://ride-v.oss-cn-beijing.aliyuncs.com/download/"+APPAPK;
	public final static String ROOT_PATH = android.os.Environment.getExternalStorageDirectory() + "/com.chamo.megapo/";
	public final static String LOG_FOLDER = ROOT_PATH + "log/";
	public final static String BASE_URL = "http://ride-v.oss-cn-beijing.aliyuncs.com";
	public final static String VIDEO = BASE_URL+"/phone_sport/video/";
	public final static String MUSIC = BASE_URL+"/phone_sport/music/";
	public final static String COVER = BASE_URL+"/phone_sport/cover/";
	public final static String VIDEOURl = "https://ride-v.oss-accelerate.aliyuncs.com/phone_sport/v_list_v2.txt";
	public final static String MUSICURl = "https://ride-v.oss-accelerate.aliyuncs.com/phone_sport/m_list_v2.txt";
	public final static String CONFIGURl = "https://ride-v.oss-accelerate.aliyuncs.com/config/ver.txt";
}
