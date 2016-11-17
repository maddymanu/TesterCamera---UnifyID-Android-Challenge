package com.adityabansal.testercamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.hardware.Camera.Size;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private Preview mPreview;

    private Camera.PictureCallback mPicture;

    final int TIMER = 500;


    private static int camId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Paper.init(getApplicationContext());

        //TODO - IMPORTANT - REMOVE this line for PROD/Deployment
//        Paper.book().destroy();

        super.onCreate(savedInstanceState);


        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {


                Thread savePic = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Inside PictureT", "Saving encoded Pic");
                        String encoded = Base64.encodeToString(data, Base64.DEFAULT);

                        //TODO - IMPORTANT - Commented out saving code because of Kryo Serialization
//                        saveToFile(encoded);
                    }
                });
                savePic.start();
            }
        };


        mPreview = new Preview(getApplicationContext());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        safeCameraOpen(camId);


        setContentView(mPreview);


        Thread takePictures = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    for (int i = 0; i < 10; i++) {
                        mCamera.takePicture(null, null, mPicture);
                        Thread.sleep(TIMER);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        takePictures.start();


    }


    //FUNCNTION that prints all encoded image strings
/*    private void printDetails() {
        final Set<String> savedImageStrings = Paper.book().read("savedImageStrings", new HashSet<String>());
        for (String s : savedImageStrings) {
            Log.d("Paper Svaed : ", s);
        }
    }*/


/*
    public void saveToFile(String encoded) {

        final Set<String> savedImageStrings = Paper.book().read("savedImageStrings", new HashSet<String>());
        savedImageStrings.add(encoded);
        Paper.book().write("savedImageStrings", savedImageStrings);

    }
*/


    @Override
    protected void onStart() {

        super.onStart();
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            /* liberate mCamera */
            releaseCameraAndPreview();

			/* open mCamera */
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

            Log.d("safeC", "being called inside");

            Thread.sleep(2000);


            mPreview.setCamera(mCamera);


            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    /**
     * Left mCamera if it is opened by other application
     */
    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mPreview.mHolder.removeCallback(mPreview);
            mCamera = null;
        }
    }

    @Override
    protected void onStop() {
        // liberate the camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mPreview.mHolder.removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }

        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mPreview.mHolder.removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }

        super.onPause();
    }

}

class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;

    Preview(Context context) {
        super(context);

        mSurfaceView = new SurfaceView(context);
        ;
        addView(mSurfaceView);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCamera.startPreview();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.


        try {
            if (mCamera != null) {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mHolder.removeCallback(this);
            mCamera.stopPreview();
            mCamera.release();
        }
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();

            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }

}

//EOF
