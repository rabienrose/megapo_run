package com.chamo.megapo.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.chamo.megapo.R;
import com.chamo.megapo.constant.ConstantKeys;
import com.chamo.megapo.listener.OnCompletedListener;
import com.chamo.megapo.listener.OnPlayOrPauseListener;
import com.chamo.megapo.model.LevelData;
import com.chamo.megapo.ui.base.ManageFragmentActivity;
import com.chamo.megapo.utils.OssService;
import com.chamo.megapo.utils.GlobalVariables;
import com.chamo.megapo.utils.GlobalFunction;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;
import com.chamo.megapo.player.VideoPlayer;
import com.chamo.megapo.utils.ScreenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@ContentView(R.layout.activity_main)
public class MainActivity extends ManageFragmentActivity implements View.OnClickListener, SensorEventListener {
    private long exitTime;
    private MediaPlayer mediaPlayer = null;
    private VideoPlayer videoPlayer = null;
    private boolean isFirst = true;
    public SensorManager sm;
    private Sensor acceleromererSensor;
    private float aLast = 0.0f;
    private TextView tvLog;
    private int time_count = 0;
    private float coef_imu = 0.1f;
    private float f1 = 0.03f;
    private float minSpeed = 0.5f;
    private float maxSpeed = 2f;
    private ImageView imageMusic;
    private ImageView imageVideo;
    private ImageView imageVoice;
    private ImageView imageNext;
    private ImageView loading_img;
    private View root_view;
    private List<ImageView> buttonItems = new ArrayList<ImageView>(3);
    private int flag = 1;
    private SharedPreferences preferences;
    private float t_imu_intense;
    private float t_speed;
    private float t_v_speed;
    private int stats_count=0;
    private boolean first_play=true;
    private ArrayList<View> level_views=new ArrayList<>();
    private Timer timer = new Timer();
    private HashMap<Integer,Integer> next_level_ids=new HashMap<>();

    class UpdatTask extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (videoPlayer.getCurrentState()==ConstantKeys.CurrentState.STATE_PLAYING){
                        preferences.edit().putLong("cur_v_pos",videoPlayer.getCurrentPosition()).commit();
                    }
                }
            });
        }
    }

    void show_loading_img(boolean b_show){
        if (b_show){
            Bitmap bmp = OssService.get_a_loadning_img();
            loading_img.setImageBitmap(bmp);
            loading_img.setVisibility(View.VISIBLE);
            imageMusic.setVisibility(View.GONE);
            imageVideo.setVisibility(View.GONE);
        }else{
            loading_img.setVisibility(View.GONE);
            imageMusic.setVisibility(View.VISIBLE);
            imageVideo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            preferences = getSharedPreferences("state", MODE_PRIVATE);
            x.view().inject(this);
            initView();
            initData();
            initListenter();
            int musicIndex=preferences.getInt("cur_music_id",0);
            playMusic(OssService.musics.get(musicIndex).name);
            Long cur_play_pos = preferences.getLong("cur_v_pos",0);
            if(cur_play_pos!=-1){
                play();
            }else{
                show_loading_img(true);
                show_v_list(true);
            }
            TimerTask update_task = new UpdatTask();
            timer.scheduleAtFixedRate(update_task, 1000, 1000);
            coef_imu = preferences.getFloat("coef_imu",0.1f);
        }catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    private void initData() {
        buttonItems.add(imageVoice);
        buttonItems.add(imageNext);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acceleromererSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(this, acceleromererSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initListenter() {
        imageMusic.setOnClickListener(this);
        imageVoice.setOnClickListener(this);
        imageNext.setOnClickListener(this);
        imageVideo.setOnClickListener(this);
        videoPlayer.setOnClickListener(this);
        videoPlayer.setOnCompletedListener(new OnCompletedListener() {
            @Override
            public void onCompleted() {
                try {
                    show_v_list(false);
                    int cur_video_id = preferences.getInt("cur_video_id",1);
                    LevelData data = OssService.levels.get(cur_video_id);
                    String cur_lev_name=data.name;
                    String end_str = "done_video|";
                    end_str = end_str +  cur_lev_name+ "|";
                    end_str = end_str + t_imu_intense / stats_count + "|";
                    end_str = end_str + t_speed / stats_count + "|";
                    end_str = end_str + t_v_speed / stats_count + "|";
                    float avg_s=t_v_speed / stats_count;
                    coef_imu=coef_imu*(1/avg_s);
                    preferences.edit().putFloat("coef_imu",coef_imu).commit();
                    OssService.appendLog(end_str, false);
                    show_v_list(false);

                    int count = preferences.getInt(cur_lev_name+"_count",0);
                    preferences.edit().putLong("cur_v_pos", -1).commit();

                    for (int i=0; i<data.next_levs_id.size(); i++){
                        String next_lev_name=OssService.levels.get(data.next_levs_id.get(i)).name;
                        int count1 = preferences.getInt(next_lev_name+"_count",-1);
                        if (count1==-1){
                            preferences.edit().putInt(next_lev_name+"_count", 0).commit();
                        }
                    }
                    count=count+1;
                    preferences.edit().putInt(cur_lev_name+"_count", count).commit();
                    OssService.fetch_a_loading_img(false);
                    show_v_list(true);
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }
        });
        videoPlayer.setOnPlayOrPauseListener(new OnPlayOrPauseListener() {
            @Override
            public void onPlayOrPauseClick(boolean isPlaying) {
                try {
                    if (isPlaying){
                        show_loading_img(false);
                    }
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }
        });
    }

    protected void initView() {
        tvLog = (TextView) findViewById(R.id.tv_log);
        videoPlayer = (VideoPlayer) findViewById(R.id.video_player);

        videoPlayer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                videoPlayer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int w= (int) ( videoPlayer.getHeight()*1.778125);
                int h=videoPlayer.getHeight();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
                int margin=(int)((videoPlayer.getWidth() - w)/2);
                lp.setMargins(margin,0,margin,0);
                videoPlayer.setLayoutParams(lp);
            }
        });
        imageMusic = (ImageView) findViewById(R.id.image_music);
        imageVoice = (ImageView) findViewById(R.id.image_voice);
        imageNext = (ImageView) findViewById(R.id.image_next);
        imageVideo = (ImageView) findViewById(R.id.image_video);
        root_view = (View) findViewById(R.id.relative_layout);
        loading_img=(ImageView) findViewById(R.id.video_loading);
    }

    private void next() {
        int cur_music_id = preferences.getInt("cur_music_id",1);
        cur_music_id++;
        cur_music_id = cur_music_id % OssService.musics.size();
        playMusic(OssService.musics.get(cur_music_id).name);
        preferences.edit().putInt("cur_music_id",cur_music_id).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                GlobalFunction.showToast(getApplicationContext(), "再按一次退出程序");
                exitTime = System.currentTimeMillis();
            } else {
                ScreenManager.getScreenManager().popActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer!= null) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer!= null) {
            mediaPlayer.pause();
        }
    }

    public void play() {
        try {
            int levelIndex = preferences.getInt("cur_video_id",-1);
            if (levelIndex==-1){
                levelIndex=1;
                preferences.edit().putInt("cur_video_id",levelIndex).commit();
            }
            String barename = OssService.levels.get(levelIndex).name;
            String full_path=GlobalVariables.VIDEO+OssService.levels.get(levelIndex).mp4 ;
            OssService.appendLog("video_start|"+barename,false);
            Long last_upload_time=preferences.getLong("last_upload",0);
            Long cur_timestamp = System.currentTimeMillis()/1000;
            if ((cur_timestamp - last_upload_time)>3600*24){
                preferences.edit().putLong("last_upload",cur_timestamp).commit();
                String uuid=preferences.getString("uuid","");
                OssService.upload_file(getApplicationContext(), uuid);
            }
            t_v_speed=0;
            t_speed=0;
            t_imu_intense=0;
            stats_count=0;
            videoPlayer.setUp(full_path, null);
            show_loading_img(true);
            show_v_list(false);
            Long cur_play_pos = preferences.getLong("cur_v_pos",0);

            if (!first_play) {
                videoPlayer.playNew();
            }else{
                videoPlayer.start(cur_play_pos);
                first_play=false;
            }
        } catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    private void playMusic(String music_name) {
        Uri playerUri = Uri.parse(GlobalVariables.MUSIC+ music_name);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(MainActivity.this, playerUri);
        int music_on=preferences.getInt("music_on", 1);
        if (music_on == 1) {
            mediaPlayer.setVolume(1, 1);
            imageVoice.setBackgroundResource(R.mipmap.voice);
        } else {
            mediaPlayer.setVolume(0, 0);
            imageVoice.setBackgroundResource(R.mipmap.voice_un);
        }
        mediaPlayer.start();
        mediaPlayer.setLooping(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void buttonAnimation(List<ImageView> buttonList) {
        if (flag == -1) {
            for (int i = 0; i < buttonItems.size(); i++) {
                buttonItems.get(i).setVisibility(View.GONE);
                buttonItems.get(i).setTranslationY(0);
            }
            flag = 1;
            return;
        }
        for (int i = 0; i < buttonList.size(); i++) {
            ObjectAnimator objAnimatorRotate;
            buttonList.get(i).setVisibility(View.VISIBLE);
            objAnimatorRotate = ObjectAnimator.ofFloat(buttonList.get(i), "rotation", 0, 360);
            objAnimatorRotate.setDuration(200);
            objAnimatorRotate.start();
            flag = -1;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);

    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private void show_v_list(Boolean b_show){
        if (b_show){
            WindowManager wm = (WindowManager) this
                    .getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            int item_w_px=dp2px(getApplicationContext(), 148);
            int item_h_px=dp2px(getApplicationContext(), 200);
            HashMap<Integer, ArrayList<Integer>> item_sizes = new HashMap<>();
            ArrayList<Integer> s1=new ArrayList<>();
            s1.add(width/2-item_w_px/2);
            item_sizes.put(1,s1);
            ArrayList<Integer> s2=new ArrayList<>();
            s2.add(width/2-item_w_px);
            s2.add(width/2);
            item_sizes.put(2,s2);
            ArrayList<Integer> s3=new ArrayList<>();
            s3.add(width/2-item_w_px-item_w_px/2);
            s3.add(width/2-item_w_px/2);
            s3.add(width/2+item_w_px/2);
            item_sizes.put(3,s3);
            int cur_video_id = preferences.getInt("cur_video_id",1);
            LevelData data = OssService.levels.get(cur_video_id);
            ArrayList<LevelData> out_levs=new ArrayList<>();
            ArrayList<Integer> out_count=new ArrayList<>();
            HashMap<Integer,Integer> complete_levs=new HashMap<>();
            if (data.next_levs_id.size()>3){
                Map<String,?> keys = preferences.getAll();
                for (int i=0; i<data.next_levs_id.size(); i++){
                    Integer lev_id=data.next_levs_id.get(i);
                    Integer count= (Integer) keys.get(OssService.levels.get(lev_id).name +"_count");
                    Log.d("chamo1",OssService.levels.get(lev_id).name +"_count"+"   "+count);
                    if (count==null){
                        out_levs.add(OssService.levels.get(data.next_levs_id.get(i)));
                        out_count.add(-1);
                    }else{
                        complete_levs.put(data.next_levs_id.get(i), count);
                    }
                    if (out_levs.size()==3){
                        break;
                    }
                }
                Log.d("chamo1","complete_levs  "+complete_levs.size());
                if (out_levs.size()<3){
                    if (complete_levs.size()<=3-out_levs.size()){
                        for (HashMap.Entry<Integer,Integer> entry : complete_levs.entrySet()){
                            out_levs.add(OssService.levels.get(entry.getKey()));
                            out_count.add(entry.getValue());
                        }
                    }else{
                        List<HashMap.Entry<Integer, Integer>> list = new ArrayList<>(complete_levs.entrySet());
                        list.sort(HashMap.Entry.comparingByValue());
                        for (HashMap.Entry<Integer, Integer> entry : list) {
                            Log.d("chamo1","getKey  "+entry.getKey()+"  "+entry.getValue());
                            out_levs.add(OssService.levels.get(entry.getKey()));
                            out_count.add(entry.getValue());
                            if (out_levs.size()==3){
                                break;
                            }
                        }
                    }
                }
            }else{
                for (int i=0; i<data.next_levs_id.size(); i++){
                    LevelData data_n=OssService.levels.get(data.next_levs_id.get(i));
                    out_levs.add(data_n);
                    int lev_count = preferences.getInt(data_n.name+"_count",-1);
                    out_count.add(lev_count);
                }
            }
            Log.d("chamo1","out_levs.size(): "+out_levs.size());
            boolean all_unlock=true;
            for (int i=0; i<out_levs.size(); i++){
                RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.list_item, (ViewGroup) root_view, false);
                rl.setId(i+1);
                rl.setOnClickListener(this);
                ((ViewGroup) root_view).addView(rl);
                rl.setX(item_sizes.get(out_levs.size()).get(i));
                rl.setY(Math.round(height/2.5)-item_h_px/2);

                for(int index = 0; index < ((ViewGroup) rl).getChildCount(); index++) {
                    View nextChild = ((ViewGroup) rl).getChildAt(index);
                    if (nextChild.getId()==R.id.image){
                        ImageView img_view=(ImageView)nextChild;
                        String image_url=GlobalVariables.COVER+out_levs.get(i).cover;
                        Glide.with(getApplicationContext()).load(image_url).into(img_view);
                    }
                    if (nextChild.getId()==R.id.text){
                        TextView text_view=(TextView)nextChild;
                        String tmp_text="";
                        if (out_count.get(i)==-1){
                            tmp_text="未解锁";
                        }else{
                            tmp_text = "完成次数： "+out_count.get(i);
                            all_unlock=false;
                        }
                        text_view.setText(tmp_text);
                    }
                }
                level_views.add(rl);
                next_level_ids.put(rl.getId(), out_levs.get(i).id);
            }
            Log.d("chamo1","out_levs  "+out_levs.get(0).name);
            if (all_unlock){

                preferences.edit().putInt(out_levs.get(0).name+"_count",0).commit();
                RelativeLayout rl = (RelativeLayout)level_views.get(0);
                for(int index = 0; index < ((ViewGroup) rl).getChildCount(); index++) {
                    View nextChild = ((ViewGroup) rl).getChildAt(index);
                    if (nextChild.getId()==R.id.text){
                        TextView text_view=(TextView)nextChild;
                        String tmp_text = "完成次数： 0";
                        text_view.setText(tmp_text);
                    }
                }
            }
        }else{
            if (level_views.size()>0){
                for (int i=0; i<level_views.size(); i++){
                    ((ViewGroup) root_view).removeView(level_views.get(i));
                }
                level_views.clear();
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {

            switch (v.getId()) {
                case 1:
                case 2:
                case 3:
                    int chosed_lev=next_level_ids.get(v.getId());
                    LevelData data = OssService.levels.get(chosed_lev);
                    int count = preferences.getInt(data.name+"_count",-1);

                    if (count!=-1){
                        preferences.edit().putInt("cur_video_id", chosed_lev).commit();
                        preferences.edit().putLong("cur_v_pos", 0).commit();
                        show_v_list(false);
                        play();
                    }
                    break;
                case R.id.image_music:
                    buttonAnimation(buttonItems);
                    break;
                case R.id.image_voice:
                    int music_on = preferences.getInt("music_on", 1);
                    if (music_on == 1) {
                        mediaPlayer.setVolume(0, 0);
                        imageVoice.setBackgroundResource(R.mipmap.voice_un);
                        music_on = 0;
                    } else {
                        mediaPlayer.setVolume(1, 1);
                        music_on = 1;
                        imageVoice.setBackgroundResource(R.mipmap.voice);
                    }
                    preferences.edit().putInt("music_on", music_on).commit();
                    break;
                case R.id.image_next:
                    next();
                    break;
                case R.id.video_player:
                    if (imageVideo.getVisibility() != View.GONE) {
                        flag = -1;
                        buttonAnimation(buttonItems);
                        show_v_list(false);
                    }
                    break;
                case R.id.image_video:
                    if (level_views.size()==0) {
                        show_v_list(true);
                    } else {
                        show_v_list(false);
                    }
                    break;
                default:
                    break;
            }
        }catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (videoPlayer.getCurrentState()==ConstantKeys.CurrentState.STATE_PLAYING){
            }else{
                return;
            }
            time_count = time_count + 1;
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            float intense = (float) Math.sqrt(ax * ax + ay * ay + az * az);
            float f2 = 1 - f1;
            float aCur = intense * f1 + aLast * f2;
            float videoPlaySpd = aCur * coef_imu;

            aLast = aCur;
            if (time_count > 50) {
                stats_count = stats_count + 1;
                t_imu_intense = t_imu_intense + intense;
                t_speed = t_speed + videoPlaySpd;
                videoPlaySpd=3;
                if (videoPlaySpd < minSpeed) {
                    videoPlayer.pause();
                } else {
                    if (videoPlayer.isPaused()) {
                        videoPlayer.restart();
                        Log.d("chamo", "videoPlayer.start()");
                    }
                    if (videoPlaySpd > maxSpeed) {
                        videoPlaySpd = maxSpeed;
                        videoPlayer.setSpeed(videoPlaySpd);
                    } else {
                        videoPlayer.setSpeed(videoPlaySpd);
                    }
                }
                t_v_speed = t_v_speed + videoPlaySpd;
                String log = "speed=" + videoPlaySpd;
                Log.d("chamo", log);
                tvLog.setText(log);
                time_count = 0;
            }

            int offset = tvLog.getLineCount() * tvLog.getLineHeight();
            if (offset > tvLog.getHeight()) {
                tvLog.scrollTo(0, offset - tvLog.getHeight());
            }
        }catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
