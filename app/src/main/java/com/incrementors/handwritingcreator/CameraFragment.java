package com.incrementors.handwritingcreator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, ItemClickListener {
    private static final float COLOR_TOLERANCE = 50;
    private static final int CAMERA_AND_EXTERNAL_REQUEST_CODE = 100;
    static String path;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera.Parameters mParameters;
    private Camera camera;
    private CardView infoCard, confirmImageLayout;
    private View view;
    private List<File> files;
    private TextView suggestion;
    private TextInputEditText character;
    private Camera.Parameters parameters;
    private ImageView captureButton, saveImage, discardImage, capturedImage;
    private ToggleButton flasbtn;
    private Camera.PictureCallback pictureCallback;
    private CameraManager camManager;
    private RecyclerView characterList;
    private CharactersAdapter charactersAdapter;
    private TextInputLayout characterInpuLayout;
    private LinearLayout cameraControlLayout, controlsLayout, scanner;
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
                    createFile();
                    File appDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
                    if (appDir.exists()) {
                        try {
                            Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                            bitmapPicture = rotateImage(bitmapPicture, 90);

                            bitmapPicture = crop(bitmapPicture);
                            //bitmapPicture = getGrayScale(bitmapPicture);
                            bitmapPicture = removeBack(bitmapPicture);


                            String imageName = character.getText().toString().trim() + ".webp";
                            File file = new File(appDir, imageName);
                            FileOutputStream fos = new FileOutputStream(file);

                            //fos.write(data);

                            bitmapPicture.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                            fos.close();

                            //Toast.makeText(getContext(), "Image saved", Toast.LENGTH_LONG).show();

                            Bitmap bitmap = BitmapFactory.decodeFile(appDir + "/" + imageName);

                            SharedPreferences sh = getContext().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString("lastsavedimage", imageName);
                            editor.commit();

                            capturedImage.setImageBitmap(bitmap);

                        } catch (FileNotFoundException e) {
                            e.getMessage();
                        } catch (IOException e) {
                            e.getMessage();
                        }

                        camera.startPreview();
                        confirmImageLayout.setVisibility(View.GONE);
                        cameraControlLayout.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getContext(), "File not found", Toast.LENGTH_SHORT).show();
                    }
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
    private FrameLayout frame;
    private boolean flash;
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

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public void populateCharacters() {
        File appDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        if (appDir.exists()) {
//            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            files = Arrays.asList(directory.listFiles());
            charactersAdapter = new CharactersAdapter(getContext(), files);
            characterList.setAdapter(charactersAdapter);
            characterList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
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
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init(view);
        if (hasCameraHardware(view.getContext())) {
            checkPermission(Manifest.permission.CAMERA, CAMERA_AND_EXTERNAL_REQUEST_CODE);
        } else {
            Toast.makeText(view.getContext(), "No camera hardware found", Toast.LENGTH_SHORT).show();
        }
    }

    //checking for camera permission
    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_AND_EXTERNAL_REQUEST_CODE);
        } else {
            createSurface();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_AND_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(view.getContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                createSurface();
            } else {
                //Toast.makeText(this, " naa naa naa Camera Permission Denied", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    private void init(View view) {
        imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        infoCard = view.findViewById(R.id.alert_card);
        character = view.findViewById(R.id.character);
        surfaceView = view.findViewById(R.id.surface);
        flasbtn = view.findViewById(R.id.toggleFlash);
        saveImage = view.findViewById(R.id.saveImage);
        suggestion = view.findViewById(R.id.suggestion);
        captureButton = view.findViewById(R.id.captureBtn);
        scanner = view.findViewById(R.id.scanner);
        frame = view.findViewById(R.id.frame);
        discardImage = view.findViewById(R.id.discardImage);
        capturedImage = view.findViewById(R.id.capturedImage);
        characterList = view.findViewById(R.id.characterList);
        controlsLayout = view.findViewById(R.id.controlsLayout);
        confirmImageLayout = view.findViewById(R.id.confirmImageLayout);
        cameraControlLayout = view.findViewById(R.id.cameraControlLayout);
        characterInpuLayout = view.findViewById(R.id.characterLayout);

        characterList.setHasFixedSize(true);

        //initializing file path
        path = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);

        //getting last saved image
        SharedPreferences preferences = getContext().getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE);
        if (preferences.contains("lastsavedimage")) {
            Bitmap bitmap = BitmapFactory.decodeFile(path + "/" + preferences.getString("lastsavedimage", ""));
            //setting last saved image in imageview
            capturedImage.setImageBitmap(bitmap);
        }

        capturedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "image container clicked", Toast.LENGTH_LONG).show();
                if (capturedImage.getDrawable() != null) {
                    showGallery();
                    hideCharacterInputBox();
                    populateCharacters();
                } else {
                    hideGallery();
                }
            }
        });

        cameraControlLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                character.clearFocus();
            }
        });

        controlsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                character.clearFocus();
            }
        });

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                character.clearFocus();
                hideGallery();
                showCharacterInputBox();
            }
        });

        createSurface();

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateView(v);
                hideGallery();
                showCharacterInputBox();

                if (character.getText().toString().trim().isEmpty()) {
                    //Toast.makeText(getContext(), "Please enter character to map the image", Toast.LENGTH_LONG).show();
                    character.setBackground(getResources().getDrawable(R.drawable.emptybox));
                } else {
                    camera.takePicture(null, null, pictureCallback_JPG);
                }
            }
        });

        flasbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hideGallery();
                    showCharacterInputBox();
                    Toast.makeText(getContext(), "Checked", Toast.LENGTH_SHORT).show();
                    flash = true;
                    try {
                        updateCamera(surfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    hideGallery();
                    showCharacterInputBox();
                    flash = false;
                    try {
                        updateCamera(surfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Not Checked", Toast.LENGTH_SHORT).show();
                }
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

        cameraControlLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGallery();
                showCharacterInputBox();
            }
        });
    }

    //I added this method because people keep asking how
    //to calculate the dimensions of the bitmap...see comments below
    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        //use the smallest dimension of the image to crop to
        return Math.min(bitmap.getWidth() / 4, bitmap.getHeight() / 4);
    }

    public Bitmap getGrayScale(Bitmap src) {
        float[] matrix = new float[]{
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0,
        };

        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);
        return dest;
    }

    public Bitmap crop(Bitmap source) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        Toast.makeText(getContext(), "screen size: " + sourceWidth + ", " + sourceHeight, Toast.LENGTH_SHORT).show();
        int cx = sourceWidth / 2;
        int cy = sourceHeight / 2;
        int newWidth = 500;
        int newHeight = 500;
        int size = Math.min(newWidth, newHeight);
        int left = cx - ((size) / 2);
        int top = cy - ((size) / 2);
        Toast.makeText(getContext(), "Left: " + left + " Right: " + newWidth + " Top: " + top + "Bottom" + newHeight + " new size: " + size, Toast.LENGTH_SHORT).show();
        source.setPixel(1560, 2080, Color.RED);
        Bitmap dest = Bitmap.createBitmap(source, left, top, size, size);
        return dest;
    }


    public void hideGallery() {
        if (infoCard.getVisibility() == View.VISIBLE)
            infoCard.setVisibility(View.GONE);
    }

    public void showGallery() {
        if (infoCard.getVisibility() == View.GONE) {
            infoCard.setVisibility(View.VISIBLE);
        }
    }

    public void hideCharacterInputBox() {
        if (characterInpuLayout.getVisibility() == View.VISIBLE)
            characterInpuLayout.setVisibility(View.GONE);
    }

    public void showCharacterInputBox() {
        if (characterInpuLayout.getVisibility() == View.GONE)
            characterInpuLayout.setVisibility(View.VISIBLE);
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
        setParameter(mParameters, flash);
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

            List<?> flashMode;
            if (flashValue) {
                flashMode = parameters.getSupportedFlashModes();
                if (flashMode != null && flashMode.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                }
            } else {
                flashMode = parameters.getSupportedFlashModes();
                if (flashMode != null && flashMode.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
            }

            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void createFile() {
        File newdir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        if (!newdir.exists())
            if (!newdir.mkdirs())
                Log.d("File creation", "failed to create directory");
            else
                Log.d("File creation", "file created successfully");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        if (surfaceView != null)
            surfaceView.setVisibility(View.GONE);
    }

    public Bitmap removeBack(Bitmap oldBitmap) {
        int colorToReplace = oldBitmap.getPixel(20, 50);

        int width = oldBitmap.getWidth();
        int height = oldBitmap.getHeight();
        int[] pixels = new int[width * height];
        oldBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int rA = Color.alpha(colorToReplace);
        int rR = Color.red(colorToReplace);
        int rG = Color.green(colorToReplace);
        int rB = Color.blue(colorToReplace);
        int pixel;

        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                int index = y * width + x;
                //Log.i("Index", "" + index);
                pixel = pixels[index];
                int rrA = Color.alpha(pixel);
                int rrR = Color.red(pixel);
                int rrG = Color.green(pixel);
                int rrB = Color.blue(pixel);

                if (rA - COLOR_TOLERANCE < rrA && rrA < rA + COLOR_TOLERANCE && rR - COLOR_TOLERANCE < rrR && rrR < rR + COLOR_TOLERANCE &&
                        rG - COLOR_TOLERANCE < rrG && rrG < rG + COLOR_TOLERANCE && rB - COLOR_TOLERANCE < rrB && rrB < rB + COLOR_TOLERANCE) {
                    pixels[index] = Color.TRANSPARENT;
                }
            }
        }

        Bitmap newBitmap = Bitmap.createBitmap(width, height, oldBitmap.getConfig());
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBitmap;
    }

    public Bitmap polish(Bitmap oldBitmap, int targetColor) {
        int colorToReplace = oldBitmap.getPixel(20, 50);

        int width = oldBitmap.getWidth();
        int height = oldBitmap.getHeight();
        int[] pixels = new int[width * height];
        oldBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int rA = Color.alpha(colorToReplace);
        int rR = Color.red(colorToReplace);
        int rG = Color.green(colorToReplace);
        int rB = Color.blue(colorToReplace);
        int pixel;

        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                int index = y * width + x;
                //Log.i("Index", "" + index);
                pixel = pixels[index];
                int rrA = Color.alpha(pixel);
                int rrR = Color.red(pixel);
                int rrG = Color.green(pixel);
                int rrB = Color.blue(pixel);

                if (rA - COLOR_TOLERANCE < rrA && rrA < rA + COLOR_TOLERANCE && rR - COLOR_TOLERANCE < rrR && rrR < rR + COLOR_TOLERANCE &&
                        rG - COLOR_TOLERANCE < rrG && rrG < rG + COLOR_TOLERANCE && rB - COLOR_TOLERANCE < rrB && rrB < rB + COLOR_TOLERANCE) {
                    pixels[index] = pixels[index];
                } else
                    pixels[index] = targetColor;
            }
        }

        Bitmap newBitmap = Bitmap.createBitmap(width, height, oldBitmap.getConfig());
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBitmap;
    }

    @Override
    public void onClick(View view, int position) {
        final File file = files.get(position);
        //NavController navController=
    }
}