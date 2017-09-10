package com.futabooo.android.booklife.screen.bookdetail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.futabooo.android.booklife.databinding.ItemReviewBinding
import com.futabooo.android.booklife.model.Review
import jp.wasabeef.glide.transformations.CropCircleTransformation

class BookReviewAdapter(var reviews: MutableList<Review>) : RecyclerView.Adapter<BookReviewAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
      ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val review = reviews[position]
    holder.binding.apply {
      Glide.with(root.context).load(review.user.image)
          .bitmapTransform(CropCircleTransformation(root.context))
          .into(itemReviewIcon)
      itemReviewName.text = review.user.name
      itemReviewReview.text = review.content
    }
  }

  override fun getItemCount() = reviews.size

  class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)
}