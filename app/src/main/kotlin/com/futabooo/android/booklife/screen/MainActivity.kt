package com.futabooo.android.booklife.screen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.futabooo.android.booklife.BuildConfig
import com.futabooo.android.booklife.MainBottomMenu
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.ActivityMainBinding
import com.futabooo.android.booklife.screen.licenses.LicensesActivity
import com.futabooo.android.booklife.screen.search.SearchActivity
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {

  private val binding by lazy { DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main) }
  private lateinit var adapter: MainViewPagerAdapter

  private var toolBarHeight: Int = 0
  private val BarcodeScan = 0

  companion object {
    fun createIntent(context: Context) = Intent(context, MainActivity::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val styledAttributes = theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
    toolBarHeight = styledAttributes.getDimension(0, 0f)
        .toInt()
    styledAttributes.recycle()

    binding.apply {
      setSupportActionBar(activityMainToolbar)
      supportActionBar?.setTitle(R.string.home)

      adapter = MainViewPagerAdapter(this@MainActivity, supportFragmentManager)
      activityMainViewpager.adapter = adapter
      activityMainTabLayout.setupWithViewPager(activityMainViewpager)

      bottomBar.setOnTabSelectListener { tabId ->
        val bottomMenu = MainBottomMenu.fromPosition(bottomBar.findPositionForTabWithId(tabId))
        // PagerAdapter の更新
        adapter.bottomMenu = bottomMenu
        adapter.notifyDataSetChanged()

        // View の更新
        activityMainToolbar.setTitle(bottomMenu.titleId)
        activityMainTabLayout.visibility = bottomMenu.tabLayoutVisibility
        activityMainTabLayout.tabMode = bottomMenu.tabLayoutMode
        activityMainViewpager.setCurrentItem(0, false)
      }

      bottomBar.setOnTabReselectListener {
        // do reselected
      }

      activityMainAppBarLayout.addOnOffsetChangedListener(
          AppBarLayout.OnOffsetChangedListener { appBarLayout, _ ->
            val lp = floatingActionButton.layoutParams as FrameLayout.LayoutParams
            val fabBottomMargin = lp.bottomMargin
            val distanceToScroll = floatingActionButton.height + fabBottomMargin
            val ratio = appBarLayout.y / toolBarHeight
            floatingActionButton.translationY = -distanceToScroll * ratio
          })

      floatingActionButton.setOnClickListener {
        if (it.isSelected) hideMenu() else showMenu()
        it.isSelected = !it.isSelected
      }

      activityMainArcLayout.arcLayoutContainer.setOnTouchListener { _, event ->
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            if (binding.floatingActionButton.isSelected) hideMenu() else showMenu()
            binding.floatingActionButton.isSelected = !binding.floatingActionButton.isSelected
            return@setOnTouchListener true
          }
          else -> {
            return@setOnTouchListener true
          }
        }
      }

      activityMainArcLayout.arcLayoutMenuBarcodeScan.setOnClickListener {
        try {
          val intent = Intent("com.google.zxing.client.android.SCAN")
          intent.putExtra("SCAN_MODE", "PRODUCT_MODE") // "PRODUCT_MODE for bar codes
          startActivityForResult(intent, BarcodeScan)
        } catch (e: Exception) {
          val marketUri = Uri.parse("market://details?id=com.google.zxing.client.android")
          val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
          startActivity(marketIntent)
        }

        hideMenu()
        binding.floatingActionButton.isSelected = false

        if (!BuildConfig.DEBUG) Answers.getInstance().logCustom(CustomEvent("Barcode Scan"))
      }
      activityMainArcLayout.arcLayoutMenuSearch.setOnClickListener {
        startActivity(SearchActivity.createIntent(this@MainActivity))
        hideMenu()
        binding.floatingActionButton.isSelected = false
        if (!BuildConfig.DEBUG) Answers.getInstance().logCustom(CustomEvent("Search"))
      }
      activityMainArcLayout.arcLayoutMenuRecordVoice.setOnClickListener {
        Toast.makeText(this@MainActivity, getString(R.string.coming_soon), Toast.LENGTH_SHORT)
            .show()
        if (!BuildConfig.DEBUG) Answers.getInstance().logCustom(CustomEvent("Record Voice"))
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    binding.activityMainToolbar.inflateMenu(R.menu.activity_main_menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_licenses -> {
        startActivity(LicensesActivity.createIntent(this))
        return true
      }
      else -> {
        return super.onOptionsItemSelected(item)
      }
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == BarcodeScan) {
      when (resultCode) {
        Activity.RESULT_OK -> {
          val isbn = data?.getStringExtra("SCAN_RESULT")
          startActivity(SearchActivity.createIntent(this, isbn!!))
        }
        Activity.RESULT_CANCELED -> {
          // handle cancel
        }
      }
    }
  }

  override fun onBackPressed() {
    if (binding.floatingActionButton.isSelected) {
      hideMenu()
      binding.floatingActionButton.isSelected = false
      return
    }
    super.onBackPressed()
  }

  private fun showMenu() {
    binding.activityMainArcLayout.arcLayoutContainer.visibility = View.VISIBLE
    val arcLayout = binding.activityMainArcLayout.arcLayout
    val animList = mutableListOf<Animator>()
    var count = 0
    val len = arcLayout.childCount
    while (count < len) {
      animList.add(createShowItemAnimator(arcLayout.getChildAt(count)))
      count++
    }

    animList.add(createFABClickAnimator(45f))

    val animSet = AnimatorSet()
    animSet.duration = 400
    animSet.interpolator = OvershootInterpolator()
    animSet.playTogether(animList)
    animSet.start()
  }

  private fun hideMenu() {
    val arcLayout = binding.activityMainArcLayout.arcLayout

    val animList = (arcLayout.childCount - 1 downTo 0).map {
      createHideItemAnimator(arcLayout.getChildAt(it))
    } as MutableList
    animList.add(createFABClickAnimator(-45f))

    val animSet = AnimatorSet()
    animSet.duration = 400
    animSet.interpolator = AnticipateInterpolator()
    animSet.playTogether(animList)
    animSet.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        super.onAnimationEnd(animation)
        binding.activityMainArcLayout.arcLayoutContainer.visibility = View.INVISIBLE
      }
    })
    animSet.start()

  }

  private fun createFABClickAnimator(rotation: Float): Animator {
    val anim = ObjectAnimator.ofPropertyValuesHolder(
        binding.floatingActionButton,
        PropertyValuesHolder.ofFloat(
            "rotation", binding.floatingActionButton.rotation,
            binding.floatingActionButton.rotation + rotation
        )
    )
    return anim
  }

  private fun createShowItemAnimator(item: View): Animator {

    val dx = binding.floatingActionButton.x - item.x
    val dy = binding.floatingActionButton.y - item.y

    item.rotation = 0f
    item.translationX = dx
    item.translationY = dy

    val anim = ObjectAnimator.ofPropertyValuesHolder(
        item,
        PropertyValuesHolder.ofFloat("rotation", 0f, 720f),
        PropertyValuesHolder.ofFloat("translationX", 0f),
        PropertyValuesHolder.ofFloat("translationY", 0f)
    )

    return anim
  }

  private fun createHideItemAnimator(item: View): Animator {
    val dx = binding.floatingActionButton.x - item.x
    val dy = binding.floatingActionButton.y - item.y

    val anim = ObjectAnimator.ofPropertyValuesHolder(
        item,
        PropertyValuesHolder.ofFloat("rotation", 720f, 0f),
        PropertyValuesHolder.ofFloat("translationX", dx),
        PropertyValuesHolder.ofFloat("translationY", dy)
    )

    anim.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        super.onAnimationEnd(animation)
        item.translationX = 0f
        item.translationY = 0f
      }
    })

    return anim
  }
}
