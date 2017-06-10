package com.futabooo.android.booklife.screen.home;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.FragmentHomeBinding;
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
import org.jsoup.select.Elements;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {

  @Inject Retrofit retrofit;
  @Inject SharedPreferences sharedPreferences;

  private FragmentHomeBinding binding;

  public HomeFragment() {
  }

  public static HomeFragment newInstance() {
    Bundle args = new Bundle();
    //args.putString(key, value);
    HomeFragment f = new HomeFragment();
    f.setArguments(args);

    return f;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getActivity().getApplication()).getNetComponent().inject(this);
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
    return binding.getRoot();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

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

            // user_idが保存されていない場合は取得して保存する
            if(!sharedPreferences.contains("user_id")){
              String userId = Jsoup.parse(result.toString()).select("div.home_index__userdata__side a").attr("href").toString().substring(7);
              SharedPreferences.Editor editor = sharedPreferences.edit();
              editor.putString("user_id", userId);
              editor.apply();
            }

            String iconUrl =
                Jsoup.parse(result.toString()).select("div.home_index__userdata__side a img").attr("src");
            Glide.with(HomeFragment.this)
                .load(iconUrl)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .into(binding.icon);


            Elements readingVolumes = Jsoup.parse(result.toString()).select("div.home_index__userdata__main section.home_index__userdata__reading-volume");

            Element thisVolume = readingVolumes.get(0);
            binding.readingVolumeThisMonthTitle.setText(thisVolume.select(".home_index__userdata__reading-volume__title").text());
            binding.readingPageThisMonth.setText(getString(R.string.reading_page, thisVolume.select(".home_index__userdata__reading-volume__count").get(0).text()));
            binding.readingVolumeThisMonth.setText(getString(R.string.reading_volume, thisVolume.select(".home_index__userdata__reading-volume__count").get(1).text()));
            binding.readingPageParDayThisMonth.setText(getString(R.string.reading_page_par_day, thisVolume.select(".home_index__userdata__reading-volume__count").get(2).text()));

            Element lastMonthReadingVolume = readingVolumes.get(1);
            binding.readingVolumeLastMonthTitle.setText(lastMonthReadingVolume.select(".home_index__userdata__reading-volume__title").text());
            binding.readingPageLastMonth.setText(getString(R.string.reading_page, lastMonthReadingVolume.select(".home_index__userdata__reading-volume__count").get(0).text()));
            binding.readingVolumeLastMonth.setText(getString(R.string.reading_volume, lastMonthReadingVolume.select(".home_index__userdata__reading-volume__count").get(1).text()));
            binding.readingPageParDayLastMonth.setText(getString(R.string.reading_page_par_day, lastMonthReadingVolume.select(".home_index__userdata__reading-volume__count").get(2).text()));
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onComplete() {

          }
        });
  }
}
