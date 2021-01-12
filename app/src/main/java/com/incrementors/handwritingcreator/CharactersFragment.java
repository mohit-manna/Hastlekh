package com.incrementors.handwritingcreator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

public class CharactersFragment extends Fragment implements View.OnClickListener {
    final int CHOOSERCODE = 100;
    TextInputEditText noteBox;
    TextInputLayout noteBoxLayout;
    File appDir;
    TextView preview;
    LinearLayout colorPalette;
    FloatingActionButton saveNote, chooseNote, editNote, previewNote;
    AppCompatImageButton green, red, blue, grey, yellow, white;
    String path;
    String TAG = "Characters Fragment";
    int colorcode = R.color.colorWhite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_characters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
        getActivity().getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        noteBox = view.findViewById(R.id.noteBox);
        preview = view.findViewById(R.id.preview);
        saveNote = view.findViewById(R.id.saveNote);
        editNote = view.findViewById(R.id.editNote);
        chooseNote = view.findViewById(R.id.chooseNote);
        previewNote = view.findViewById(R.id.previewNote);
        noteBoxLayout = view.findViewById(R.id.noteLayout);
        colorPalette = view.findViewById(R.id.colorPalette);

        red = view.findViewById(R.id.red);
        blue = view.findViewById(R.id.blue);
        green = view.findViewById(R.id.green);
        white = view.findViewById(R.id.white);
        yellow = view.findViewById(R.id.yellow);
        grey = view.findViewById(R.id.lightGrey);

        red.setOnClickListener(this);
        blue.setOnClickListener(this);
        green.setOnClickListener(this);
        white.setOnClickListener(this);
        yellow.setOnClickListener(this);
        grey.setOnClickListener(this);
        editNote.setOnClickListener(this);
        previewNote.setOnClickListener(this);

        chooseNote.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Choose Note"), CHOOSERCODE);
        });

        appDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));

        saveNote.setOnClickListener(v -> {
            //SpannableString note = SpannableString.valueOf(noteBox.getText());
            String note = preview.getText().toString().trim();
            if (!note.isEmpty()) {
//                noteBoxLayout.setVisibility(View.GONE);
//                preview.setVisibility(View.VISIBLE);
//                SpannableString finalNote = convertNote(note);
//                preview.setText(finalNote);


                File newdir = new File(Environment.getExternalStorageDirectory(), "SavedNote");
                if (!newdir.exists())
                    if (!newdir.mkdirs())
                        Log.d("File creation", "failed to create directory");
                    else
                        Log.d("File creation", "file created successfully");

                path = Environment.getExternalStorageDirectory().toString() + "/" + "SavedNote";
                File file = new File(path);
                if (file.exists()) {
                    Date currentTime = Calendar.getInstance().getTime();
                    try {
                        String noteName = "note_" + currentTime.getMinutes() + currentTime.getSeconds() + ".webp";
                        preview.setDrawingCacheEnabled(true);
                        preview.buildDrawingCache(true);
                        preview.setDrawingCacheBackgroundColor(getResources().getColor(colorcode));
                        Bitmap image = Bitmap.createBitmap(preview.getDrawingCache());

                        File imageFile = new File(file, noteName);
                        FileOutputStream fos = new FileOutputStream(imageFile);

                        image.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                        fos.close();

                        Toast.makeText(getActivity(), "File saved at location " + file.getPath(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.i("Final note", e.getMessage());
                    }
                } //else
                //Log.i("Note file", "File not exits");
            } else {
                Toast.makeText(getActivity(), "Empty note", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSERCODE:
                noteBoxLayout.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);
                saveNote.setVisibility(View.VISIBLE);
                chooseNote.setVisibility(View.GONE);
                editNote.setVisibility(View.VISIBLE);
                previewNote.setVisibility(View.GONE);
                colorPalette.setVisibility(View.VISIBLE);

                Uri uri = data.getData().normalizeScheme();
                Log.d(TAG, "File Uri: " + uri.getPath());
                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String docNote = "";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        docNote += line;
                    }
                    Log.i(TAG, "onActivityResult: " + docNote);
                    preview.setText(convertNote(docNote), TextView.BufferType.SPANNABLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    SpannableString convertNote(String note) {
        SpannableString ss = new SpannableString(note);
        if (appDir.exists()) {
            path = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
            File fontDir = new File(path);
            File[] fontFiles = fontDir.listFiles();
            //Log.i("Files", Arrays.toString(fontFiles));
            if (ss.length() > 0) {
                //Log.i("SS", ss.toString());
                for (int i = 0; i < ss.length(); i++) {
                    for (File file : fontFiles) {
                        if (String.valueOf((char) Integer.parseInt(file.getName().split(Pattern.quote("."))[0])).equals(String.valueOf(ss.charAt(i)))) {
                            //Log.i("File name", file.getName());
                            //Log.i("Index : character", i + ":" + ss.charAt(i));
                            Bitmap b = BitmapFactory.decodeFile(file.getPath());
                            Bitmap newB = Bitmap.createScaledBitmap(b, 35, 38, true);
                            Drawable dr = new BitmapDrawable(getResources(), newB);
                            dr.setBounds(0, 0, newB.getWidth(), newB.getHeight());
                            ss.setSpan(new ImageSpan(dr, DynamicDrawableSpan.ALIGN_BOTTOM), i, i + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        } else if (ss.charAt(i) == ' ') {
                            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                            Bitmap newB = Bitmap.createBitmap(50, 45, conf);
                            Drawable dr = new BitmapDrawable(getResources(), newB);
                            dr.setBounds(0, 0, newB.getWidth(), newB.getHeight());
                            ss.setSpan(new ImageSpan(dr, DynamicDrawableSpan.ALIGN_BOTTOM), i, i + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        //else
//                            Log.i("Else part", "not found");
                    }
                }
            }
        }
        return ss;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.red:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorRed));
                preview.setBackgroundColor(getResources().getColor(R.color.colorRed));
                colorcode = R.color.colorRed;
                break;
            case R.id.white:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
                preview.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                colorcode = R.color.colorWhite;
                break;
            case R.id.blue:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                preview.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                colorcode = R.color.colorPrimary;
                break;
            case R.id.yellow:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorYellow));
                preview.setBackgroundColor(getResources().getColor(R.color.colorYellow));
                colorcode = R.color.colorYellow;
                break;
            case R.id.green:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorGreen));
                preview.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                colorcode = R.color.colorGreen;
                break;

            case R.id.lightGrey:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorLightGray));
                preview.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
                colorcode = R.color.colorLightGray;
                break;
            case R.id.previewNote:
                String note = noteBox.getText().toString().trim();
                if (!note.isEmpty()) {
                    noteBoxLayout.setVisibility(View.GONE);
                    chooseNote.setVisibility(View.GONE);
                    preview.setVisibility(View.VISIBLE);
                    saveNote.setVisibility(View.VISIBLE);
                    editNote.setVisibility(View.VISIBLE);
                    previewNote.setVisibility(View.GONE);
                    colorPalette.setVisibility(View.VISIBLE);
                    SpannableString finalNote = convertNote(note);
                    preview.setText(finalNote);
                } else {
                    Toast.makeText(getActivity(), "Empty Note", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.editNote:
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite));
                noteBoxLayout.setVisibility(View.VISIBLE);
                chooseNote.setVisibility(View.VISIBLE);
                preview.setVisibility(View.GONE);
                saveNote.setVisibility(View.GONE);
                editNote.setVisibility(View.GONE);
                previewNote.setVisibility(View.VISIBLE);
                colorPalette.setVisibility(View.GONE);
                String previewNote = preview.getText().toString().trim();
                if (!previewNote.isEmpty()) {
                    noteBox.setText(previewNote);
                }
                break;
        }
    }
}