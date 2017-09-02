package com.futabooo.android.booklife.screen.option

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityOptionBinding

class OptionActivity : AppCompatActivity() {

  private val binding by lazy { DataBindingUtil.setContentView<ActivityOptionBinding>(this, R.layout.activity_option) }

  companion object {

    fun createIntent(context: Context) = Intent(context, OptionActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.apply {
      setSupportActionBar(activityOptionToolbar)
      supportActionBar?.setTitle(R.string.settings)
      supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
  }
}