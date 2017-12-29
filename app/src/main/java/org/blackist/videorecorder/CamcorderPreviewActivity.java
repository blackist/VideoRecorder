package org.blackist.videorecorder;

import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import org.blackist.videorecorder.common.camcorder.CamcorderWrapper;
import org.blackist.videorecorder.common.camcorder.CameraWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CamcorderPreviewActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "CamcorderPreviewActivity";

    @BindView(R.id.preview_recorder_surface)
    SurfaceView mSurfaceView;
    @BindView(R.id.recorder_stop)
    ImageButton mRecorderStop;
    @BindView(R.id.recorder_start)
    ImageButton mRecorderStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_recorder);
        ButterKnife.bind(this);
        // landscape orientation.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // set full screen.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set translucent in the activity containing surface view.
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        mSurfaceView.getHolder().setFixedSize(1080, 720);
        mSurfaceView.getHolder().setKeepScreenOn(true);

        CameraWrapper.getInstance().setSurfaceView(mSurfaceView);
        CameraWrapper.getInstance().openCamera();

        mRecorderStop.setVisibility(View.INVISIBLE);
    }

    @OnClick({
            R.id.recorder_start,
            R.id.recorder_stop
    })
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.recorder_start: {
                CamcorderWrapper.getInstance()
                        .setCallback(new CamcorderWrapper.CamcorderCallback() {
                            @Override
                            public void onCamcorderStarted() {
                                MainActivity.getMainHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecorderStop.setVisibility(View.VISIBLE);
                                        mRecorderStart.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }

                            @Override
                            public void onCamcorderStopped() {
                                MainActivity.getMainHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecorderStop.setVisibility(View.INVISIBLE);
                                        mRecorderStart.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                            @Override
                            public void onCamcorderFailed() {

                            }
                        })
                        .startAsyncRecorder();
            }
            break;

            case R.id.recorder_stop: {
                CamcorderWrapper.getInstance().stopRecorder();
            }
            break;

            default:
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "[Recorder]: on surface create   ");

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CameraWrapper.getInstance().closeCamera();
    }
}
