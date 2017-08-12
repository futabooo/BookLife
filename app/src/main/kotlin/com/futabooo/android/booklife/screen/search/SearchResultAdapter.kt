package com.futabooo.android.booklife.screen.search

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.databinding.ComponentLoadingBinding
import com.futabooo.android.booklife.databinding.ComponentSearchResultCardViewBinding
import com.futabooo.android.booklife.model.SearchResultResource

class SearchResultAdapter(val resources: MutableList<SearchResultResource>, val listener: (View, Int) -> Unit)
  : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

  companion object {
    const val LOADING = 0
    const val CONTENTS = 1
  }

  private var showProgress = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    if (viewType == LOADING) {
      return ViewHolder(ComponentLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    } else {
      return ViewHolder(
          ComponentSearchResultCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
          .apply {
            binding as ComponentSearchResultCardViewBinding
            itemView.setOnClickListener { listener(it, resources[adapterPosition].contents.book.id) }
            binding.searchResultBookAction.setOnClickListener {
              listener(it, resources[adapterPosition].contents.book.id)
            }
          }
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder.binding) {
      is ComponentLoadingBinding -> {
        with(holder.binding) {
          if (showProgress) progressBar.visibility = View.VISIBLE else progressBar.visibility = View.GONE
        }
      }
      is ComponentSearchResultCardViewBinding -> {
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
    }
  }

  override fun getItemCount() = if (resources.size == 0) 0 else resources.size + 1

  override fun getItemId(position: Int) =
      if (position != 0 && position == itemCount - 1) -1 else super.getItemId(position)

  override fun getItemViewType(position: Int) =
      if (position != 0 && position == itemCount - 1) LOADING else CONTENTS


  fun addAll(list: MutableList<SearchResultResource>) {
    val addIndex = if (resources.lastIndex < 0) 0 else resources.lastIndex + 1
    resources.addAll(addIndex, list)
    notifyItemRangeInserted(addIndex, list.size)
  }

  fun showProgress(show: Boolean) {
    showProgress = show
    notifyItemChanged(resources.size)
  }

  class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}
