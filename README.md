# BookLife
Unofficial Android application of bookmeter(https://bookmeter.com/)

|![](https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/header.png)|
|:-:|

## Preview
![](https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/1.gif)
![](https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/2.gif)

## ScreenShots
<img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-0.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-1.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-2.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-3.png" width="240"><img src="https://raw.githubusercontent.com/futabooo/BookLife/assets/assets/screen-shot-4.png" width="240">

<a href='https://play.google.com/store/apps/details?id=com.futabooo.android.booklife&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="92" width="240"/></a>

## How to build
#### create keystore.properties
```
$ cd path/to/BookLife
$ mv keystore.properties.template keystore.properties
```

```txt:keystore.properties
storeFile=[your keystore filename]
storePassword=[your keystore password]
keyAlias=[your key alias]
keyPassword=[your key password]
```

#### create fabric.properties.properties
```
$ cd path/to/BookLife/app
$ mv fabric.properties.template fabric.properties
```

```txt:fabric.properties
apiSecret=[your api secret]
apiKey=[your api key]
```

#### create google-services.json
Create Firebase project and download google-services.json

#### create licenses.html
```
$ cd path/to/BookLife
$ ./gradlew generateLicensePage
```

## How to remote build with mainframer
See https://github.com/gojuno/mainframer/tree/development/samples/gradle-android

Japanese document [Androidのリモートビルドにmainframerを使ってみる - Qiita](https://qiita.com/futabooo/items/ed70efdd3929ebbfd161)

## Thanks
- [OkHttp](https://github.com/square/okhttp)
- [Retrofit](https://github.com/square/retrofit)
- [RxJava2 Adapter](https://github.com/square/retrofit/tree/master/retrofit-adapters/rxjava2)
-[Gson Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/gson)
- [jsoup](https://jsoup.org/)
- [PersistentCookieJar](https://github.com/franmontiel/PersistentCookieJar)
- [RxJava2](https://github.com/ReactiveX/RxJava)
- [RxAndroid2](https://github.com/ReactiveX/RxAndroid)
- [RxKotlin2](https://github.com/ReactiveX/RxKotlin)
- [Cryptore](https://github.com/KazaKago/Cryptore)
- [Dagger2](https://github.com/google/dagger)
- [Glide](https://github.com/bumptech/glide)
- [Glide Transformations](https://github.com/wasabeef/glide-transformations)
- [BottomBar](https://github.com/roughike/BottomBar)
- [DrawMe](https://github.com/rafakob/DrawMe)
- [ArcLayout](https://github.com/ogaclejapan/ArcLayout)
- [Timber](https://github.com/JakeWharton/timber)
