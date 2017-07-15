package com.futabooo.android.booklife.screen.home;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.FragmentHomeBinding;
import com.futabooo.android.booklife.model.HomeResource;
import com.futabooo.android.booklife.model.Resource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import retrofit2.Retrofit;
import timber.log.Timber;

public class HomeFragment extends Fragment {

  @Inject Retrofit retrofit;
  @Inject SharedPreferences sharedPreferences;

  private FragmentHomeBinding binding;

  private String page;
  private String volume;
  private String pageParDay;

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

    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM");
    binding.fragmentHomeDate.setText(dateFormat.format(date));

    Observable<ResponseBody> observable = retrofit.create(HomeService.class).home();
    observable.subscribeOn(Schedulers.io()).flatMap(new Function<ResponseBody, ObservableSource<JsonObject>>() {
      @Override public ObservableSource<JsonObject> apply(ResponseBody responseBody) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
        StringBuffer result = null;
        try {
          result = new StringBuffer();
          String line;
          while ((line = reader.readLine()) != null) {
            result.append(line);
          }
        } catch (IOException e) {
          Timber.e(e);
        }

        Elements readingVolumes = Jsoup.parse(result.toString())
            .select("div.home_index__userdata__main section.home_index__userdata__reading-volume");

        Elements thisVolume = readingVolumes.get(0).select("ul span");
        page = thisVolume.get(0).text();
        volume = thisVolume.get(2).text();
        pageParDay = thisVolume.get(4).text();

        String csrfToken = Jsoup.parse(result.toString()).select("meta[name=csrf-token]").get(0).attr("content");

        return retrofit.create(HomeService.class).getJson(csrfToken, 0, 10);
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JsonObject>() {
      @Override public void onSubscribe(Disposable d) {

      }

      @Override public void onNext(JsonObject value) {

        //user_idが保存されていない場合は取得して保存する
        if (!sharedPreferences.contains("user_id")) {
          JsonArray jsonArray = value.getAsJsonArray("resources");
          Gson gson = new Gson();
          HomeResource[] resources = gson.fromJson(jsonArray, HomeResource[].class);
          int userId = resources[0].getUser().getId();
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putInt("user_id", userId);
          editor.apply();
        }

        binding.readingPageCurrentMonth.setText(page);
        binding.readingVolumeCurrentMonth.setText(volume);
        binding.readingPageParDayCurrentMonth.setText(pageParDay);
      }

      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onComplete() {

      }
    });
  }
}
