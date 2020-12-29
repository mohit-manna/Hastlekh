package com.incrementors.handwritingcreator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

public class CharactersFragment extends Fragment {
    TextInputEditText noteBox;
    TextInputLayout noteBoxLayout;
    File appDir;
    TextView preview;
    FloatingActionButton saveNote;
    String path;
    SpannableStringBuilder sb;

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
        saveNote = view.findViewById(R.id.saveNote);
        preview = view.findViewById(R.id.preview);
        noteBoxLayout = view.findViewById(R.id.noteLayout);

        appDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        sb = new SpannableStringBuilder();

        saveNote.setOnClickListener(v -> {
            //SpannableString note = SpannableString.valueOf(noteBox.getText());
            String note = noteBox.getText().toString();
            noteBoxLayout.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);
            preview.setText(convertNote(note));
        });
    }


    SpannableString convertNote(String note) {
        SpannableString ss = new SpannableString(note);
        if (appDir.exists()) {
            path = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);
            File fontDir = new File(path);
            File[] fontFiles = fontDir.listFiles();
            Log.i("Files", Arrays.toString(fontFiles));
            if (ss.length() > 0) {
                Log.i("SS", "afterTextChanged: " + ss);
                for (int i = 0; i < ss.length(); i++) {
                    for (File file : fontFiles) {
                        if (String.valueOf((char) Integer.parseInt(file.getName().split(Pattern.quote("."))[0])).equals(String.valueOf(ss.charAt(i)))) {
                            Log.i("File name", file.getName());
                            Log.i("Index : character", i + ":" + ss.charAt(i));
                            Bitmap b = BitmapFactory.decodeFile(file.getPath());
                            Bitmap newB = Bitmap.createScaledBitmap(b, 45, 45, true);
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

}