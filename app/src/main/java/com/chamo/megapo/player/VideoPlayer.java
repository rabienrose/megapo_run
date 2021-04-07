package com.chamo.megapo.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.chamo.megapo.constant.ConstantKeys;
import com.chamo.megapo.listener.OnCompletedListener;
import com.chamo.megapo.listener.OnPlayOrPauseListener;
import com.chamo.megapo.listener.OnSurfaceListener;
import com.chamo.megapo.view.VideoTextureView;
import java.io.IOException;
import java.util.Map;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class VideoPlayer extends FrameLayout {
    private int mPlayerType = ConstantKeys.IjkPlayerType.TYPE_IJK;
    private int mCurrentState = ConstantKeys.CurrentState.STATE_IDLE;
    private Context mContext;
    private AudioManager mAudioManager;
    private IMediaPlayer mMediaPlayer;
    private FrameLayout mContainer;
    private VideoTextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private String mUrl;
    private Map<String, String> mHeaders;
    private int mBufferPercentage;
    private long skipToPosition;
    SharedPreferences preferences;
    private OnCompletedListener mOnCompletedListener;
    private OnPlayOrPauseListener mOnPlayOrPauseListener;

    public VideoPlayer(Context context) {
        this(context, null);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public void setOnPlayOrPauseListener(OnPlayOrPauseListener listener){
        this.mOnPlayOrPauseListener = listener;
    }

    public void setOnCompletedListener(OnCompletedListener listener){
        this.mOnCompletedListener = listener;
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
        preferences = mContext.getSharedPreferences("state", Context.MODE_PRIVATE);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    public void setUp(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    public void setSpeed(float speed) {
        ((IjkMediaPlayer) mMediaPlayer).setSpeed(speed);
    }

    public void start() {
        if (mCurrentState == ConstantKeys.CurrentState.STATE_IDLE) {
            initAudioManager();
            initMediaPlayer();
            initTextureView();
        }
    }

    public void start(long position) {
        if (position < 0) {
            return;
        }
        skipToPosition = position;
        start();
    }

    public void restart() {
        if (mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
        } else if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
        } else if (mCurrentState == ConstantKeys.CurrentState.STATE_COMPLETED ) {
            mMediaPlayer.reset();
            openMediaPlayer();
        } else if ( mCurrentState == ConstantKeys.CurrentState.STATE_ERROR) {
            mMediaPlayer.reset();
            openMediaPlayer();
        }else {
        }
    }

    public void pause() {
        if (mCurrentState == ConstantKeys.CurrentState.STATE_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = ConstantKeys.CurrentState.STATE_PAUSED;
        } else if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
        }
    }

    public void seekTo(long pos) {
        if (pos < 0) {
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    public void setVolume(int volume) {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }
    }

    public boolean isIdle() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_IDLE;
    }

    public boolean isPreparing() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PREPARING;
    }

    public boolean isPrepared() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PREPARED;
    }

    public boolean isBufferingPlaying() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
    }

    public boolean isBufferingPaused() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
    }

    public boolean isPlaying() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PLAYING;
    }

    public boolean isPaused() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED;
    }

    public boolean isError() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_ERROR;
    }

    public boolean isCompleted() {
        return mCurrentState == ConstantKeys.CurrentState.STATE_COMPLETED;
    }

    public int getMaxVolume() {
        if (mAudioManager != null) {
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    public int getVolume() {
        if (mAudioManager != null) {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    public long getDuration() {
        if (mMediaPlayer == null) {
            return 0;
        }
        updateDurationData(mMediaPlayer.getDuration());
        return mMediaPlayer.getDuration();
    }

    private void updateDurationData(long durationV) {
        if (mMediaPlayer.getCurrentPosition() != 0) {
//            int id = preferences.getInt("cur_video_ind", 0);
//            Log.d("chamo1", "getCurrentPosition: " + mMediaPlayer.getCurrentPosition()+"  "+id);
//            if (id == 0) {
//                numV = allData.get(0).getNum();
//            }else {
//                numV = allData.get(id - 1).getNum();
//            }
//            //位置除以时长乘100
//            double sum = mMediaPlayer.getCurrentPosition() / (double) durationV * 100;
//            b = new BigDecimal(sum);
//            double f1 = b.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
//            int endNum;
//            endNum = Integer.parseInt(new DecimalFormat("0").format(f1));
//            if (!Empty.isEmpty(numV) && !numV.equals("0")) {
//                endNum = endNum + Integer.parseInt(numV);
//            }
//            Media media = new Media();
//            media.setId(id);
//            media.setPercentage(endNum + "");
//            media.setPosition(mMediaPlayer.getCurrentPosition() + "");
//            skipToPosition=mMediaPlayer.getCurrentPosition();
//            dbOpenHelper.update(media);
        }
    }

    public long getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    public float getSpeed(float speed) {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            return ((IjkMediaPlayer) mMediaPlayer).getSpeed(speed);
        }
        return 0;
    }

    public long getTcpSpeed() {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            return ((IjkMediaPlayer) mMediaPlayer).getTcpSpeed();
        }
        return 0;
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            createIjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void createIjkMediaPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        int player = IjkMediaPlayer.OPT_CATEGORY_PLAYER;
        int codec = IjkMediaPlayer.OPT_CATEGORY_CODEC;
        int format = IjkMediaPlayer.OPT_CATEGORY_FORMAT;
        ((IjkMediaPlayer) mMediaPlayer).setOption(format, "analyzemaxduration", 100L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(format, "analyzeduration", 1L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(format, "probesize", 10240L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "soundtouch", 1);
        ((IjkMediaPlayer) mMediaPlayer).setOption(format, "flush_packets", 1L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "packet-buffering", 0L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "reconnect", 5);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "max-buffer-size", 10240L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "framedrop", 1L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "max-fps", 30L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "enable-accurate-seek", 1L);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "opensles", 0);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "framedrop", 1);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "start-on-prepared", 0);
        ((IjkMediaPlayer) mMediaPlayer).setOption(format, "http-detect-range-support", 0);
        ((IjkMediaPlayer) mMediaPlayer).setOption(codec, "skip_loop_filter", 48);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "mediacodec", 0);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "mediacodec-auto-rotate", 1);
        ((IjkMediaPlayer) mMediaPlayer).setOption(player, "mediacodec-handle-resolution-change", 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new VideoTextureView(mContext);
            mTextureView.setOnSurfaceListener(new OnSurfaceListener() {
                @Override
                public void onSurfaceAvailable(SurfaceTexture surface) {
                    if (mSurfaceTexture == null) {
                        mSurfaceTexture = surface;
                        openMediaPlayer();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mTextureView.setSurfaceTexture(mSurfaceTexture);
                        }
                    }
                }
                @Override
                public void onSurfaceSizeChanged(SurfaceTexture surface, int width, int height) {
                }
                @Override
                public boolean onSurfaceDestroyed(SurfaceTexture surface) {
                    return mSurfaceTexture == null;
                }
                @Override
                public void onSurfaceUpdated(SurfaceTexture surface) {
                }
            });
        }
        mTextureView.addTextureView(mContainer, mTextureView);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void openMediaPlayer() {
        mContainer.setKeepScreenOn(true);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        if (mUrl == null || mUrl.length() == 0) {
            Toast.makeText(mContext, "视频链接不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri path = Uri.parse(mUrl);
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), path, mHeaders);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = ConstantKeys.CurrentState.STATE_PREPARING;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            mCurrentState = ConstantKeys.CurrentState.STATE_PREPARED;
            mp.start();
            if (skipToPosition != 0) {
                mp.seekTo(skipToPosition);
            }
        }
    };

    public void playNew() {
        skipToPosition=0;
        mMediaPlayer.reset();
        openMediaPlayer();
    }

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            mCurrentState = ConstantKeys.CurrentState.STATE_COMPLETED;
            mContainer.setKeepScreenOn(false);
            mOnCompletedListener.onCompleted();
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            mBufferPercentage = percent;
            Log.d("chamo1","onBufferingUpdate: "+mBufferPercentage);
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
            Log.d("chamo1","onSeekComplete");
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            mTextureView.adaptVideoSize(width, height);
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            Log.e("chamo1","mOnErrorListener  "+what);
            // 直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错误
            if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
                mCurrentState = ConstantKeys.CurrentState.STATE_ERROR;
            }
            return true;
        }
    };

    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
                Log.d("chamo1","MEDIA_INFO_VIDEO_RENDERING_START");
                mOnPlayOrPauseListener.onPlayOrPauseClick(true);
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                if (mCurrentState == ConstantKeys.CurrentState.STATE_PAUSED || mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED;
                } else {
                    mCurrentState = ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING;
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PLAYING) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_PLAYING;
                    mOnPlayOrPauseListener.onPlayOrPauseClick(true);
                    Log.d("chamo1","STATE_BUFFERING_PLAYING");
                }
                if (mCurrentState == ConstantKeys.CurrentState.STATE_BUFFERING_PAUSED) {
                    mCurrentState = ConstantKeys.CurrentState.STATE_PAUSED;
                    mOnPlayOrPauseListener.onPlayOrPauseClick(false);
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                if (mTextureView != null) {
                    mTextureView.setRotation(extra);
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                Log.e("chamo1","MEDIA_INFO_NOT_SEEKABLE");
            } else {
            }
            return true;
        }
    };

    public void release() {
        releasePlayer();
        Runtime.getRuntime().gc();
    }

    public void releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mContainer != null) {
            mContainer.removeView(mTextureView);
        }
        if (mSurface != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mSurface.release();
            }
            mSurface = null;
        }
        if (mSurfaceTexture != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = null;
        }
        mCurrentState = ConstantKeys.CurrentState.STATE_IDLE;
    }
}
