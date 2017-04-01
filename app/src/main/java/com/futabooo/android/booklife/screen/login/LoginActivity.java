package com.futabooo.android.booklife.screen.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import com.futabooo.android.booklife.BookLife;
import com.futabooo.android.booklife.HostSelectionInterceptor;
import com.futabooo.android.booklife.R;
import com.futabooo.android.booklife.databinding.ActivityLoginBinding;
import com.futabooo.android.booklife.screen.MainActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

  @Inject Retrofit retrofit;
  @Inject HostSelectionInterceptor hostSelectionInterceptor;

  private ActivityLoginBinding binding;

  private LoginPresenterImpl loginPresenter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((BookLife) getApplication()).getNetComponent().inject(this);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
    loginPresenter = new LoginPresenterImpl();

    binding.password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    Button signInButton = (Button) findViewById(R.id.sign_in_button);
    signInButton.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View view) {
        attemptLogin();
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
    String email = binding.email.getText().toString();
    String password = binding.password.getText().toString();

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
      binding.loginProgress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
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

