package com.futabooo.android.booklife.screen.licenses

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityLisensesBinding

class LicensesActivity : AppCompatActivity() {

  private val binding by lazy {
    DataBindingUtil.setContentView<ActivityLisensesBinding>(this, R.layout.activity_lisenses)
  }

  companion object {

    fun createIntent(context: Context) = Intent(context, LicensesActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.apply {
      setSupportActionBar(activityLicensesToolbar)
      supportActionBar?.setTitle(R.string.licenses)
      supportActionBar?.setDisplayHomeAsUpEnabled(true)
      activityLicensesWebView.loadUrl("file:///android_asset/licenses.html")
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    when (id) {
      android.R.id.home -> {
        finish()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }
}