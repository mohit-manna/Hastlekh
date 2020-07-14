package com.incrementors.handwritingcreator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.CharacterViewHolder> {
    List<File> characters;
    Context context;
    CharacterViewHolder holder;

    public CharactersAdapter(Context context, List<File> characters) {
        this.context = context;
        this.characters = characters;
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.characters_view, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CharacterViewHolder holder, final int position) {
        holder.characterName.setText(String.valueOf(characters.get(position).getName().charAt(0)));
        String path = characters.get(position).getAbsolutePath();
        Glide.with(context)
                .asBitmap()
                .load(path)
                .into(holder.characterImage);
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }

    public class CharacterViewHolder extends RecyclerView.ViewHolder {
        ImageView characterImage;
        TextView characterName;

        public CharacterViewHolder(@NonNull View view) {
            super(view);
            characterImage = view.findViewById(R.id.characterImage);
            characterName = view.findViewById(R.id.characterName);
        }
    }
}
