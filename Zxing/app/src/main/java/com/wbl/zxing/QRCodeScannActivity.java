package com.wbl.zxing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.orhanobut.logger.Logger;
import com.wbl.zxing.camera.CameraManager;
import com.wbl.zxing.decoding.CaptureActivityHandler;
import com.wbl.zxing.decoding.InactivityTimer;
import com.wbl.zxing.permission.PermissionListener;
import com.wbl.zxing.permission.PermissionManager;
import com.wbl.zxing.view.ViewfinderView;


import java.io.IOException;
import java.util.Vector;

public class QRCodeScannActivity extends Activity implements Callback, View.OnClickListener{


    private static final int REQUEST_CODE_CAMERA = 1;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    PermissionManager helper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scann);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);

        com.wbl.zxing.camera.CameraManager.init(getApplication());
        viewfinderView = (com.wbl.zxing.view.ViewfinderView) findViewById(R.id.viewfinder_view);

        hasSurface = false;
        //创建一个Timer：如果设备使用电池供电，一段时间不活动之后结束activity
        inactivityTimer = new com.wbl.zxing.decoding.InactivityTimer(this);

        helper = PermissionManager.with(this)
                //添加权限请求码
                .addRequestCode(QRCodeScannActivity.REQUEST_CODE_CAMERA)
                //设置权限，可以添加多个权限
                .permissions(Manifest.permission.CAMERA)
                //设置权限监听器
                .setPermissionsListener(new PermissionListener() {

                    @Override
                    public void onGranted() {
                        //当权限被授予时调用
//                        Toast.makeText(QRCodeScannActivity.this, "Camera Permission granted",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDenied() {
                        //用户拒绝该权限时调用
                        Logger.t("TAG").e("拍照权限被拒绝");
                    }

                    @Override
                    public void onShowRationale(String[] permissions) {
                        //当用户拒绝某权限时并点击`不再提醒`的按钮时，下次应用再请求该权限时，需要给出合适的响应（比如,给个展示对话框来解释应用为什么需要该权限）
                        Snackbar.make(viewfinderView, "需要相机权限才能扫描二维码", Snackbar.LENGTH_INDEFINITE)
                                .setAction("获取权限", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //必须调用该`setIsPositive(true)`方法
                                        helper.setIsPositive(true);
                                        helper.request();
                                    }
                                }).show();
                    }
                })
                //请求权限
                .request();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_back:
                this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化SurfaceView
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        //主要为了停止消息的派发
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     * @param result
     */
    public void handleDecode(Result result) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        onResultHandler(resultString);
    }

    /**
     * 跳转到上一个页面
     * @param resultString
     */
    private void onResultHandler(String resultString){
        if(TextUtils.isEmpty(resultString)){
            Toast.makeText(QRCodeScannActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this,resultString,Toast.LENGTH_LONG).show();
        QRCodeScannActivity.this.finish();
        //Intent resultIntent = new Intent();
        //Bundle bundle = new Bundle();
        //bundle.putString("result", resultString);
        //bundle.putParcelable("bitmap", bitmap);
        //resultIntent.putExtras(bundle);
        //this.setResult(100, resultIntent);
        //QRCodeScannActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
}
