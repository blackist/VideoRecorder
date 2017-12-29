package org.blackist.videorecorder.common.camcorder;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

/**
 * TODO
 *
 * @author LiangLiang.Dong <1075512174@qq.com>.
 * @Date 2017/12/28 10:05.
 */
public class CamcorderWrapper {

    private static final String TAG = "CamcorderWrapper";

    private Camera mCamera;
    private static Boolean isRecording = false;
    private static CamcorderWrapper mCamcordWrapper = new CamcorderWrapper();

    /**
     * output video file.
     */
    private File mVideoFile;
    private String mVideoPath;
    private MediaRecorder mRecorder;
    private CamcorderCallback mCamcorderCallback;

    private CamcorderWrapper() {

    }

    public static CamcorderWrapper getInstance() {
        return mCamcordWrapper;
    }

    /**
     * setters
     */
    public CamcorderWrapper setVideoPath(String path) {
        mVideoPath = path;
        return this;
    }

    public CamcorderWrapper setCallback(CamcorderCallback callback) {
        mCamcorderCallback = callback;
        return this;
    }

    /**
     * start recording.
     */
    public void startRecorder() {
        if (prepareVideoRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            // start recording.
            Log.d(TAG, "[Recorder]: start recording.");
            mRecorder.start();
            isRecording = true;
            // callback.
            if (mCamcorderCallback != null) {
                mCamcorderCallback.onCamcorderStarted();
            }
        } else {
            // prepare didn't work, release the camera
            releaseRecorder();
        }
    }

    /**
     * start recording.
     */
    public void startAsyncRecorder() {
        if (!isRecording) {
            new CamcorderPrepareTask().execute(null, null, null);
        }
    }

    public boolean prepareVideoRecorder() {
        // create recorder.
        mRecorder = new MediaRecorder();
        mRecorder.reset();

        mCamera = CameraWrapper.getInstance().getCamera();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mRecorder.setCamera(mCamera);

        // Step 2: Set sources
        // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        // 设置从摄像头采集图像
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        CamcorderProfile profile = CamcorderProfile.get(
                CameraWrapper.getCameraId(),
                CamcorderProfile.QUALITY_HIGH);
        // 设置视频文件的输出格式, 必须在设置声音编码格式、图像编码格式之前设置
        // mRecorder.setOutputFormat();
        profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        // 设置声音编码的格式
        // mRecorder.setAudioEncoder();
        profile.audioCodec = MediaRecorder.AudioEncoder.DEFAULT;
        // 设置图像编码的格式
        // mRecorder.setVideoEncoder();
        profile.videoCodec = MediaRecorder.VideoEncoder.DEFAULT;
        // 采样率,每秒 4帧
        // mRecorder.setVideoFrameRate(30);
        profile.videoBitRate = 30;
        // 设置video size
        // mRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
        profile.videoFrameWidth = CameraWrapper.getInstance().getPreviewWidth();
        profile.videoFrameHeight = CameraWrapper.getInstance().getPreviewHeight();
        mRecorder.setProfile(profile);

        // Step 4: Create video file andSet output file
        mVideoFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mVideoFile == null) {
            Log.d(TAG, "[Recorder]: create video failed.");
            return false;
        }
        Log.d(TAG, "[Recorder]: video path - " + mVideoFile.getAbsolutePath());
        mRecorder.setOutputFile(mVideoFile.getAbsolutePath());

        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "[Recorder]: prepare IllegalStateException");
            releaseRecorder();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "[Recorder]: prepare IOException");
            releaseRecorder();
            return false;
        }
        Log.d(TAG, "[Recorder]: prepared");

        return true;
    }

    /**
     * stop recording.
     */
    public void stopRecorder() {
        if (isRecording && mRecorder != null) {
            Log.d(TAG, "[Recorder]: stop recording. ");
            try {
                mRecorder.stop();
            } catch (RuntimeException e) {
                // output file is not properly constructed ans should be deleted.
                mVideoFile.delete();
            }
            isRecording = false;
            // callback.
            if (mCamcorderCallback != null) {
                mCamcorderCallback.onCamcorderStopped();
                mCamcorderCallback = null;
            }
            releaseRecorder();
        }
    }

    /**
     * release the MediaRecorder object
     * and take camera access back from MediaRecorder
     */
    private void releaseRecorder() {
        if (mRecorder != null) {
            Log.d(TAG, "[Recorder]: recorder release. ");
            // clear recorder configuration
            mRecorder.reset();
            // release the recorder object
            mRecorder.release();
            mRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
            mCamera = null;
        }
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    class CamcorderPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                // start recording.
                Log.d(TAG, "[Recorder]: start recording.");
                mRecorder.start();
                isRecording = true;
                // callback.
                if (mCamcorderCallback != null) {
                    mCamcorderCallback.onCamcorderStarted();
                }
            } else {
                // prepare didn't work, release the camera
                releaseRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result && mCamcorderCallback != null) {
                mCamcorderCallback.onCamcorderFailed();
            }
        }
    }

    /**
     * camcorder callback.
     */
    public interface CamcorderCallback {

        /**
         * invoked on camcorder started.
         */
        void onCamcorderStarted();

        /**
         * invoked on camcorder stopped.
         */
        void onCamcorderStopped();

        /**
         * invoked on camcorder failed.
         */
        void onCamcorderFailed();
    }
}
