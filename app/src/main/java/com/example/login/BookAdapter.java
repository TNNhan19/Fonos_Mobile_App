package com.example.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_GRID = 1;

    public interface OnBookActionListener {
        void onPlay(Book book);
        void onFavorite(Book book);
        void onDownload(Book book);
        void onEdit(Book book);
        void onDelete(Book book);
    }

    private List<Book> books;
    private boolean isGrid;
    private OnBookActionListener listener;

    public BookAdapter(List<Book> books, boolean isGrid) {
        this.books = books;
        this.isGrid = isGrid;
    }

    public void setOnBookActionListener(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void setGridMode(boolean isGrid) {
        this.isGrid = isGrid;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isGrid ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_GRID
                ? R.layout.item_book_grid
                : R.layout.item_book_list;

        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        if (holder.tvCover != null) {
            holder.tvCover.setText(makeCoverText(book.getTitle()));
        }
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor() + " · " + book.getYear());
        holder.tvGenre.setText(book.getType() + " · " + book.getGenre());

        if (holder.btnFavorite != null) {
            holder.btnFavorite.setText(book.isFavorite() ? "♥" : "♡");
        }

        if (holder.btnDownload != null) {
            holder.btnDownload.setText(book.isDownloaded() ? "✅" : "⬇️");
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onPlay(book));

            if (holder.btnPlay != null) {
                holder.btnPlay.setOnClickListener(v -> listener.onPlay(book));
            }

            if (holder.btnFavorite != null) {
                holder.btnFavorite.setOnClickListener(v -> listener.onFavorite(book));
            }

            if (holder.btnDownload != null) {
                holder.btnDownload.setOnClickListener(v -> listener.onDownload(book));
            }

            if (holder.btnEdit != null) {
                holder.btnEdit.setOnClickListener(v -> listener.onEdit(book));
            }

            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> listener.onDelete(book));
            }
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    private String makeCoverText(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "BOOK";
        }

        String[] words = title.trim().split("\\s+");

        if (words.length == 1) {
            return words[0].toUpperCase();
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < Math.min(words.length, 3); i++) {
            builder.append(words[i].toUpperCase());

            if (i < Math.min(words.length, 3) - 1) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvCover, tvTitle, tvAuthor, tvGenre;
        TextView btnPlay, btnFavorite, btnDownload, btnEdit, btnDelete;

        BookViewHolder(View v) {
            super(v);

            tvCover = v.findViewById(R.id.tvCover);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvGenre = v.findViewById(R.id.tvGenre);

            btnPlay = v.findViewById(R.id.btnPlay);
            btnFavorite = v.findViewById(R.id.btnFavorite);
            btnDownload = v.findViewById(R.id.btnDownload);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}