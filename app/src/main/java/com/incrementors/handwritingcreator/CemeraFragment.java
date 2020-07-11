package com.incrementors.handwritingcreator;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class CemeraFragment extends Fragment implements SurfaceHolder.Callback {

    private static final long ANIMATION_DURATION = 1000L;
    private static final int CAMERA_REQUEST_CODE = 100;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera.Parameters mParameters;
    private Camera camera;
    private TextView text;
    private Animator animator;
    private FrameLayout frame;
    private CardView infoCard, request;
    private View view;
    private FloatingActionButton captureButton;

    public CemeraFragment() {
        // Required empty public constructor
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void animate(View v) {
        TranslateAnimation mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.RELATIVE_TO_PARENT, -1f, TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
        mAnimation.setDuration(2500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        v.setAnimation(mAnimation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cemera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        infoCard = view.findViewById(R.id.alert_card);
        request = view.findViewById(R.id.request);
        if (hasCameraHardware(view.getContext())) {
            checkPermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(view.getContext(), "No camera hardware found", Toast.LENGTH_SHORT).show();
        }
    }

    //checking for camera permission
    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            infoCard.setVisibility(View.VISIBLE);
            request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateView(v);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
            });
        } else {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            init(view);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(view.getContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                infoCard.setVisibility(View.GONE);
                init(view);
            } else {
                //Toast.makeText(this, " naa naa naa Camera Permission Denied", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    private void init(View view) {
        frame = view.findViewById(R.id.frame);
        captureButton = view.findViewById(R.id.captureBtn);
        text = view.findViewById(R.id.text);
        surfaceView = view.findViewById(R.id.surface);
        animator = ObjectAnimator.ofFloat(text, View.ALPHA, 0f, 1f);
        frame.setVisibility(View.VISIBLE);
        createSurface();
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateView(v);
                Toast.makeText(v.getContext(), "Capture button clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createSurface() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void animateView(final View v) {
        v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(20).withEndAction(new Runnable() {
            @Override
            public void run() {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(20);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Toast.makeText(this, "Surface Created", Toast.LENGTH_SHORT).show();
        if (holder.getSurface() == null)
            return;
        try {
            updateCamera(holder);
        } catch (IOException e) {
            Log.d(String.valueOf(getActivity()), "Error starting camera preview on surfaceCreated: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Toast.makeText(this, "Surface changed", Toast.LENGTH_SHORT).show();
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // start preview with new settings
        try {
            updateCamera(holder);
        } catch (Exception e) {
            Log.d(String.valueOf(getActivity()), "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Toast.makeText(this, "Surface Destroyed", Toast.LENGTH_SHORT).show();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }

    public void updateCamera(SurfaceHolder holder) throws IOException {
        camera = getCameraInstance();
        camera.stopPreview();
        mParameters = camera.getParameters();
        setParameter(mParameters);
        camera.setPreviewDisplay(holder);
        camera.startPreview();
    }

    public void setParameter(Camera.Parameters parameters) {
        try {
            parameters = camera.getParameters();

            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mParameters.set("orientation", "protrait");
                camera.setDisplayOrientation(90);
                mParameters.setRotation(90);
            } else {
                mParameters.set("orientation", "landscape");
                camera.setDisplayOrientation(0);
                mParameters.setRotation(0);
            }

            parameters.setPreviewSize(parameters.getPreviewSize().width, parameters.getPreviewSize().height);

            List<?> focus = parameters.getSupportedFocusModes();
            if (focus != null && focus.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            List<?> whiteMode = parameters.getSupportedWhiteBalance();
            if (whiteMode != null && whiteMode.contains(android.hardware.Camera.Parameters.WHITE_BALANCE_AUTO)) {
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            }

            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
//            return;
        }
    }

    //Check if this device has a camera
    private boolean hasCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
        if (surfaceView != null)
            surfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        if (surfaceView != null)
            surfaceView.setVisibility(View.GONE);
    }
}