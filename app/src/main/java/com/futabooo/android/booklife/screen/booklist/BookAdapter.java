package com.futabooo.android.booklife.screen.booklist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.model.Book;
import com.futabooo.android.booklife.model.Resource;
import java.util.Arrays;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> implements View.OnClickListener {

  private Context context;
  private List<Resource> resources;

  private RecyclerView recyclerView;
  private OnItemClickListener listener;

  public BookAdapter(Context context, Resource[] resources) {
    this.context = context;
    this.resources = Arrays.asList(resources);
  }

  @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    this.recyclerView = recyclerView;
  }

  @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);
    this.recyclerView = null;
  }

  @Override public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_book_card_view, parent, false);
    itemView.setOnClickListener(this);
    return new BookViewHolder(itemView);
  }

  @Override public void onBindViewHolder(BookViewHolder holder, int position) {
    Book book = resources.get(position).getBook();
    String thumbnail = book.getImageUrl();
    Glide.with(context).load(thumbnail)
        //.bitmapTransform(new CropCircleTransformation(()))
        .into(holder.thumbnail);

    holder.title.setText(book.getTitle());
  }

  @Override public int getItemCount() {
    return resources.size();
  }

  @Override public void onClick(View view) {
    if (recyclerView == null) {
      return;
    }

    if (listener != null) {
      int position = recyclerView.getChildAdapterPosition(view);
      Book book = resources.get(position).getBook();
      listener.onItemClick(this, position, book);
    }
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  public interface OnItemClickListener {
    void onItemClick(BookAdapter adapter, int position, Book book);
  }

  public static class BookViewHolder extends RecyclerView.ViewHolder {
    protected ImageView thumbnail;
    protected TextView title;

    public BookViewHolder(View v) {
      super(v);
      thumbnail = (ImageView) v.findViewById(R.id.book_thumbnail);
      title = (TextView) v.findViewById(R.id.book_title);
    }
  }
}
