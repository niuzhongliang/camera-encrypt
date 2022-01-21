package com.example.encoder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
public class MainActivity extends Activity implements SurfaceHolder.Callback,PreviewCallback{
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Parameters parameters;
    int width = 1280;
    int height = 720;
    int framerate = 30;
    int biterate = 8500*1000;
    private static int yuvqueuesize = 10;
    //待解码视频缓冲队列，静态成员！
    public static ArrayBlockingQueue YUVQueue = new ArrayBlockingQueue(yuvqueuesize);
    private AvcEncoder avcCodec;
    private String[] permission = {Manifest.permission.CAMERA};
    private Button mBtnPhotograph;
    private Button mBtnStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnPhotograph = findViewById(R.id.btn_Photograph);
        mBtnStop = findViewById(R.id.btn_stop);
        initPermission();
        surfaceview = (SurfaceView)findViewById(R.id.surfaceview);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        mBtnPhotograph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                takePicture();//拍照
//                String path = cacheDir.getPath() + "/test1.h264";
//                avcCodec = new CameraEncoder(width,height,framerate,biterate, path);
//                avcCodec.StartEncoderThread();

            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != camera) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    avcCodec.StopThread();
                }
            }
        });
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = getBackCamera();
        startcamera(camera);
//创建AvEncoder对象
//        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/test1.h264";
        String path = this.getExternalCacheDir().getPath() + "/test1.h264";
        avcCodec = new AvcEncoder(width,height,framerate,biterate, path);
//启动编码线程
        avcCodec.StartEncoderThread();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            avcCodec.StopThread();
        }
    }
    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
//将当前帧图像保存在队列中
        putYUVData(data,data.length);
        Log.d("tag", "putyuv....====");
    }
    public void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
            Log.d("tag", "poll.");
        }
        YUVQueue.add(buffer);
    }
    private void startcamera(Camera mCamera){
        if(mCamera != null){
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);
                if(parameters == null){
                    parameters = mCamera.getParameters();
                }
//获取默认的camera配置
                parameters = mCamera.getParameters();
//设置预览格式
                parameters.setPreviewFormat(ImageFormat.NV21);
//设置预览图像分辨率
                parameters.setPreviewSize(width, height);
//配置camera参数
                mCamera.setParameters(parameters);
//将完全初始化的SurfaceHolder传入到setPreviewDisplay(SurfaceHolder)中
//没有surface的话，相机不会开启preview预览
                mCamera.setPreviewDisplay(surfaceHolder);
//调用startPreview()用以更新preview的surface，必须要在拍照之前start Preview
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Camera getBackCamera() {
        Camera c = null;
        try {
//获取Camera的实例
            c = Camera.open(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
//获取Camera的实例失败时返回null
        return c;
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 1);
        }

    }
}