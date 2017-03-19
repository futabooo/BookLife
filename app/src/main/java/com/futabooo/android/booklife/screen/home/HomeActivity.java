package com.futabooo.android.booklife.screen.home;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityHomeBinding;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.inject.Inject;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import retrofit2.Retrofit;

public class HomeActivity extends AppCompatActivity {

  @Inject Retrofit retrofit;

  private ActivityHomeBinding binding;

  public static Intent createIntent(Context context) {
    return new Intent(context, HomeActivity.class);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

    ((BookLife) getApplication()).getNetComponent().inject(this);

    Observable<ResponseBody> observable = retrofit.create(HomeService.class).home();
    observable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<ResponseBody>() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onNext(ResponseBody value) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(value.byteStream()));
            StringBuffer result = null;
            try {
              result = new StringBuffer();
              String line;
              while ((line = reader.readLine()) != null) {
                result.append(line);
              }
            } catch (IOException e) {
              e.printStackTrace();
            }
            String icon = Jsoup.parse(result.toString()).select("div.default_box a[href^=/u/] div").get(0).attr("style");
            Glide.with(HomeActivity.this)
                .load(icon.substring(icon.indexOf("http://"), icon.indexOf(")")))
                .bitmapTransform(new CropCircleTransformation(HomeActivity.this))
                .into(binding.icon);

            Element readingVolume = Jsoup.parse(result.toString()).select("div.default_box [style=font-size:12px;line-height:22px;font-weight:bold;color:#808080;]").get(0).parent();
            binding.readingVolumeThisMonthTitle.setText(readingVolume.child(0).ownText());
            binding.readingPageThisMonth.setText(getString(R.string.reading_page, readingVolume.child(1).ownText()));
            binding.readingVolumeThisMonth.setText(getString(R.string.reading_volume, readingVolume.child(2).ownText()));
            binding.readingPageParDayThisMonth.setText(getString(R.string.reading_page_par_day, readingVolume.child(3).ownText()));

            Element lastMonthReadingVolume = Jsoup.parse(result.toString()).select("div.default_box [style=font-size:12px;line-height:22px;font-weight:bold;color:#808080;]").get(1).parent();
            binding.readingVolumeLastMonthTitle.setText(lastMonthReadingVolume.child(0).ownText());
            binding.readingPageLastMonth.setText(getString(R.string.reading_page, lastMonthReadingVolume.child(1).ownText()));
            binding.readingVolumeLastMonth.setText(getString(R.string.reading_volume, lastMonthReadingVolume.child(2).ownText()));
            binding.readingPageParDayLastMonth.setText(getString(R.string.reading_page_par_day, lastMonthReadingVolume.child(3).ownText()));
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onComplete() {

          }
        });
  }
}
