package com.futabooo.android.booklife.screen

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.futabooo.android.booklife.MainBottomMenu
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityMainBinding
import com.futabooo.android.booklife.screen.search.SearchActivity
import com.roughike.bottombar.OnTabReselectListener
import com.roughike.bottombar.OnTabSelectListener

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var adapter: MainViewPagerAdapter

  companion object {
    fun createIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

    with(binding) {
      setSupportActionBar(activityMainToolbar)
      supportActionBar?.setTitle(R.string.home)

      adapter = MainViewPagerAdapter(this@MainActivity, supportFragmentManager)
      activityMainViewpager.adapter = adapter
      activityMainTabLayout.setupWithViewPager(activityMainViewpager)

      bottomBar.setOnTabSelectListener { tabId ->
        val bottomMenu = MainBottomMenu.fromPosition(bottomBar.findPositionForTabWithId(tabId))
        // PagerAdapter の更新
        adapter.bottomMenu = bottomMenu!!
        adapter.notifyDataSetChanged()

        // View の更新
        activityMainToolbar.setTitle(bottomMenu.titleId)
        activityMainTabLayout.visibility = bottomMenu!!.tabLayoutVisibility
        activityMainTabLayout.tabMode = bottomMenu!!.tabLayoutMode
        activityMainViewpager.setCurrentItem(0, false)
      }

      bottomBar.setOnTabReselectListener {
        // do reselected
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    binding.activityMainToolbar.inflateMenu(R.menu.activity_main_menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    if (id == R.id.menu_search) {
      startActivity(SearchActivity.createIntent(this))
      return true
    }

    return super.onOptionsItemSelected(item)
  }
}
