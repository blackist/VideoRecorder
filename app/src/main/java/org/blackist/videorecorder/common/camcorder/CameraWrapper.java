package org.blackist.videorecorder.common.camcorder;

import android.annotation.TargetApi;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author LiangLiang.Dong <1075512174@qq.com>.
 * @Date 2017/12/28 9:32.
 */
public class CameraWrapper {

    private static final String TAG = "CameraWrapper";

    private static int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private SurfaceView mSurfaceView;

    private int mPreviewWidth;
    private int mPreviewHeight;

    private Camera mCamera;
    private static CameraWrapper mCameraWrapper = new CameraWrapper();
    private CameraHandlerThread mCameraThread;

    private CameraWrapper() {
        mCameraThread = new CameraHandlerThread(TAG);
    }

    public static CameraWrapper getInstance() {
        return mCameraWrapper;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public static int getCameraId() {
        return mCameraId;
    }

    /**
     * set surface view.
     *
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    /**
     * @return
     */
    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    /**
     * @return
     */
    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    /**
     *
     */
    public void openCamera() {
        mCameraThread.openCamera();
    }

    /**
     *
     */
    public void openCamera(CameraOpenCallback cameraOpenCallback) {
        mCameraThread.openCamera(cameraOpenCallback);
    }

    /**
     *
     */
    public void closeCamera() {
        mCameraThread.closeCamera();
    }

    /**
     *
     */
    public void closeCamera(CameraCloseCallback callback) {
        mCameraThread.closeCamera(callback);
    }

    /**
     * open original camera.
     */
    private void turnOnCamera() {

        Log.d(TAG, "[Camera]: open camera.");

        mCamera = getDefaultCamera(mCameraId);

        Camera.Parameters params = mCamera.getParameters();
        mCamera.setParameters(params);
        Camera.Parameters p = mCamera.getParameters();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Size optimalSize;
        if (mSurfaceView != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, mSurfaceView.getWidth(), mSurfaceView.getHeight());
        } else {
            List<Camera.Size> listSize = p.getSupportedPreviewSizes();
            optimalSize = listSize.get(0);

        }
        mPreviewWidth = optimalSize.width;
        mPreviewHeight = optimalSize.height;
        Log.d(TAG, "[Camera]: width = " + mPreviewWidth + " height = " + mPreviewHeight);

        p.setPreviewSize(mPreviewWidth, mPreviewHeight);
        p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        mCamera.setParameters(p);

        try {
            if (mSurfaceView != null) {
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(TAG, "[Camera]: start preview failed.");
            e.printStackTrace();
        }
    }

    /**
     * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
     *                 or Camera.CameraInfo.CAMERA_FACING_BACK.
     * @return the default camera on the device. Returns null if camera is not available.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static Camera getDefaultCamera(int position) {
        // Find the total number of cameras available
        int mNumberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);
            }
        }
        return null;
    }

    /**
     * close original camera.
     */
    private void shutdownCamera() {
        Log.d(TAG, "[Camera]: stop camera. ");
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private static final int CAMERA_CLOSE = 0;
    private static final int CAMERA_OPEN = 1;

    /**
     * camera thread on which to handle the camera data.
     */
    private class CameraHandlerThread extends HandlerThread {

        private Handler mHandler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            mHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case CAMERA_OPEN: {
                            turnOnCamera();
                            Log.d(TAG, "[Camera]: camera opened.");
                            if (msg.obj != null) {
                                ((CameraOpenCallback) msg.obj).onCameraOpened();
                            }
                        }
                        break;

                        case CAMERA_CLOSE: {
                            shutdownCamera();
                            mSurfaceView = null;
                            Log.d(TAG, "[Camera]: camera closed.");
                            if (msg.obj != null) {
                                ((CameraCloseCallback) msg.obj).onCameraClosed();
                            }
                        }
                        break;

                        default:
                    }
                }
            };
        }

        /**
         * open camera on camera thread.
         */
        public void openCamera() {
            mHandler.sendEmptyMessage(CAMERA_OPEN);
        }

        /**
         * open camera with callback on camera thread.
         */
        public void openCamera(CameraOpenCallback callback) {
            Message message = mHandler.obtainMessage(CAMERA_OPEN);
            message.obj = callback;
            mHandler.sendMessage(message);
        }

        /**
         * close camera on camera thread.
         */
        public void closeCamera() {
            mHandler.sendEmptyMessage(CAMERA_CLOSE);
        }

        /**
         * close camera on camera thread.
         */
        public void closeCamera(CameraCloseCallback callback) {
            Message message = mHandler.obtainMessage(CAMERA_CLOSE);
            message.obj = callback;
            mHandler.sendMessage(message);
        }

        public void post(Runnable runnable) {
            if (runnable != null) {
                mHandler.post(runnable);
            }
        }
    }

    /**
     * camera open callback.
     */
    public interface CameraOpenCallback {

        /**
         * invoked on camera is opened.
         */
        void onCameraOpened();
    }

    /**
     * camera close callback.
     */
    public interface CameraCloseCallback {

        /**
         * invoked on camera is closed.
         */
        void onCameraClosed();
    }
}
