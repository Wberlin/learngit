package com.wbl.zxing.decodeImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.wbl.zxing.image.RGBLuminanceSource;

/**
 * Created by djtao on 2016/9/6.
 */

public class ImageDecodeManager {
    private Bitmap bt=null;
    private static ImageDecodeManager imageDecodeManager;

    public static ImageDecodeManager get(){
        return imageDecodeManager;
    }
    public static void init(Context context){
        if(imageDecodeManager==null){
            imageDecodeManager=new ImageDecodeManager();
        }
    }

    public RGBLuminanceSource buildRGBLuminanceSource(Bitmap bt){
            return new RGBLuminanceSource(bt);
    }

    public void decodeImage(Handler handler,int message){
        if(handler!=null){
            Message msg=handler.obtainMessage(message,bt);
            msg.sendToTarget();
            handler=null;
        }
    }

    public void setSourceImage(Bitmap bt){
        this.bt=bt;
    }

}
