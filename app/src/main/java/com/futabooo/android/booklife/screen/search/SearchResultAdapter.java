package com.futabooo.android.booklife.screen.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.R;
import com.rafakob.drawme.DrawMeButton;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>
    implements View.OnClickListener {

  private Context context;
  private Elements books;

  private RecyclerView recyclerView;
  private OnCardClickListener listener;

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
    View itemView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.component_search_result_card_view, parent, false);
    itemView.setOnClickListener(this);
    itemView.findViewById(R.id.search_result_book_register).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (listener != null) {
          int position = recyclerView.getChildAdapterPosition((View)v.getParent().getParent());
          Element book = books.get(position);
          String asin = book.select("div.book_list_detail a").attr("href").substring(3);
          listener.onRegisterClick(asin);
        }
      }
    });
    return new ResultViewHolder(itemView);
  }

  @Override public void onBindViewHolder(ResultViewHolder holder, int position) {
    Element book = books.get(position);
    String thumbnail = book.select("div.book_list_thumb a img").first().absUrl("src");
    Glide.with(context).load(thumbnail).into(holder.thumbnail);
    holder.title.setText(book.select("div.book_list_detail a").first().attr("title"));
    holder.author.setText(book.select("div.book_box_book_author a").text());
    holder.readers.setText(book.select("span.readers").text());
    String mark = book.select("div.dokuryou_flag_mark").text();
    if (!TextUtils.isEmpty(mark)) {
      holder.readMark.setText(mark);
      holder.readMark.setVisibility(View.VISIBLE);
      holder.button.setText(context.getString(R.string.edit));
    } else {
      holder.readMark.setVisibility(View.INVISIBLE);
      holder.button.setText(context.getString(R.string.register));
    }
  }

  @Override public int getItemCount() {
    return books.size();
  }

  @Override public void onClick(View v) {
    if (recyclerView == null) {
      return;
    }

    if (listener != null) {
      int position = recyclerView.getChildAdapterPosition(v);
      Element book = books.get(position);
      listener.onCardClick(this, position, book);
    }
  }

  public void setOnCardClickListener(OnCardClickListener listener) {
    this.listener = listener;
  }

  public interface OnCardClickListener {
    void onCardClick(SearchResultAdapter adapter, int position, Element book);

    void onRegisterClick(String asin);
  }

  public static class ResultViewHolder extends RecyclerView.ViewHolder {
    protected ImageView thumbnail;
    protected TextView title;
    protected TextView author;
    protected TextView readers;
    protected TextView readMark;
    protected DrawMeButton button;

    public ResultViewHolder(View v) {
      super(v);
      thumbnail = (ImageView) v.findViewById(R.id.search_result_book_thumbnail);
      title = (TextView) v.findViewById(R.id.search_result_book_title);
      author = (TextView) v.findViewById(R.id.search_result_book_author);
      readers = (TextView) v.findViewById(R.id.search_result_book_readers);
      readMark = (TextView) v.findViewById(R.id.search_result_book_read_mark);
      button = (DrawMeButton) v.findViewById(R.id.search_result_book_register);
    }
  }
}
