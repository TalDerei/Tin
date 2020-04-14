package edu.lehigh.cse216.teamtin;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {

    SurfaceView surfaceView;
    SurfaceHolder holder;
    Camera.Size previewSize;
    List<Camera.Size> supportedPreviewSizes;
    Camera cam;

    public CameraPreview(Context context, SurfaceView sv) {
        super(context);
        surfaceView = sv;
        if(surfaceView == null) {
            Log.e("SurfaceView", "SurfaceView is null for some reason");
        }
        //addView(surfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
    }

    public void setCamera(Camera cm) {
        cam = cm;

        if(cam != null) {
            Camera.Parameters params = cam.getParameters();
            supportedPreviewSizes = params.getSupportedPreviewSizes();
            List<String> focusModes = params.getSupportedFocusModes();

            this.requestLayout();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            cam.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.e("CameraError", e.getMessage(), e);
        } catch (NullPointerException e) {
            Log.e("CameraError", "Camera was set to null");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (cam == null) {
            return;
        }
        cam.stopPreview();
        Camera.Parameters parameters = cam.getParameters();
        if(previewSize != null) parameters.setPreviewSize(previewSize.width, previewSize.height);

        int orientation = getResources().getConfiguration().orientation;
        int rotation = 0;
        if(orientation == 1) {
            cam.setDisplayOrientation(0);
        } else if (orientation == 2) {
            cam.setDisplayOrientation(90);
        }

        requestLayout();

        cam.setParameters(parameters);
        cam.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(cam != null) cam.stopPreview();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }
        }
    }
}
