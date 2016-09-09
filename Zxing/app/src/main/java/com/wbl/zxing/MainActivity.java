package com.wbl.zxing;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.djcps.library.IosDialog.IosDialog;
import com.djcps.library.IosDialog.SheetItem;
import com.djcps.library.permission.PermissionRequestCode;
import com.djcps.library.photo.utils.BitmapUtils;
import com.djcps.library.photo.widget.PhotoMenuDialog.PhotoMenuDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.orhanobut.logger.Logger;
import com.wbl.zxing.decodeImage.DecodeImageHandler;
import com.wbl.zxing.decodeImage.ImageDecodeManager;
import com.wbl.zxing.decodeImage.MainActivityHandler;
import com.wbl.zxing.permission.PermissionListener;
import com.wbl.zxing.permission.PermissionManager;
import com.wbl.zxing.utils.BitmapUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private TextView mTvPhone;//打开相册
    private TextView mTvScan;//扫一扫
    private ImageView mIvImg;//二维码图片

    private PhotoMenuDialog mPhotoMenu;
    private Bitmap bt;
    private PermissionManager permissionManager;
    private IosDialog mIosDialog;
    private List<SheetItem> sheetItemList;
    PermissionManager helper;


    private MainActivityHandler handler;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1000://用于得到选择图片的返回结果
                if(data!=null){
                    List<Bitmap> bitmapList=data.getParcelableArrayListExtra("bitmap");
                    List<String> paths=data.getStringArrayListExtra("data");
                    if(bitmapList!=null&&paths!=null){
                        bt= BitmapUtil.getBitmap(paths.get(0));
                        mIvImg.setImageBitmap(bt);
                    }
                }


                break;
            case 2000://用于拍照得到
                try{
                    String imagePath=mPhotoMenu.getImageUri();
                    if(imagePath!=null){
                            bt= BitmapUtil.getBitmap(imagePath);
                            if(bt!=null)
                            mIvImg.setImageBitmap(bt);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        ImageDecodeManager.init(this);

        initListener();
    }
    private void initView(){
        mTvPhone=(TextView)findViewById(R.id.main_photo);
        mTvScan=(TextView)findViewById(R.id.main_scan);
        mIvImg=(ImageView)findViewById(R.id.main_img);
        mPhotoMenu=new PhotoMenuDialog(this);
        mIosDialog=new IosDialog(this);
        sheetItemList=new ArrayList<>();
        sheetItemList.add(new SheetItem("识别二维码",1));
        sheetItemList.add(new SheetItem("处理图片清晰度",2));
    }
    private void initListener(){
        mTvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoMenu.show();

            }
        });
        mTvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,QRCodeScannActivity.class));
            }
        });

        mIvImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Logger.t("TAG").e("long->click");
                mIosDialog.show();
                return true;
            }
        });

        mIvImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.t("TAG").e("click");
            }
        });
        mIosDialog.setSheetItems(sheetItemList, new IosDialog.OnSheetMyItemClickListner() {
            @Override
            public void onClickItem(int which) {
                switch (which){
                    case 1:

                        helper = PermissionManager.with(MainActivity.this)
                                //添加权限请求码
                                .addRequestCode(PermissionRequestCode.CAMERA)
                                //设置权限，可以添加多个权限
                                .permissions(Manifest.permission.CAMERA)
                                //设置权限监听器
                                .setPermissionsListener(new PermissionListener() {

                                    @Override
                                    public void onGranted() {
                                        //识别二维码图片
                                        identityImage();
                                    }

                                    @Override
                                    public void onDenied() {
                                        //用户拒绝该权限时调用
                                        Logger.t("TAG").e("拍照权限被拒绝");
                                    }

                                    @Override
                                    public void onShowRationale(String[] permissions) {
                                        //当用户拒绝某权限时并点击`不再提醒`的按钮时，下次应用再请求该权限时，需要给出合适的响应（比如,给个展示对话框来解释应用为什么需要该权限）
                                        Snackbar.make(mIvImg, "需要相机权限才能扫描二维码", Snackbar.LENGTH_INDEFINITE)
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


                        break;
                }
            }


        });
    }
    //识别二维码
    private void identityImage() {
        if(bt==null){
            Toast.makeText(this,"图片为空",Toast.LENGTH_LONG).show();
            return;
        }
        decodeFormats = null;
        characterSet = null;
        ImageDecodeManager.get().setSourceImage(bt);
        handler=new MainActivityHandler(this,decodeFormats,characterSet);
    }

    public Handler getHandler() {
        return handler;
    }

    public void handleDecode(Result rawResult){
        String resultString=rawResult.getText();
        Toast.makeText(this,resultString,Toast.LENGTH_LONG).show();
    }
}
