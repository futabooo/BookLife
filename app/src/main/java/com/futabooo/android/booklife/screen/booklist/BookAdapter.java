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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

  private Context context;
  private Elements books;

  public BookAdapter(Context context, Elements books) {
    this.context = context;
    this.books = books;
  }

  @Override public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_book_card_view, parent, false);
    return new BookViewHolder(itemView);
  }

  @Override public void onBindViewHolder(BookViewHolder holder, int position) {
    Element book = books.get(position);
    String thumbnail = book.select("div.book_list_thumb a img").first().absUrl("src");
    Glide.with(context).load(thumbnail)
        //.bitmapTransform(new CropCircleTransformation(()))
        .into(holder.thumbnail);

    holder.title.setText(book.select("div.book_list_detail a").first().attr("title"));
  }

  @Override public int getItemCount() {
    return books.size();
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
