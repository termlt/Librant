package com.librant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.librant.R;
import com.librant.models.Book;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnItemClickListener listener;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(book.getImageUrl())
                .into(holder.bookCoverImage);

        holder.bookNameText.setText(book.getTitle());
        holder.bookAuthorText.setText(book.getAuthorName() + " " + book.getAuthorSurname());
        holder.bookDescription.setText(book.getDescription());

        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public List<Book> getBooks() {
        return bookList;
    }

    public void setBooks(List<Book> books) {
        bookList = books;
        notifyDataSetChanged();
    }

    public void removeBook(int position) {
        if (position >= 0 && position < bookList.size()) {
            bookList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCoverImage;
        TextView bookNameText;
        TextView bookAuthorText;
        TextView bookDescription;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCoverImage = itemView.findViewById(R.id.book_cover_image);
            bookNameText = itemView.findViewById(R.id.book_name_text);
            bookAuthorText = itemView.findViewById(R.id.book_author_text);
            bookDescription = itemView.findViewById(R.id.book_description_text);
        }
    }
}