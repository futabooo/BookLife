package com.futabooo.android.booklife.screen.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.screen.booklist.BookAdapter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>
    implements View.OnClickListener {

  private Context context;
  private Elements books;

  private RecyclerView recyclerView;
  private BookAdapter.OnItemClickListener listener;

  public SearchResultAdapter(Context context, Elements books) {
    this.context = context;
    this.books = books;
  }

  @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    this.recyclerView = recyclerView;
  }

  @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);
    this.recyclerView = null;
  }

  @Override public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_result_view, parent, false);
    itemView.setOnClickListener(this);
    return new ResultViewHolder(itemView);
  }

  @Override public void onBindViewHolder(ResultViewHolder holder, int position) {
    Element book = books.get(position);
    String thumbnail = book.select("div.book_list_thumb a img").first().absUrl("src");
    Glide.with(context).load(thumbnail).into(holder.thumbnail);
    holder.title.setText(book.select("div.book_list_detail a").first().attr("title"));
  }

  @Override public int getItemCount() {
    return books.size();
  }

  @Override public void onClick(View v) {

  }

  public static class ResultViewHolder extends RecyclerView.ViewHolder {
    protected ImageView thumbnail;
    protected TextView title;

    public ResultViewHolder(View v) {
      super(v);
      thumbnail = (ImageView) v.findViewById(R.id.search_result_book_thumbnail);
      title = (TextView) v.findViewById(R.id.search_result_book_title);
    }
  }
}
