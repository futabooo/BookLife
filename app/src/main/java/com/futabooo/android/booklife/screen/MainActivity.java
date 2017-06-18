package com.futabooo.android.booklife.screen;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.futabooo.android.booklife.MainBottomMenu;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityMainBinding;
import com.futabooo.android.booklife.screen.search.SearchActivity;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  private MainViewPagerAdapter adapter;

  public static Intent createIntent(Context context) {
    return new Intent(context, MainActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    setSupportActionBar(binding.activityMainToolbar);
    getSupportActionBar().setTitle(R.string.home);

    adapter = new MainViewPagerAdapter(MainActivity.this, getSupportFragmentManager());
    binding.activityMainViewpager.setAdapter(adapter);
    binding.activityMainTabLayout.setupWithViewPager(binding.activityMainViewpager);

    binding.bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
      @Override public void onTabSelected(@IdRes int tabId) {
        MainBottomMenu bottomMenu = MainBottomMenu.fromPosition(binding.bottomBar.findPositionForTabWithId(tabId));
        // PagerAdapter の更新
        adapter.setBottomMenu(bottomMenu);
        adapter.notifyDataSetChanged();

        // View の更新
        binding.activityMainToolbar.setTitle(bottomMenu.getTitleId());
        binding.activityMainTabLayout.setVisibility(bottomMenu.getTabLayoutVisibility());
        binding.activityMainTabLayout.setTabMode(bottomMenu.getTabLayoutMode());
        binding.activityMainViewpager.setCurrentItem(0, false);
      }
    });

    binding.bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
      @Override public void onTabReSelected(@IdRes int tabId) {
        // do reselected
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    binding.activityMainToolbar.inflateMenu(R.menu.activity_main_menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();

    if (id == R.id.menu_search) {
      startActivity(SearchActivity.createIntent(this));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
