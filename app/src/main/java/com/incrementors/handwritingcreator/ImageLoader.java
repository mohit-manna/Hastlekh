package com.incrementors.handwritingcreator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;
    private WeakReference<CharactersAdapter> charactersAdapterWeakReference;

    public ImageLoader(CharactersAdapter charactersAdapter, ImageView imageView) {
        this.imageView = imageView;
        this.charactersAdapterWeakReference = new WeakReference<>(charactersAdapter);
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
        if (charactersAdapterWeakReference.get() != null)
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
    }
}
