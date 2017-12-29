package org.blackist.videorecorder;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.recorder_stop)
    ImageButton mRecorderStop;
    @BindView(R.id.recorder_start)
    ImageButton mRecorderStart;

    private static MainHandler mMainHandler = new MainHandler();

    public static MainHandler getMainHandler() {
        return mMainHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            }, 1);
        }

        mRecorderStop.setVisibility(View.INVISIBLE);
    }

    @OnClick({
            R.id.preview_recorder_btn,
            R.id.back_recorder_btn,
            R.id.recorder_start,
            R.id.recorder_stop
    })
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.preview_recorder_btn: {
                startActivity(new Intent(
                        MainActivity.this,
                        CamcorderPreviewActivity.class));
            }
            break;

            case R.id.back_recorder_btn: {

            }
            break;

            case R.id.recorder_start: {
            }
            break;

            case R.id.recorder_stop: {
            }
            break;

            default:
        }
    }

    /**
     * main task action.
     */
    public static final int MAIN_RECORDER_BACK_START = 1;
    public static final int MAIN_RECORDER_BACK_STOP = 2;
    public static final int MAIN_RECORDER_BACK_PAUSE = 3;

    /**
     * handler of main task on ui thread.
     */
    static class MainHandler extends Handler {

        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MAIN_RECORDER_BACK_START: {

                }
                break;

                case MAIN_RECORDER_BACK_PAUSE: {

                }
                break;

                case MAIN_RECORDER_BACK_STOP: {

                }
                break;

                default:
            }
        }
    }
}
