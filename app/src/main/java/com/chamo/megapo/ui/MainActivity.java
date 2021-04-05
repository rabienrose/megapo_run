package com.chamo.megapo.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.chamo.megapo.R;
import org.yczbj.ycvideoplayerlib.db.DBOpenHelper;
import org.yczbj.ycvideoplayerlib.model.Music;
import org.yczbj.ycvideoplayerlib.model.Media;
import com.chamo.megapo.ui.base.ManageFragmentActivity;
import com.chamo.megapo.utils.OssService;
import com.chamo.megapo.utils.GlobalVariables;
import com.chamo.megapo.utils.PopupWindowRight;
import com.chamo.megapo.utils.HandlerUtils;
import com.chamo.megapo.utils.GlobalFunction;
import com.chamo.megapo.utils.ScreenManager;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.x;
import org.yczbj.ycvideoplayerlib.controller.VideoPlayerController;
import org.yczbj.ycvideoplayerlib.inter.listener.OnCompletedListener;
import org.yczbj.ycvideoplayerlib.inter.listener.OnPlayOrPauseListener;
import org.yczbj.ycvideoplayerlib.manager.VideoPlayerManager;
import org.yczbj.ycvideoplayerlib.player.VideoPlayer;
import org.yczbj.ycvideoplayerlib.utils.Empty;
import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_main)
public class MainActivity extends ManageFragmentActivity implements View.OnClickListener, SensorEventListener {
    private static final String TAG = MainActivity.class.getClass().getSimpleName();
    private long exitTime;
    private MediaPlayer mediaPlayer = null;
    private VideoPlayer videoPlayer = null;
    private int musicIndex = 0;
    private boolean isFirst = true;
    private VideoPlayerController controller;
    public SensorManager sm;
    private Sensor acceleromererSensor;
    private float aLast = 0.0f;
    private EditText et1;
    private EditText et2;
    private TextView tvLog;
    private int time_count = 0;
    private float coef = 0.09f;
    private float f1 = 0.01f;
    private float minSpeed = 0.5f;
    private float maxSpeed = 2f;
    private ImageView imageMusic;
    private ImageView imageVideo;
    private ImageView imageVoice;
    private ImageView imageNext;
    private List<ImageView> buttonItems = new ArrayList<ImageView>(3);
    // 标识当前按钮弹出与否，1代表已经未弹出，-1代表已弹出
    private int flag = 1;
    private List<Media> mediaList = new ArrayList<>();
    private List<Music> musicList = new ArrayList<>();
    private PopupWindowRight popupWindowRight;
    private boolean popuTag = false;
    private DBOpenHelper dbOpenHelper;
    private HandlerUtils handlerUtils;
    private SharedPreferences preferences;
    private float t_imu_intense;
    private float t_speed;
    private float t_v_speed;
    private int stats_count=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            preferences = getSharedPreferences("state", MODE_PRIVATE);
            x.view().inject(this);
            initView();
            initMusic();
            initData();
            initListenter();
        }catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    private void initMusic() {
        musicIndex = preferences.getInt("cur_music_ind",0);
    }

    public MainActivity() {
        popupWindowRight.mMainActivity = this;
    }

    private void initData() {
        dbOpenHelper = new DBOpenHelper(this);
        handlerUtils = new HandlerUtils(this);
        popupWindowRight = new PopupWindowRight(MainActivity.this);
        buttonItems.add(imageVoice);
        buttonItems.add(imageNext);
        controller = new VideoPlayerController(this);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acceleromererSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(this, acceleromererSensor, SensorManager.SENSOR_DELAY_NORMAL);
        videoPlayer.setController(controller);
        controller.hideFullScreenButton();
        if (dbOpenHelper.getAllData().size() == 0) {
            getData();
        } else {
            int videoIndex = preferences.getInt("cur_video_ind",0);
            ArrayList<Media> allData = dbOpenHelper.getAllData();
            if (videoIndex == 0) {
                play(allData.get(0), 0);
            }else {
                play(allData.get(videoIndex - 1), 0);
            }
            mediaList.addAll(allData);
        }
        getDataMusic();
    }

    private void initListenter() {
        imageMusic.setOnClickListener(this);
        imageVoice.setOnClickListener(this);
        imageNext.setOnClickListener(this);
        imageVideo.setOnClickListener(this);
        videoPlayer.setOnClickListener(this);
        controller.setOnCompletedListener(new OnCompletedListener() {
            @Override
            public void onCompleted() {
                try {
                    Media media_next = new Media();
                    media_next.setUnlock("1");
                    int cur_video_ind = preferences.getInt("cur_video_ind", 0);
                    media_next.setId(cur_video_ind + 1);
                    dbOpenHelper.update(media_next);
                    ArrayList<Media> allData = dbOpenHelper.getAllData();
                    Media media_past = allData.get(cur_video_ind - 1);
                    media_past.setNum(String.valueOf(Integer.parseInt(media_past.getNum()) + 100));
                    media_past.setPosition("1");
                    media_past.setId(cur_video_ind);
                    dbOpenHelper.update(media_past);
                    ArrayList<Media> allData1 = dbOpenHelper.getAllData();
                    allData1.get(0).getNum();
                    mediaList.clear();
                    mediaList.addAll(allData);
                    String end_str = "done_video|";
                    end_str = end_str + media_past.getBareName() + "|";
                    end_str = end_str + t_imu_intense / stats_count + "|";
                    end_str = end_str + t_speed / stats_count + "|";
                    end_str = end_str + t_v_speed / stats_count + "|";
                    OssService.appendLog(end_str, false);
                    show_v_list(true);
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }
        });
        controller.setOnPlayOrPauseListener(new OnPlayOrPauseListener() {
            @Override
            public void onPlayOrPauseClick(boolean isPlaying) {
                try {
                    Log.d("chamo1","setOnPlayOrPauseListener");
                    imageMusic.setVisibility(View.VISIBLE);
                    imageVideo.setVisibility(View.VISIBLE);
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }
        });
        controller.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Event(0);
                    popuTag = false;
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
                return false;
            }
        });
    }

    protected void initView() {
        et1 = (EditText) findViewById(R.id.et_spd);
        et1.setText(Float.toString(coef));
        et2 = (EditText) findViewById(R.id.et_delay);
        et2.setText(Float.toString(f1));
        tvLog = (TextView) findViewById(R.id.tv_log);
        videoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        imageMusic = (ImageView) findViewById(R.id.image_music);
        imageVoice = (ImageView) findViewById(R.id.image_voice);
        imageNext = (ImageView) findViewById(R.id.image_next);
        imageVideo = (ImageView) findViewById(R.id.image_video);
    }

    private void next() {
        musicIndex++;
        musicIndex = musicIndex % musicList.size();
        Music music = musicList.get(musicIndex);
        playMusic(music);
    }

    private void getData() {
        RequestParams params = new RequestParams(GlobalVariables.VIDEOURl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String[] ary = result.split("\n");
                    for (String item : ary) {
                        String[] ay = item.split(",");
                        Media media = new Media();
                        media.setVideoName(GlobalVariables.VIDEO + ay[0]);
                        media.setVideoImage(GlobalVariables.COVER + ay[1]);
                        media.setUnlock("0");
                        media.setPercentage("0");
                        media.setNum("0");
                        mediaList.add(media);
                    }
                    for (int i = 0; i < mediaList.size(); i++) {
                        if (i == 0) {
                            mediaList.get(i).setUnlock("1");
                        }
                        dbOpenHelper.add(mediaList.get(i));
                    }
                    handler.sendEmptyMessage(0);
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                OssService.appendLog("fetch_video_list_err", false);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                OssService.appendLog("fetch_video_list_cancel", false);
            }

            @Override
            public void onFinished() {
                OssService.appendLog("fetch_video_list_done", false);
            }
        });
    }

    private void getDataMusic() {
        RequestParams params = new RequestParams(com.chamo.megapo.utils.GlobalVariables.MUSICURl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String[] ary = result.split("\n");
                    for (String item : ary) {
                        Music media = new Music();
                        media.setMusic(com.chamo.megapo.utils.GlobalVariables.MUSIC + item);
                        musicList.add(media);
                    }
                    handler.sendEmptyMessage(2);
                }catch (Exception e) {
                    OssService.appendErr(e);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                OssService.appendLog("fetch_music_list_err", false);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                OssService.appendLog("fetch_music_list_cancel", false);
            }

            @Override
            public void onFinished() {
                OssService.appendLog("fetch_music_list_done", false);
            }
        });
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    play(dbOpenHelper.getAllData().get(0), 0);
                    break;
                case 1:
                    GlobalFunction.showToast(MainActivity.this, msg.obj.toString());
                    break;
                case 2:
                    Music music = musicList.get(musicIndex);
                    playMusic(music);
                    break;
                default:
                    break;
            }
        }
    };

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

    public void play(Media media, int i) {
        try {
            String barename = media.getBareName();
            String full_path=media.getVideoName();
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
            imageMusic.setVisibility(View.GONE);
            imageVideo.setVisibility(View.GONE);
            show_v_list(false);

            if (!Empty.isEmpty(media.getPosition())) {
                videoPlayer.start(Long.parseLong(media.getPosition()));
            } else {
                videoPlayer.start(0);
            }
            preferences.edit().putInt("cur_video_ind",media.getId()).commit();
            if (i == 1) {
                videoPlayer.playNew();
            }
        } catch (Exception e) {
            OssService.appendErr(e);
        }
    }

    private void playMusic(Music media) {
        preferences.edit().putInt("cur_music_ind",musicIndex).commit();
        Uri playerUri = Uri.parse(media.getMusic());
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(MainActivity.this, playerUri);
        Log.e(TAG, "playMusic: " + playerUri);
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
        VideoPlayerManager.instance().releaseVideoPlayer();
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
        VideoPlayerManager.instance().suspendVideoPlayer();
    }

    @Override
    public void onBackPressed() {
        if (VideoPlayerManager.instance().onBackPressed()) {
            return;
        } else {
            VideoPlayerManager.instance().releaseVideoPlayer();
        }
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        VideoPlayerManager.instance().resumeVideoPlayer();
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
            ObjectAnimator objAnimatorY;
            ObjectAnimator objAnimatorRotate;
            buttonList.get(i).setVisibility(View.VISIBLE);
            objAnimatorY = ObjectAnimator.ofFloat(buttonList.get(i), "y", buttonList.get(i).getY() + 0, buttonList.get(i).getY() + 10);
            objAnimatorY.setDuration(200);
            objAnimatorY.setStartDelay(100);
            objAnimatorY.start();
            objAnimatorRotate = ObjectAnimator.ofFloat(buttonList.get(i), "rotation", 0, 360);
            objAnimatorRotate.setDuration(200);
            objAnimatorY.setStartDelay(100);
            objAnimatorRotate.start();
            flag = -1;
        }

    }

    public void Event(int messageEvent) {
        flag = -1;
        buttonAnimation(buttonItems);
        popupWindowRight.dismiss();
        popuTag = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);

    }

    private void show_v_list(Boolean b_show){
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        if (b_show) {
            ArrayList<Media> allData = dbOpenHelper.getAllData();
            popupWindowRight.setData(allData, MainActivity.this, preferences.getInt("cur_video_ind", 0) - 2);
            popupWindowRight.showAtLocation(getWindow().getDecorView(), Gravity.RIGHT | Gravity.CENTER, width / 4, 0);
            popuTag = true;
        } else {
//                    handler1 = handlerUtils.startHandlerTiming(2);
            popupWindowRight.dismiss();
            popuTag = false;
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.image_music:
                    popuTag = false;
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
                    Event(0);
                    break;
                case R.id.image_video:
                    if (!popuTag) {
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
            time_count = time_count + 1;
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            float intense = (float) Math.sqrt(ax * ax + ay * ay + az * az);
            float f2 = 1 - f1;
            float aCur = intense * f1 + aLast * f2;
            float videoPlaySpd = aCur * coef;

            aLast = aCur;
            if (time_count > 50) {
                stats_count = stats_count + 1;
                t_imu_intense = t_imu_intense + intense;
                t_speed = t_speed + videoPlaySpd;
//            String s1 = et1.getText().toString();
//            if (!TextUtils.isEmpty(s1)) {
//                try {
//                    coef = Float.parseFloat(s1);
//                } catch (Exception ex) {
//
//                }
//            }
//            String s2 = et2.getText().toString();
//            if (!TextUtils.isEmpty(s1)) {
//                try {
//                    f1 = Float.parseFloat(s2);
//                } catch (Exception ex) {
//
//                }
//            }
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
//                save_video_pos();
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
