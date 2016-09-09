package com.wbl.zxing.decodeImage;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.orhanobut.logger.Logger;
import com.wbl.zxing.MainActivity;
import com.wbl.zxing.R;
import com.wbl.zxing.decoding.CaptureActivityHandler;

import java.util.Vector;

/**
 * Created by djtao on 2016/9/6.
 */

public class MainActivityHandler extends Handler {

    private final MainActivity activity;
    private final DecodeImageThread decodeImageThread;
    private State state;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case R.id.decode_succeeded://解码成功
                state = State.SUCCESS;
                Logger.t("TAG").e("解码成功");
                Toast.makeText(activity,"解码成功",Toast.LENGTH_LONG).show();
                activity.handleDecode((Result) msg.obj);
                break;
            case R.id.decode_failed://解码失败
                Logger.t("TAG").e("解码失败");
                Toast.makeText(activity,"解码失败",Toast.LENGTH_LONG).show();
                //Toast.makeText(activity,msg.obj.toString(),Toast.LENGTH_LONG).show();
                break;
        }
    }

    private enum State{
        PREVIEW,
        SUCCESS,
        DONE
    }
    public MainActivityHandler(MainActivity activity, Vector<BarcodeFormat> decodeFormats,
                               String characterSet){
        this.activity = activity;
        //启动一个DecodeImageThread：用于解析二维码的子线程
        decodeImageThread=new DecodeImageThread(activity,decodeFormats,characterSet);
        decodeImageThread.start();
        state=State.SUCCESS;
        restartDecodeImage();
    }

    private void restartDecodeImage() {
        if(state==State.SUCCESS){
            state=State.PREVIEW;
            ImageDecodeManager.get().decodeImage(decodeImageThread.getHandler(),R.id.decode);
        }
    }

}
