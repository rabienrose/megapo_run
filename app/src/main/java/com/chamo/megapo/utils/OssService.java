package com.chamo.megapo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.chamo.megapo.model.LevelData;
import com.chamo.megapo.model.MusicData;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class OssService {
    private static final String TAG = "OssService";
    private OSS oss = null;
    private String accessKeyId;
    private String bucketName;
    private String accessKeySecret;
    private String endpoint;
    private Context context;
    static String BUCKET_NAME = "ride-v";
    static String OSS_ACCESS_KEY_ID = "LTAI4GJDtEd1QXeUPZrNA4Yc";
    static String OSS_ACCESS_KEY_SECRET = "rxWAZnXNhiZ8nemuvshvKxceYmUCzP";
    static String OSS_ENDPOINT="https://oss-accelerate.aliyuncs.com";
    public static String file_root = "/data/data/com.chamo.megapo/files/";
    public static HashMap<Integer,LevelData> levels= new HashMap<>();
    public static ArrayList<MusicData> musics= new ArrayList<>();
    public static boolean level_data_ready=false;
    public static boolean music_data_ready=false;
    public static boolean loading_img_ready=false;

    public OssService(Context context) {
        this.context = context;
        this.endpoint = OSS_ENDPOINT;
        this.bucketName = BUCKET_NAME;
        this.accessKeyId = OSS_ACCESS_KEY_ID;
        this.accessKeySecret = OSS_ACCESS_KEY_SECRET;
    }

    public static Bitmap get_a_loadning_img(){
        File file = new File(file_root);
        File[] files = file.listFiles();
        ArrayList<String> out_imgs=new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isFile() && files[i].getName().contains(".jpg")) {
                out_imgs.add(files[i].getName());
            }
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, out_imgs.size());
        String chosed_img_name=out_imgs.get(randomNum);
        Bitmap bmp = BitmapFactory.decodeFile(OssService.file_root+chosed_img_name);
        return bmp;
    }

    public static void fetch_a_loading_img(boolean fetch_first){
        loading_img_ready=false;
        File file = new File(file_root);
        File[] files = file.listFiles();
        ArrayList<String> out_imgs=new ArrayList<>();
        boolean has_img=false;
        for (int i = 0; i < files.length; i++) {
            if(files[i].isFile() && files[i].getName().contains(".jpg")) {
                has_img=true;
            }
        }
        String tmp_local_file = file_root+"1.jpg";
        String tmp_path=GlobalVariables.BASE_URL+"/phone_sport/loading_img/1.jpg";
        if (has_img==true){
            if (fetch_first){
                loading_img_ready=true;
                return;
            }
            Random fRandom = new Random();
            int i= (int)(Math.abs(fRandom.nextGaussian()*34));
            tmp_path=GlobalVariables.BASE_URL+"/phone_sport/loading_img/"+i+".jpg";
            tmp_local_file = file_root+i+".jpg";
        }
        File file1=new File(tmp_local_file);
        if (file1.exists()){
            loading_img_ready=true;
            return;
        }
        final String local_file=tmp_local_file;

        RequestParams params = new RequestParams(tmp_path);
        x.http().get(params, new Callback.CommonCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                try {
                    Files.write(Paths.get(local_file), result);
                    loading_img_ready=true;
                }catch (IOException e) {
                    OssService.appendErr(e);
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                loading_img_ready=true;
            }
            @Override
            public void onCancelled(CancelledException cex) {
                loading_img_ready=true;
            }
            @Override
            public void onFinished() {
                loading_img_ready=true;
            }
        });
    }

    public static void getDataMusic() {
        RequestParams params = new RequestParams(com.chamo.megapo.utils.GlobalVariables.MUSICURl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String[] ary = result.split("\n");
                    for (String item : ary) {
                        MusicData data= new MusicData();
                        data.name=item;
                        musics.add(data);
                    }
                    music_data_ready=true;
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                OssService.appendLog("fetch_music_list_err", "",false);
            }
            @Override
            public void onCancelled(CancelledException cex) {
                OssService.appendLog("fetch_music_list_cancel", "",false);
            }
            @Override
            public void onFinished() {
                OssService.appendLog("fetch_music_list_done","", false);
            }
        });
    }

    public static void fetch_level_data(){
        RequestParams params = new RequestParams(GlobalVariables.VIDEOURl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String[] ary = result.split("\n");
                    for (String item : ary) {

                        String[] ay = item.split(",");
                        LevelData data = new LevelData();
                        data.id=Integer.parseInt(ay[0]);
                        data.mp4=ay[1];
                        data.name=ay[1].split("\\.")[0];
                        data.cover=ay[2];
                        for (int i=3; i<ay.length; i++){
                            data.next_levs_id.add(Integer.parseInt(ay[i]));
                        }
                        levels.put(data.id, data);
                    }
                    level_data_ready=true;

                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                OssService.appendLog("fetch_video_list_err", "",false);
            }
            @Override
            public void onCancelled(CancelledException cex) {
                OssService.appendLog("fetch_video_list_cancel", "",false);
            }
            @Override
            public void onFinished() {
                OssService.appendLog("fetch_video_list_done", "",false);
            }
        });
    }

    public void initOSSClient() {
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(30 * 1000);
        conf.setSocketTimeout(60 * 1000);
        conf.setMaxConcurrentRequest(10);
        conf.setMaxErrorRetry(1);
        oss = new OSSClient(context, endpoint, credentialProvider, conf);
    }

    public static void upload_file(Context context, String uuid){
        appendLog("upload_log","",false);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String strDate = formatter.format(date);
        String mFilePath=file_root+"/chamo.txt";
        OssService ossService = new OssService(context);
        ossService.initOSSClient();
        String real_file_path = "client_log/"+strDate+"_"+uuid+".txt";
        ossService.beginupload(context, real_file_path, mFilePath);
    }

    static public void appendErr(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        appendLog("crash",sStackTrace,false);
        e.printStackTrace();
    }

    static public void appendLog(String key, String text, boolean b_new)
    {
        File folder1 = new File(file_root);
        if (!folder1.exists()){
            folder1.mkdirs();
        }
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String strDate = formatter.format(date);
        text="["+strDate+"]"+key+"|"+text;
        File logFile = new File(file_root+"/chamo.txt");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            if (b_new){
                logFile.delete();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void beginupload(Context context, String objectname, String mFilePath) {
        if (objectname == null || objectname.equals("") || mFilePath.equals("")) {
            return;
        }
        PutObjectRequest put = new PutObjectRequest(bucketName, objectname, mFilePath);
        if (mFilePath == null || mFilePath.equals("")) {
            return;
        }
        // @SuppressWarnings("rawtypes")
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                appendLog("new_log","",true);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            }
        });
    }
}
