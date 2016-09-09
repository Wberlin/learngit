package com.wbl.zxing.decodeImage;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.wbl.zxing.MainActivity;
import com.wbl.zxing.decoding.DecodeFormatManager;

import java.util.Hashtable;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 * 解码线程
 */
public class DecodeImageThread extends Thread{
    private final CountDownLatch handlerInitLatch;
    private Handler handler;
    private final Hashtable<DecodeHintType, Object> hints;
    private MainActivity activity;
    DecodeImageThread(MainActivity activity,
            Vector<BarcodeFormat> decodeFormats,
                      String characterSet
                      ){
        this.activity=activity;
        handlerInitLatch = new CountDownLatch(1);
        hints = new Hashtable<DecodeHintType, Object>(3);
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }


    }

    Handler getHandler() {
        try {
            //可以看到每次调用getHandler()时，都会调用handlerInitLatch.await();
            // ：调用此方法会一直阻塞当前线程，直到计时器的值为0；而创建CountDownLatch时，传入的是1，所以计数器的值只会从1-0
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler=new DecodeImageHandler(activity,hints);
        //释放阻塞线程
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
