package com.abcs.sociax.t4.android.video;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abcs.sociax.t4.android.weibo.ActivityCreateBase;
import com.yixia.camera.MediaRecorderBase;
import com.yixia.camera.MediaRecorderBase.OnErrorListener;
import com.yixia.camera.MediaRecorderBase.OnPreparedListener;
import com.yixia.camera.MediaRecorderNative;
import com.yixia.camera.MediaRecorderSystem;
import com.yixia.camera.VCamera;
import com.yixia.camera.model.MediaObject;
import com.yixia.camera.util.DeviceUtils;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;
import com.yixia.videoeditor.adapter.UtilityAdapter;
import com.abcs.sociax.android.R;
import com.abcs.sociax.t4.unit.UnitSociax;

public class MediaRecorderActivity extends BaseActivity implements
        OnErrorListener, OnClickListener, OnPreparedListener,
        MediaRecorderBase.OnEncodeListener {
    public static final String action_video_ok = "action_video_ok";
    // 导入视频
    public final static int REQUEST_CODE_IMPORT_VIDEO = 998;
    /**
     * 导入视频截取
     */
    public final static int REQUEST_CODE_IMPORT_VIDEO_EDIT = 997;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(action_video_ok)) {
                if (mMediaObject != null) {
                    mMediaObject.delete();
                }

                finish();
            }
        }
    };
    /**
     * 录制最长时间
     */
    public final static int RECORD_TIME_MAX = 10 * 1000;  //10s
    /**
     * 录制最小时间
     */
    public final static int RECORD_TIME_MIN = 3 * 1000;
    /**
     * 刷新进度条
     */
    private static final int HANDLE_INVALIDATE_PROGRESS = 0;
    /**
     * 延迟拍摄停止
     */
    private static final int HANDLE_STOP_RECORD = 1;
    /**
     * 对焦
     */
    private static final int HANDLE_HIDE_RECORD_FOCUS = 2;

    /**
     * 下一步
     */
    private CheckedTextView mTitleNext;
    /**
     * 对焦图标-带动画效果
     */
    private ImageView mFocusImage;
    /**
     * 前后摄像头切换
     */
    private CheckBox mCameraSwitch;
    /**
     * 回删按钮、延时按钮、滤镜按钮
     */
    private CheckedTextView mRecordDelete;
    /**
     * 闪光灯
     */
    private CheckBox mRecordLed;
    /**
     * 拍摄按钮
     */
    private TextView mRecordController;

    /**
     * 底部条
     */
    private RelativeLayout mBottomLayout;
    //取景框
    private RelativeLayout mCameraLayout;
    /**
     * 摄像头数据显示画布
     */
    private SurfaceView mSurfaceView;
    /**
     * 录制进度
     */
    private ProgressView mProgressView;
    /**
     * 对焦动画
     */
    private Animation mFocusAnimation;

    /**
     * SDK视频录制对象
     */
    private MediaRecorderBase mMediaRecorder;
    /**
     * 视频信息
     */
    private MediaObject mMediaObject;

    /**
     * 需要重新编译（拍摄新的或者回删）
     */
    private boolean mRebuild;
    /**
     * on
     */
    private boolean mCreated;
    /**
     * 是否是点击状态
     */
    private volatile boolean mPressedStatus;
    /**
     * 是否已经释放
     */
    private volatile boolean mReleased;
    /**
     * 对焦图片宽度
     */
    private int mFocusWidth;
    /**
     * 底部背景色
     */
    private int mBackgroundColorNormal, mBackgroundColorPress;
    /**
     * 屏幕宽度
     */
    private int mWindowWidth;

    private static MediaRecorderActivity instance;

    public static MediaRecorderActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCreated = false;
        super.onCreate(savedInstanceState);
        instance = this;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
        loadIntent();
        loadViews();
        mCreated = true;
        // 注册监听
        registerReceiver(receiver, new IntentFilter(action_video_ok));
    }

    protected void onDestroy() {
        // 注销监听
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    ;

    /**
     * 加载传入的参数
     */
    private void loadIntent() {
        mWindowWidth = UnitSociax.getWindowWidth(this);
        mFocusWidth = UnitSociax.dip2px(this, 64);
        mBackgroundColorNormal = getResources().getColor(R.color.black);// camera_bottom_bg
        mBackgroundColorPress = getResources().getColor(
                R.color.camera_bottom_press_bg);
    }

    /**
     * 加载视图
     */
    private void loadViews() {
        setContentView(R.layout.activity_media_recorder);
        // ~~~ 绑定控件
        mSurfaceView = (SurfaceView) findViewById(R.id.record_preview);
        mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
        mTitleNext = (CheckedTextView) findViewById(R.id.title_next);
        mFocusImage = (ImageView) findViewById(R.id.record_focusing);
        mProgressView = (ProgressView) findViewById(R.id.record_progress);
        mRecordDelete = (CheckedTextView) findViewById(R.id.record_delete);
        mRecordController = (TextView) findViewById(R.id.record_controller);
        mCameraLayout = (RelativeLayout) findViewById(R.id.camera_layout);
        //设置取景框宽度和高度
        int width = com.thinksns.sociax.thinksnsbase.utils.UnitSociax.getWindowWidth(this);
        int height = (width * 3) / 4;    //宽：高=4：3；
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        mCameraLayout.setLayoutParams(params);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRecordLed = (CheckBox) findViewById(R.id.record_camera_led);
        // ~~~ 绑定事件
        if (DeviceUtils.hasICS())
            mSurfaceView.setOnTouchListener(mOnSurfaveViewTouchListener);

        mTitleNext.setOnClickListener(this);
        findViewById(R.id.record_import).setOnClickListener(this);
        findViewById(R.id.title_back).setOnClickListener(this);
        mRecordDelete.setOnClickListener(this);
        mRecordController.setOnTouchListener(mOnVideoControllerTouchListener);

        // ~~~ 设置数据
        // 是否支持前置摄像头
        if (MediaRecorderBase.isSupportFrontCamera()) {
            mCameraSwitch.setOnClickListener(this);
        } else {
            mCameraSwitch.setVisibility(View.GONE);
        }
        // 是否支持闪光灯
        if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
            mRecordLed.setOnClickListener(this);
        } else {
            mRecordLed.setVisibility(View.GONE);
        }

        try {
            mFocusImage.setImageResource(R.drawable.video_focus);
        } catch (OutOfMemoryError e) {
            Logger.e(e);
        }

        mProgressView.setMaxDuration(RECORD_TIME_MAX);
        initSurfaceView();
    }

    /**
     * 初始化画布
     */
    private void initSurfaceView() {
        final int w = DeviceUtils.getScreenWidth(this);
//		((LinearLayout.LayoutParams) mBottomLayout.getLayoutParams()).topMargin = w;
        //4:3
//		int width = w;
//		int height = w * 3 / 4;

//		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
//		lp.width = width;
//		lp.height = height;
//		mSurfaceView.setLayoutParams(lp);
    }

    /**
     * 初始化拍摄SDK
     */
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorderNative();
        mRebuild = true;

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnEncodeListener(this);
        File f = new File(VCamera.getVideoCachePath());
        if (!FileUtils.checkFile(f)) {
            f.mkdirs();
        }
        String key = String.valueOf(System.currentTimeMillis());
        mMediaObject = mMediaRecorder.setOutputDirectory(key,
                VCamera.getVideoCachePath() + key);
        mMediaRecorder.setSurfaceHolder(mSurfaceView.getHolder());
        mMediaRecorder.prepare();
    }

    /**
     * 点击屏幕录制
     */
    private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaRecorder == null || !mCreated) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 检测是否手动对焦
                    if (checkCameraFocus(event))
                        return true;
                    break;
            }
            return true;
        }

    };

    /**
     * 点击屏幕录制
     */
    private View.OnTouchListener mOnVideoControllerTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaRecorder == null) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 检测是否手动对焦
                    // 判断是否已经超时
                    if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                        return true;
                    }

                    // 取消回删
                    if (cancelDelete())
                        return true;

                    startRecord();

                    break;

                case MotionEvent.ACTION_UP:
                    // 暂停
                    if (mPressedStatus) {
                        stopRecord();

                        // 检测是否已经完成
                        if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                            mTitleNext.performClick();
                        }
                    }
                    break;
            }
            return true;
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        UtilityAdapter.freeFilterParser();
        UtilityAdapter.initFilterParser();
        if (mMediaRecorder == null) {
            initMediaRecorder();
        } else {
            mRecordLed.setChecked(false);
            mMediaRecorder.prepare();
            mProgressView.setData(mMediaObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecord();
        UtilityAdapter.freeFilterParser();
        if (!mReleased) {
            if (mMediaRecorder != null)
                mMediaRecorder.release();
        }
        mReleased = false;
    }

    /**
     * 手动对焦
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean checkCameraFocus(MotionEvent event) {
        mFocusImage.setVisibility(View.GONE);
        float x = event.getX();
        float y = event.getY();
        float touchMajor = event.getTouchMajor();
        float touchMinor = event.getTouchMinor();

        Rect touchRect = new Rect((int) (x - touchMajor / 2),
                (int) (y - touchMinor / 2), (int) (x + touchMajor / 2),
                (int) (y + touchMinor / 2));
        // The direction is relative to the sensor orientation, that is, what
        // the sensor sees. The direction is not affected by the rotation or
        // mirroring of setDisplayOrientation(int). Coordinates of the rectangle
        // range from -1000 to 1000. (-1000, -1000) is the upper left point.
        // (1000, 1000) is the lower right point. The width and height of focus
        // areas cannot be 0 or negative.
        // No matter what the zoom level is, (-1000,-1000) represents the top of
        // the currently visible camera frame
        if (touchRect.right > 1000)
            touchRect.right = 1000;
        if (touchRect.bottom > 1000)
            touchRect.bottom = 1000;
        if (touchRect.left < 0)
            touchRect.left = 0;
        if (touchRect.right < 0)
            touchRect.right = 0;

        if (touchRect.left >= touchRect.right
                || touchRect.top >= touchRect.bottom)
            return false;

        ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(new Camera.Area(touchRect, 1000));
        if (!mMediaRecorder.manualFocus(new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // if (success) {
                mFocusImage.setVisibility(View.GONE);
                // }
            }
        }, focusAreas)) {
            mFocusImage.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage
                .getLayoutParams();
        int left = touchRect.left - (mFocusWidth / 2);// (int) x -
        // (focusingImage.getWidth()
        // / 2);
        int top = touchRect.top - (mFocusWidth / 2);// (int) y -
        // (focusingImage.getHeight()
        // / 2);
        if (left < 0)
            left = 0;
        else if (left + mFocusWidth > mWindowWidth)
            left = mWindowWidth - mFocusWidth;
        if (top + mFocusWidth > mWindowWidth)
            top = mWindowWidth - mFocusWidth;

        lp.leftMargin = left;
        lp.topMargin = top;
        mFocusImage.setLayoutParams(lp);
        mFocusImage.setVisibility(View.VISIBLE);

        if (mFocusAnimation == null)
            mFocusAnimation = AnimationUtils.loadAnimation(this,
                    R.anim.record_focus);

        mFocusImage.startAnimation(mFocusAnimation);

        mHandler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 3500);// 最多3.5秒也要消失
        return true;
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (mMediaRecorder != null) {
            MediaObject.MediaPart part = mMediaRecorder.startRecord();
            if (part == null) {
                return;
            }

            // 如果使用MediaRecorderSystem，不能在中途切换前后摄像头，否则有问题
            if (mMediaRecorder instanceof MediaRecorderSystem) {
                mCameraSwitch.setVisibility(View.GONE);
            }
            mProgressView.setData(mMediaObject);
        }

        mRebuild = true;
        mPressedStatus = true;
        mRecordController.setBackgroundResource(R.drawable.ic_paishe);
        if (mHandler != null) {
            mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
            mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);

            mHandler.removeMessages(HANDLE_STOP_RECORD);
            mHandler.sendEmptyMessageDelayed(HANDLE_STOP_RECORD,
                    RECORD_TIME_MAX - mMediaObject.getDuration());
        }

        mTitleNext.setVisibility(View.GONE);
        mRecordDelete.setVisibility(View.GONE);
        mCameraSwitch.setEnabled(false);
        mRecordLed.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (mRecordDelete != null && mRecordDelete.isChecked()) {
            cancelDelete();
            return;
        }

        if (mMediaObject != null && mMediaObject.getDuration() > 1) {
            // 未转码
            new AlertDialog.Builder(this)
                    .setTitle(R.string.hint)
                    .setMessage(R.string.record_camera_exit_dialog_message)
                    .setNegativeButton(
                            R.string.record_camera_cancel_dialog_yes,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    mMediaObject.delete();
                                    finish();
                                }

                            })
                    .setPositiveButton(R.string.record_camera_cancel_dialog_no,
                            null).setCancelable(false).show();
            return;
        }

        if (mMediaObject != null)
            mMediaObject.delete();
        finish();
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        mPressedStatus = false;
        mRecordController.setBackgroundResource(R.drawable.ic_paishe);
//		mBottomLayout.setBackgroundColor(mBackgroundColorNormal);
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
        mTitleNext.setVisibility(View.VISIBLE);
        mRecordDelete.setVisibility(View.VISIBLE);
        mCameraSwitch.setEnabled(true);
        mRecordLed.setEnabled(true);
        mHandler.removeMessages(HANDLE_STOP_RECORD);
        checkStatus();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
            mHandler.removeMessages(HANDLE_STOP_RECORD);
        }
        // 处理开启回删后其他点击操作
        if (id != R.id.record_delete) {
            if (mMediaObject != null) {
                MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                if (part != null) {
                    if (part.remove) {
                        part.remove = false;
                        mRecordDelete.setChecked(false);
                        if (mProgressView != null)
                            mProgressView.invalidate();
                    }
                }
            }
        }

        switch (id) {
            case R.id.record_import:// 导入视频
                // showImportMenu();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.record_camera_import_video_choose)),
                        REQUEST_CODE_IMPORT_VIDEO);
                break;
            case R.id.title_back:
                onBackPressed();
                break;
            case R.id.record_camera_switcher:// 前后摄像头切换
                if (mRecordLed.isChecked()) {
                    if (mMediaRecorder != null) {
                        mMediaRecorder.toggleFlashMode();
                    }
                    mRecordLed.setChecked(false);
                }

                if (mMediaRecorder != null) {
                    mMediaRecorder.switchCamera();
                }

                if (mMediaRecorder.isFrontCamera()) {
                    mRecordLed.setEnabled(false);
                } else {
                    mRecordLed.setEnabled(true);
                }
                break;
            case R.id.record_camera_led:// 闪光灯
                // 开启前置摄像头以后不支持开启闪光灯
                if (mMediaRecorder != null) {
                    if (mMediaRecorder.isFrontCamera()) {
                        return;
                    }
                }

                if (mMediaRecorder != null) {
                    mMediaRecorder.toggleFlashMode();
                }
                break;
            case R.id.title_next:// 停止录制
                //开始转码
                mMediaRecorder.startEncoding();
                break;
            case R.id.record_delete:

                if (mMediaObject != null) {
                    MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                    if (part != null) {
                        if (part.remove) {
                            mRebuild = true;
                            part.remove = false;
                            mMediaObject.removePart(part, true);
                            mRecordDelete.setChecked(false);
                        } else {
                            part.remove = true;
                            mRecordDelete.setChecked(true);
                        }
                    }
                    if (mProgressView != null)
                        mProgressView.invalidate();

                    // 检测按钮状态
                    checkStatus();
                }
                break;
        }
    }

    /**
     * 取消回删
     */
    private boolean cancelDelete() {
        if (mMediaObject != null) {
            MediaObject.MediaPart part = mMediaObject.getCurrentPart();
            if (part != null && part.remove) {
                part.remove = false;
                mRecordDelete.setChecked(false);
                if (mProgressView != null)
                    mProgressView.invalidate();
                return true;
            }
        }
        return false;
    }

    /**
     * 检查录制时间，显示/隐藏下一步按钮
     */
    private int checkStatus() {
        int duration = 0;
        if (!isFinishing() && mMediaObject != null) {
            duration = mMediaObject.getDuration();
            if (duration < RECORD_TIME_MIN) {
                if (duration == 0) {
                    mCameraSwitch.setVisibility(View.VISIBLE);
                    mRecordDelete.setVisibility(View.GONE);
                    mTitleNext.setVisibility(View.GONE);
                }
                // 视频必须大于3秒
                mTitleNext.setEnabled(false);
                mTitleNext.setChecked(false);
            } else {
                // 下一步
                if (mTitleNext.getVisibility() != View.VISIBLE) {
                    mTitleNext.setVisibility(View.VISIBLE);
                }
                mTitleNext.setEnabled(true);
                mTitleNext.setChecked(true);
                mRecordDelete.setVisibility(View.VISIBLE);
            }
        }
        return duration;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_INVALIDATE_PROGRESS:
                    if (mMediaRecorder != null && !isFinishing()) {
                        if (mProgressView != null)
                            mProgressView.invalidate();
                        if (mMediaObject.getDuration() >= RECORD_TIME_MAX) {
                            stopRecord();
                        }
                        if (mPressedStatus)
                            sendEmptyMessageDelayed(0, 30);
                    }
                    break;
            }
        }
    };

    @Override
    public void onEncodeStart() {
        showProgress("", getString(R.string.record_camera_progress_message));
    }

    @Override
    public void onEncodeProgress(int progress) {
        Logger.e("[MediaRecorderActivity]onEncodeProgress..." + progress);
    }

    /**
     * 转码完成
     */
    @Override
    public void onEncodeComplete() {
        hideProgress();
        //跳转至分享编辑页
        ActivityCreateBase.staticVideoPath = mMediaObject.getOutputTempVideoPath();
        Intent intent = new Intent(this, ActivityCreateBase.class);
//        intent.putExtra("type", AppConstant.CREATE_VIDEO_WEIBO);
//        intent.putExtra("video_path", mMediaObject.getOutputTempVideoPath());
        setResult(RESULT_OK, intent);
//        startActivity(intent);
        mRebuild = false;
        finish();
    }

    /**
     * 转码失败 检查sdcard是否可用，检查分块是否存在
     */
    @Override
    public void onEncodeError() {
        hideProgress();
        Toast.makeText(this, R.string.record_video_transcoding_faild,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVideoError(int what, int extra) {

    }

    @Override
    public void onAudioError(int what, String message) {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMPORT_VIDEO:
                    // 视频导入
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            String columnName = MediaStore.Video.Media.DATA;
                            if (StringUtils.isNotEmpty(columnName)) {
                                Cursor cursor = getContentResolver().query(uri,
                                        new String[]{columnName}, null, null,
                                        null);
                                if (cursor != null) {
                                    String path = "";
                                    if (cursor.moveToNext()) {
                                        path = cursor.getString(0);
                                    }
                                    cursor.close();
                                    Logger.d("path:", path);
                                    if (saveMediaObject(mMediaObject)) {
                                        //修改
//									startActivityForResult(
//											new Intent(this, ImportVideoActivity.class)
//													.putExtra(
//															"obj",
//															mMediaObject
//																	.getObjectFilePath())
//													.putExtra("path", path),
//											REQUEST_CODE_IMPORT_VIDEO_EDIT);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case REQUEST_CODE_IMPORT_VIDEO_EDIT:
                    /** 导入视频截取 */
                    mMediaObject = restoneMediaObject(mMediaObject
                            .getObjectFilePath());
                    mProgressView.setData(mMediaObject);
                    mMediaRecorder.startEncoding();
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
