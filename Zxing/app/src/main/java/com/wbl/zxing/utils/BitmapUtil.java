package com.wbl.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by djtao on 2016/9/6.
 */

public class BitmapUtil {


    /**
     * 读取图片属性：旋转的角度
     * @param path 图片绝对路径
     * @return degree 旋转的角度
     *
     */
    public static int readPictureDegree(String path){
        int degree=0;
        try{
            ExifInterface exifInterface=new ExifInterface(path);
            int orientation=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree=90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree=180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree=270;
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap getBitmap(String path){
        Bitmap bt=null;
        try {
            File f=new File(path);
            if(!f.exists()||f.isDirectory()){
                return null;
            }

            BufferedInputStream in=new BufferedInputStream(new FileInputStream(
                    new File(path)));

            int orientation=readPictureDegree(path);


            //小于500KB，不进行压缩
            if(f.length()<1000*500){
                bt=BitmapFactory.decodeStream(in);
            }else{
                BitmapFactory.Options options=new BitmapFactory.Options();
                //如果将其设为true的话，在decode时将会返回null,通过此设置可以去查询一个bitmap的属性，比如bitmap的长与宽，而不占用内存大小。
                options.inJustDecodeBounds=true;
                //通过设置此值可以用来降低内存消耗，默认为ARGB_8888: 每个像素4字节. 共32位。
                //如果不需要透明度，可把默认值ARGB_8888改为RGB_565,节约一半内存。
                options.inPreferredConfig=Bitmap.Config.RGB_565;
                options.inSampleSize=4;

                BitmapFactory.decodeStream(in, null, options);

                in=new BufferedInputStream(new FileInputStream(new File(path)));
                options.inJustDecodeBounds=false;
                bt=BitmapFactory.decodeStream(in,null,options);
                in.close();
            }
            if(orientation!=0){
                Matrix matrix=new Matrix();
                matrix.postRotate(orientation,bt.getWidth()/2,bt.getHeight()/2);

                return Bitmap.createBitmap(bt,0,0,bt.getWidth(),bt.getHeight(),matrix,true);

            }

            return bt;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }



    }
}
