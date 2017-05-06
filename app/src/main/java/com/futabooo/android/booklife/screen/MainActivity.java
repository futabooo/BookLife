package com.futabooo.android.booklife.screen;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityMainBinding;
import com.futabooo.android.booklife.screen.booklist.BookListFragment;
import com.futabooo.android.booklife.screen.home.HomeFragment;
import com.futabooo.android.booklife.screen.search.SearchActivity;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  public static Intent createIntent(Context context) {
    return new Intent(context, MainActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    setSupportActionBar(binding.activityMainToolbar);

    binding.bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
      @Override public void onTabSelected(@IdRes int tabId) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (tabId == R.id.tab_home) {
          binding.activityMainToolbar.setTitle("Home");
          HomeFragment homeFragment = HomeFragment.newInstance();
          fragmentTransaction.replace(binding.contentContainer.getId(), homeFragment);
          fragmentTransaction.commit();
        }
        if (tabId == R.id.tab_book) {
          binding.activityMainToolbar.setTitle("Book");
          BookListFragment bookListFragment = BookListFragment.newInstance();
          fragmentTransaction.replace(binding.contentContainer.getId(), bookListFragment);
          fragmentTransaction.commit();
        }
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
