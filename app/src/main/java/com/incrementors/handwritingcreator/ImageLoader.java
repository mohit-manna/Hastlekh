package com.incrementors.handwritingcreator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;

    ImageLoader(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String imagePath = strings[0];
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
    }
}
