package com.futabooo.android.booklife.screen.booklist

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ComponentBookCardViewBinding
import com.futabooo.android.booklife.databinding.ComponentLoadingBinding
import com.futabooo.android.booklife.model.Book
import com.futabooo.android.booklife.model.Resource

class BookAdapter(var resources: MutableList<Resource>,
                  val listener: (View, Book) -> Unit) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {

  companion object {
    const val LOADING = 0
    const val CONTENTS = 1
  }

  private var showProgress = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    if (viewType == BookAdapter.LOADING) {
      return ViewHolder(ComponentLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    } else {
      return ViewHolder(
          ComponentBookCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)).apply {
        itemView.setOnClickListener { v -> resources[adapterPosition].book?.let { listener(v.findViewById(R.id.book_thumbnail), it) } }
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
      is ComponentBookCardViewBinding -> {
        val book = resources[position].book
        with(holder.binding) {
          Glide.with(root.context).load(book?.imageUrl).into(bookThumbnail)
          bookTitle.text = book?.title
        }
      }
    }

  }

  override fun getItemCount() = if (resources.size == 0) 0 else resources.size + 1

  override fun getItemId(position: Int) =
      if (position != 0 && position == itemCount - 1) -1 else super.getItemId(position)

  override fun getItemViewType(position: Int) =
      if (position != 0 && position == itemCount - 1) LOADING else CONTENTS

  fun addAll(list: MutableList<Resource>) {
    val addIndex = if (resources.size == 0) 0 else resources.size
    resources.addAll(addIndex, list)
    notifyItemRangeInserted(addIndex, list.size)
  }

  fun showProgress(show: Boolean) {
    showProgress = show
    notifyItemChanged(resources.size)
  }

  class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

}
