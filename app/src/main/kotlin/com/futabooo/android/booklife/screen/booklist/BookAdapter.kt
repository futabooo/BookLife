package com.futabooo.android.booklife.screen.booklist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.databinding.ComponentBookCardViewBinding
import com.futabooo.android.booklife.model.Book
import com.futabooo.android.booklife.model.Resource

class BookAdapter(val resources: Array<Resource>, val listener: (Book) -> Unit)
  : RecyclerView.Adapter<BookAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val holder = ViewHolder(ComponentBookCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    holder.itemView.setOnClickListener { resources[holder.adapterPosition].book?.let { listener(it) } }
    return holder
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val book = resources[position].book
    Glide.with(holder.itemView.context).load(book?.imageUrl).into(holder.binding.bookThumbnail)
    holder.binding.bookTitle.text = book?.title
  }

  override fun getItemCount() = resources.size

  class ViewHolder(val binding: ComponentBookCardViewBinding) : RecyclerView.ViewHolder(binding.root)

}
