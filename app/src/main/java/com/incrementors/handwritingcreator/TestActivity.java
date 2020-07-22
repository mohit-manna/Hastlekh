package com.incrementors.handwritingcreator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class TestActivity extends AppCompatActivity {
    private static final float COLOR_TOLERANCE = 20;
    int w = 400, h = 400;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        File appDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        bitmap = BitmapFactory.decodeFile(appDir + "/" + "D.png");
        imageView = findViewById(R.id.image);
        textView = findViewById(R.id.text);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache(true);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    bitmap = imageView.getDrawingCache();
                    bitmap = removeBack(bitmap, (int) event.getX(), (int) event.getY());

                    int pixel = bitmap.getPixel((int) event.getX(), (int) event.getY());
                    int r = Color.red(pixel);
                    int g = Color.red(pixel);
                    int b = Color.red(pixel);
                    textView.setBackgroundColor(Color.rgb(r, g, b));
                    textView.setText("R: " + r + "\n" + "G: " + g + "\n" + "B: " + b);
//                    bitmap.setPixel((int) event.getX(), (int) event.getY(), Color.TRANSPARENT);
                    imageView.setImageBitmap(bitmap);
                }
                return false;
            }
        });


    }

    public Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return bmOut;
    }

    public Bitmap removeBack(Bitmap oldBitmap, int i, int j) {
        int colorToReplace = oldBitmap.getPixel(i, j);

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
}