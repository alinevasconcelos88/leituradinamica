package com.example.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.LetterItem;

import java.util.List;

public class LetterAdapter extends RecyclerView.Adapter<LetterAdapter.LetterViewHolder> {

    public interface OnLetterClickListener {
        void onLetterClick(LetterItem item);
    }

    private final List<LetterItem> letters;
    private final OnLetterClickListener listener;

    public LetterAdapter(List<LetterItem> letters, OnLetterClickListener listener) {
        this.letters = letters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LetterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letter_grid, parent, false);
        return new LetterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LetterViewHolder holder, int position) {
        LetterItem item = letters.get(position);
        holder.tvSymbol.setText(item.getLetter());
        holder.tvExample.setText(item.getWordsSummary());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLetterClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return letters.size();
    }

    static class LetterViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol;
        TextView tvExample;

        public LetterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.tv_letter_symbol);
            tvExample = itemView.findViewById(R.id.tv_letter_example);
        }
    }
}
