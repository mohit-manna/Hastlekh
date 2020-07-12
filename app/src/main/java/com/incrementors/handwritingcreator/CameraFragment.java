package com.incrementors.handwritingcreator;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final long ANIMATION_DURATION = 1000L;
    private static final int CAMERA_AND_EXTERNAL_REQUEST_CODE = 100;
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }
    };
    Camera.PictureCallback pictureCallback_RAW = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {

        }
    };
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera.Parameters mParameters;
    private Camera camera;
    private FrameLayout frame;
    private CardView infoCard, request;
    private View view;
    private TextInputEditText character;
    private Camera.Parameters parameters;
    private ImageView captureButton, saveImage, discardImage;
    private ToggleButton flasbtn;
    private Camera.PictureCallback pictureCallback;
    private CameraManager camManager;
    private TextInputLayout characterInpuLayout;
    private LinearLayout confirmImageLayout, cameraControlLayout;
    Camera.PictureCallback pictureCallback_JPG = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera cam) {
            // TODO Auto-generated method stub
            confirmImageLayout.setVisibility(View.VISIBLE);
            cameraControlLayout.setVisibility(View.GONE);
            saveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateView(v);
                    Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Uri uriTarget = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    OutputStream imageFileOS;
                    try {
                        imageFileOS = getActivity().getContentResolver().openOutputStream(uriTarget);
                        imageFileOS.write(data);
                        imageFileOS.flush();
                        imageFileOS.close();

                        Toast.makeText(getContext(), "Image saved: " + uriTarget.toString(), Toast.LENGTH_LONG).show();
                        Log.i("image", uriTarget.normalizeScheme().toString());

                    } catch (FileNotFoundException e) {
                        e.getMessage();
                    } catch (IOException e) {
                        e.getMessage();
                    }
                    camera.startPreview();
                    confirmImageLayout.setVisibility(View.GONE);
                    cameraControlLayout.setVisibility(View.VISIBLE);
                }
            });

            discardImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateView(v);
                    camera.startPreview();
                    confirmImageLayout.setVisibility(View.GONE);
                    cameraControlLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    };
    private InputMethodManager imm;

    public CameraFragment() {

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
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        infoCard = view.findViewById(R.id.alert_card);
        request = view.findViewById(R.id.request);
        if (hasCameraHardware(view.getContext())) {
            checkPermission(Manifest.permission.CAMERA, CAMERA_AND_EXTERNAL_REQUEST_CODE);
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
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_AND_EXTERNAL_REQUEST_CODE);
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
        if (requestCode == CAMERA_AND_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(view.getContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                init(view);
            } else {
                //Toast.makeText(this, " naa naa naa Camera Permission Denied", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    private void init(View view) {
        imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        frame = view.findViewById(R.id.frame);
        captureButton = view.findViewById(R.id.captureBtn);
        character = view.findViewById(R.id.character);
        surfaceView = view.findViewById(R.id.surface);
        flasbtn = view.findViewById(R.id.toggleFlash);
        saveImage = view.findViewById(R.id.saveImage);
        discardImage = view.findViewById(R.id.discardImage);
        confirmImageLayout = view.findViewById(R.id.confirmImageLayout);
        cameraControlLayout = view.findViewById(R.id.cameraControlLayout);
        characterInpuLayout = view.findViewById(R.id.characterLayout);
        infoCard.setVisibility(View.GONE);
        frame.setVisibility(View.VISIBLE);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                character.clearFocus();
            }
        });

        createSurface();

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateView(v);
                if (character.getText().toString().trim().isEmpty()) {
                    //Toast.makeText(getContext(), "Please enter character to map the image", Toast.LENGTH_LONG).show();
                    character.setBackground(getResources().getDrawable(R.drawable.emptybox));
                } else {
                    camera.takePicture(shutterCallback, pictureCallback_RAW, pictureCallback_JPG);
                }
            }
        });

        flasbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getContext(), "Checked", Toast.LENGTH_SHORT).show();
                    turnFlashlightOn();
                } else
                    Toast.makeText(getContext(), "Not Checked", Toast.LENGTH_SHORT).show();
            }
        });


        character.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    characterInpuLayout.setErrorEnabled(false);
                } else {
                    imm.hideSoftInputFromWindow(character.getWindowToken(), 0);
                    character.setBackground(getResources().getDrawable(R.drawable.characterbox));
                }
            }
        });
    }


    private void turnFlashlightOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                camManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                String cameraId = null;
                if (camManager != null) {
                    cameraId = camManager.getCameraIdList()[0];
                    camManager.setTorchMode(cameraId, true);
                }
            } catch (CameraAccessException e) {
                Log.e(getContext().toString(), e.toString());
            }
        } else {
            camera = Camera.open();
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        }
    }

    public void createSurface() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void animateView(final View v) {
        v.animate().scaleX(0.80f).scaleY(0.80f).setDuration(50).withEndAction(new Runnable() {
            @Override
            public void run() {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(50);
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
        setParameter(mParameters, true);
        camera.setPreviewDisplay(holder);
        camera.startPreview();
    }

    public void setParameter(Camera.Parameters parameters, boolean flashValue) {
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

            if (flashValue) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        camManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                        String cameraId = null;
                        if (camManager != null) {
                            cameraId = camManager.getCameraIdList()[0];
                            camManager.setTorchMode(cameraId, true);
                        }
                    } catch (CameraAccessException e) {
                        Log.e(getContext().toString(), e.toString());
                    }
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
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