package com.futabooo.android.booklife.screen.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.databinding.ComponentSearchResultCardViewBinding
import com.futabooo.android.booklife.model.SearchResultResource

class SearchResultAdapter(val resources: Array<SearchResultResource>, val listener: (View, Int) -> Unit)
  : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val holder = ViewHolder(
        ComponentSearchResultCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    holder.itemView.setOnClickListener {
      listener(it, resources[holder.adapterPosition].contents.book.id)
    }
    holder.binding.searchResultBookAction.setOnClickListener {
      listener(it, resources[holder.adapterPosition].contents.book.id)
    }
    return holder
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val book = resources[position].contents.book
    with(holder.binding) {
      Glide.with(root.context).load(book.imageUrl).into(searchResultBookThumbnail)
      searchResultBookTitle.text = book.title
      searchResultBookAuthor.text = book.author.name
      searchResultBookReaders.text = book.registrationCount.toString()
      val mark = resources[position].statusText
      if (mark.isNotEmpty()) {
        searchResultBookReadMark.text = mark
        searchResultBookReadMark.visibility = View.VISIBLE
      } else {
        searchResultBookReadMark.visibility = View.INVISIBLE
      }
    }
  }

  override fun getItemCount() = resources.size

  class ViewHolder(val binding: ComponentSearchResultCardViewBinding) : RecyclerView.ViewHolder(binding.root)
}
