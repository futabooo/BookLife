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
import com.futabooo.android.booklife.model.Book;
import com.futabooo.android.booklife.model.SearchResultResource;
import java.util.Arrays;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>
    implements View.OnClickListener {

  private Context context;
  private List<SearchResultResource> resource;

  private RecyclerView recyclerView;
  private OnCardClickListener listener;

  public SearchResultAdapter(Context context, SearchResultResource[] searchResultResources) {
    this.context = context;
    this.resource = Arrays.asList(searchResultResources);
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
    itemView.findViewById(R.id.search_result_book_action).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (listener != null) {
          int position = recyclerView.getChildAdapterPosition((View) v.getParent().getParent());
          Book book = resource.get(position).getContents().getBook();
          listener.onRegisterClick(book.getId());
        }
      }
    });
    return new ResultViewHolder(itemView);
  }

  @Override public void onBindViewHolder(ResultViewHolder holder, int position) {
    Book book = resource.get(position).getContents().getBook();
    String thumbnail = book.getImageUrl();
    Glide.with(context).load(thumbnail).into(holder.thumbnail);
    holder.title.setText(book.getTitle());
    holder.author.setText(book.getAuthor().getName());
    holder.readers.setText(Integer.toString(book.getRegistrationCount()));
    String mark = resource.get(position).getStatusText();
    if (!TextUtils.isEmpty(mark)) {
      holder.readMark.setText(mark);
      holder.readMark.setVisibility(View.VISIBLE);
    } else {
      holder.readMark.setVisibility(View.INVISIBLE);
    }
  }

  @Override public int getItemCount() {
    return resource.size();
  }

  @Override public void onClick(View v) {
    if (recyclerView == null) {
      return;
    }

    if (listener != null) {
      int position = recyclerView.getChildAdapterPosition(v);
      Book book = resource.get(position).getContents().getBook();
      listener.onCardClick(this, position, book);
    }
  }

  public void setOnCardClickListener(OnCardClickListener listener) {
    this.listener = listener;
  }

  public interface OnCardClickListener {
    void onCardClick(SearchResultAdapter adapter, int position, Book book);

    void onRegisterClick(int bookId);
  }

  public static class ResultViewHolder extends RecyclerView.ViewHolder {
    protected ImageView thumbnail;
    protected TextView title;
    protected TextView author;
    protected TextView readers;
    protected TextView readMark;
    protected ImageView button;

    public ResultViewHolder(View v) {
      super(v);
      thumbnail = (ImageView) v.findViewById(R.id.search_result_book_thumbnail);
      title = (TextView) v.findViewById(R.id.search_result_book_title);
      author = (TextView) v.findViewById(R.id.search_result_book_author);
      readers = (TextView) v.findViewById(R.id.search_result_book_readers);
      readMark = (TextView) v.findViewById(R.id.search_result_book_read_mark);
      button = (ImageView) v.findViewById(R.id.search_result_book_action);
    }
  }
}
