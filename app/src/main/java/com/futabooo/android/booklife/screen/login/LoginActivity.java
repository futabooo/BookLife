package com.futabooo.android.booklife.screen.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.HostSelectionInterceptor;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityLoginBinding;
import com.futabooo.android.booklife.screen.MainActivity;
import com.kazakago.cryptore.Cryptore;
import com.kazakago.cryptore.EncryptResult;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

  @Inject Retrofit retrofit;
  @Inject HostSelectionInterceptor hostSelectionInterceptor;
  @Inject SharedPreferences sharedPreferences;
  @Inject Cryptore cryptore;

  public static final String EXTRA_EMAIL = "email";
  public static final String EXTRA_PASSWORD = "password";

  private ActivityLoginBinding binding;
  private LoginPresenterImpl loginPresenter;

  public static Intent createIntent(Context context, String email, String password) {
    Intent intent = new Intent(context, LoginActivity.class);
    intent.putExtra(EXTRA_EMAIL, email);
    intent.putExtra(EXTRA_PASSWORD, password);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
    loginPresenter = new LoginPresenterImpl();

    binding.email.setText(getIntent().getStringExtra(EXTRA_EMAIL));
    binding.password.setText(getIntent().getStringExtra(EXTRA_PASSWORD));

    binding.password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    binding.signInButton.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        attemptLogin();
      }
    });

    binding.forgetPassword.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        Uri uri = Uri.parse("http://i.bookmeter.com/reset_password");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
      }
    });

    binding.signUp.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        Uri uri = Uri.parse("https://i.bookmeter.com/signup");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
      }
    });
  }

  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private void attemptLogin() {
    // Reset errors.
    binding.email.setError(null);
    binding.password.setError(null);

    // Store values at the time of the login attempt.
    final String email = binding.email.getText().toString();
    final String password = binding.password.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (!TextUtils.isEmpty(password) && !loginPresenter.isPasswordValid(password)) {
      binding.password.setError(getString(R.string.error_invalid_password));
      focusView = binding.password;
      cancel = true;
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      binding.email.setError(getString(R.string.error_field_required));
      focusView = binding.email;
      cancel = true;
    } else if (!loginPresenter.isEmailValid(email)) {
      binding.email.setError(getString(R.string.error_invalid_email));
      focusView = binding.email;
      cancel = true;
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus();
    } else {
      hostSelectionInterceptor.setScheme("https");
      Observable<ResponseBody> observable = retrofit.create(LoginService.class).login(email, password);
      observable.subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe(new Consumer<Disposable>() {
            @Override public void accept(Disposable disposable) throws Exception {
              showProgress(true);
            }
          })
          .subscribe(new Observer<ResponseBody>() {
            @Override public void onSubscribe(Disposable d) {

            }

            @Override public void onNext(ResponseBody value) {
              try {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                byte[] emailBytes = email.getBytes();
                EncryptResult encryptEmail = cryptore.encrypt(emailBytes);
                editor.putString("email", Base64.encodeToString(encryptEmail.getBytes(), Base64.DEFAULT));
                editor.apply();

                byte[] passwordBytes = password.getBytes();
                EncryptResult encryptPassword = cryptore.encrypt(passwordBytes);
                editor.putString("password", Base64.encodeToString(encryptPassword.getBytes(), Base64.DEFAULT));
                editor.apply();
              } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
              } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
              } catch (KeyStoreException e) {
                e.printStackTrace();
              } catch (InvalidKeyException e) {
                e.printStackTrace();
              } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
              } catch (NoSuchPaddingException e) {
                e.printStackTrace();
              } catch (IOException e) {
                e.printStackTrace();
              } catch (NoSuchProviderException e) {
                e.printStackTrace();
              }

              startActivity(MainActivity.createIntent(LoginActivity.this));
              hostSelectionInterceptor.setScheme(null);
            }

            @Override public void onError(Throwable e) {
              hostSelectionInterceptor.setScheme(null);
            }

            @Override public void onComplete() {

            }
          });
    }
  }

  /**
   * Shows the progress UI and hides the login form.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) private void showProgress(final boolean show) {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
      int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

      binding.loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
      binding.loginForm.animate()
          .setDuration(shortAnimTime)
          .alpha(show ? 0 : 1)
          .setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
              binding.loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
          });

      binding.loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
      binding.loginProgress.animate()
          .setDuration(shortAnimTime)
          .alpha(show ? 1 : 0)
          .setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
              binding.loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
          });
    } else {
      // The ViewPropertyAnimator APIs are not available, so simply show
      // and hide the relevant UI components.
      binding.loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
      binding.loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
    }
  }
}

