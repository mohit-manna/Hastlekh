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
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.CharacterViewHolder> {
    List<File> characters;
    Context context;
    CharacterViewHolder holder;
    ItemClickListener clickListener;

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
        String[] nm = characters.get(position).getName().split(Pattern.quote("."));
        //Log.i("character name", Arrays.toString(nm));
        holder.characterName.setText(String.valueOf((char) Integer.parseInt(nm[0])));
        //Log.i("character name", String.valueOf((char) Integer.parseInt(nm[0])));
        String path = characters.get(position).getAbsolutePath();
        Glide.with(context)
                .asBitmap()
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.characterImage);
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }


    public class CharacterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView characterImage;
        TextView characterName;

        public CharacterViewHolder(@NonNull View view) {
            super(view);
            characterImage = view.findViewById(R.id.characterImage);
            characterName = view.findViewById(R.id.characterName);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) clickListener.onClick(v, getAdapterPosition());

        }
    }
}
