package com.wbl.zxing.decodeImage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.orhanobut.logger.Logger;
import com.wbl.zxing.MainActivity;
import com.wbl.zxing.R;
import com.wbl.zxing.image.RGBLuminanceSource;

import java.util.Hashtable;


/**
 * Created by djtao on 2016/9/6.
 */

public class DecodeImageHandler extends Handler {
    private static final String TAG=DecodeImageHandler.class.getSimpleName();
    private MainActivity activity;
    private final MultiFormatReader multiFormatReader;
    public DecodeImageHandler(MainActivity activity,Hashtable<DecodeHintType,Object> hints){
        multiFormatReader=new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity=activity;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case R.id.decode:
                decode((Bitmap) msg.obj);
                break;
            case R.id.quit:
                Looper.myLooper().quit();
                break;
        }
    }


    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     *
     */
    private void decode(Bitmap bt) {
        long start=System.currentTimeMillis();
        Result rawResult=null;
        RGBLuminanceSource source=ImageDecodeManager.get().buildRGBLuminanceSource(bt);

        BinaryBitmap bitmap=new BinaryBitmap(new HybridBinarizer(source));
        try{
            rawResult=multiFormatReader.decodeWithState(bitmap);
        }catch (ReaderException e){
            //continue;
        }finally {
            multiFormatReader.reset();
        }
        Handler handler=activity.getHandler();
        if(rawResult!=null){
            long end = System.currentTimeMillis();
            Logger.t("TAG").e("Found barcode ( %d ms ) %s",(end-start),rawResult.toString());
            if(handler!=null){
                Message message=Message.obtain(handler,R.id.decode_succeeded,rawResult);
                message.sendToTarget();
            }
        }else{
            if(handler!=null){
                Message message=Message.obtain(handler,R.id.decode_failed);
                message.sendToTarget();
            }
        }

    }

}
