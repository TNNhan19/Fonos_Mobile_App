package com.example.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SectionBookAdapter extends RecyclerView.Adapter<SectionBookAdapter.SectionBookViewHolder> {

    private List<Book> books;

    public SectionBookAdapter(List<Book> books) {
        this.books = books;
    }

    @NonNull
    @Override
    public SectionBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section_book, parent, false);
        return new SectionBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionBookViewHolder holder, int position) {
        Book book = books.get(position);

        holder.tvCover.setText(makeCoverText(book.getTitle()));
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvMeta.setText(
                "★ " + String.format(java.util.Locale.US, "%.1f", book.getRating())
                        + " · " + book.getGenre()
        );
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
    public interface OnSectionBookClickListener {
        void onBookClick(Book book);
    }
    private OnSectionBookClickListener listener;
    public void setOnSectionBookClickListener(OnSectionBookClickListener listener) {
        this.listener = listener;
    }

    private String makeCoverText(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "BOOK";
        }

        String[] words = title.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();

        int limit = Math.min(words.length, 3);

        for (int i = 0; i < limit; i++) {
            builder.append(words[i].toUpperCase());

            if (i < limit - 1) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    static class SectionBookViewHolder extends RecyclerView.ViewHolder {
        TextView tvCover, tvTitle, tvAuthor, tvMeta;

        SectionBookViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCover = itemView.findViewById(R.id.tvCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}