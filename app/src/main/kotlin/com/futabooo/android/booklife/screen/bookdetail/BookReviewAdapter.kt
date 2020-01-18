package com.futabooo.android.booklife.screen.bookdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.databinding.ItemReviewBinding
import com.futabooo.android.booklife.model.Review

class BookReviewAdapter(var reviews: MutableList<Review>) : RecyclerView.Adapter<BookReviewAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
      ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val review = reviews[position]
    holder.binding.apply {
      Glide.with(root.context).load(review.user.image)
          .circleCrop()
          .into(itemReviewIcon)
      itemReviewName.text = review.user.name
      itemReviewReview.text = review.content

      if (review.netabare.netabare) itemReviewNetabare.visibility = View.VISIBLE else View.GONE

    }

  }

  override fun getItemCount() = reviews.size

  class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)
}