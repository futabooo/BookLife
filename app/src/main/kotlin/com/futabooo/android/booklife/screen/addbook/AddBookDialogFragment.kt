package com.futabooo.android.booklife.screen.addbook

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.DatePicker
import com.futabooo.android.booklife.BookLife
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.databinding.DialogBookAddBinding
import com.futabooo.android.booklife.model.Review
import com.futabooo.android.booklife.screen.search.ActionService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class AddBookDialogFragment : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

  @Inject lateinit var retrofit: Retrofit

  lateinit var binding: DialogBookAddBinding

  lateinit var listener: OnAddBookActionListener

  lateinit var readAt: String

  val review by lazy {
    val review = arguments.getSerializable(EXTRA_REVIEW)
    if (review != null) {
      review as Review
    } else {
      null
    }
  }

  companion object {

    private val EXTRA_CSRF_TOKEN = "csrf_token"
    private val EXTRA_BOOK_USER_ID = "user_id"
    private val EXTRA_BOOK_ID = "book_id"
    private val EXTRA_REVIEW = "review]"

    fun newInstance(csrfToken: String, userID: Int, bookId: Int, review: Review? = null): AddBookDialogFragment {
      val dialogFragment = AddBookDialogFragment()
      val bundle = Bundle().apply {
        putString(EXTRA_CSRF_TOKEN, csrfToken)
        putInt(EXTRA_BOOK_USER_ID, userID)
        putInt(EXTRA_BOOK_ID, bookId)
        if (review != null) putSerializable(EXTRA_REVIEW, review)
      }
      dialogFragment.arguments = bundle
      return dialogFragment
    }
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)

    if (context !is OnAddBookActionListener) {
      throw ClassCastException("activity must implements OnAddBookActionListener")
    }
    this.listener = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activity.application as BookLife).netComponent.inject(this)
  }

  @SuppressLint("RestrictedApi")
  override fun setupDialog(dialog: Dialog, style: Int) {
    super.setupDialog(dialog, style)
    binding = DialogBookAddBinding.inflate(LayoutInflater.from(context), null, false).apply {
      dialog.setContentView(root)

      val calendar = Calendar.getInstance()
      val year = calendar.get(Calendar.YEAR)
      val month = calendar.get(Calendar.MONTH)
      val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

      readAt = DateFormat.format("yyyy/M/d", calendar).toString()
      dialogBookAddReadDate.text = DateFormat.format("yyyy/MM/dd", calendar)

      dialogBookAddUnknown.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) readAt = "日付不明" else readAt = dialogBookAddReadDate.text.toString()
      }

      review?.let {
        dialogBookAddPositive.text = getText(R.string.update)
        dialogBookAddImpressions.setText(it.content)
        dialogBookAddSpoiler.isChecked = it.netabare.netabare
        dialogBookAddReadDate.text = it.createdAt
        readAt = it.createdAt.toString()
        dialogBookAddUnknown.isChecked = readAt == "日付不明"
      }

      dialogBookAddReadDate.setOnClickListener {
        val datePickerDialog = DatePickerDialog(context, this@AddBookDialogFragment, year, month, dayOfMonth)
        datePickerDialog.show()
      }

      dialogBookAddNegative.setOnClickListener { dismiss() }

      dialogBookAddPositive.setOnClickListener {
        val csrfToken = arguments.getString(EXTRA_CSRF_TOKEN)
        val userId = arguments.getInt(EXTRA_BOOK_USER_ID)
        val bookId = arguments.getInt(EXTRA_BOOK_ID)
        val impressions = dialogBookAddImpressions.text.toString()
        val netabare = if (dialogBookAddSpoiler.isChecked) 1 else 0

        review?.let {
          if (readAt == "日付不明") {
            updateUnknown(csrfToken, it.id, bookId, impressions, netabare)
          } else {
            update(csrfToken, it.id, bookId, readAt, impressions, netabare)
          }

          return@setOnClickListener
        }

        if (readAt == "日付不明") {
          addUnknown(csrfToken, userId, bookId, impressions, netabare)
        } else {
          add(csrfToken, userId, bookId, readAt, impressions, netabare)
        }
      }
    }
  }

  private fun add(csrfToken: String,
                  userId: Int,
                  bookId: Int,
                  readAt: String,
                  impressions: String,
                  netabare: Int) {

    retrofit.create(ActionService::class.java)
        .read(csrfToken, userId, bookId, readAt, impressions, netabare)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = {
              listener.onRegister(bookId)
              dismiss()
            },
            onError = {
              listener.onError()
              Timber.e(it, it.message)
            },
            onComplete = {}
        )
  }

  private fun addUnknown(csrfToken: String,
                         userId: Int,
                         bookId: Int,
                         impressions: String,
                         netabare: Int) {

    retrofit.create(ActionService::class.java)
        .readUnknown(csrfToken, userId, bookId, "on", impressions, netabare)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = {
              listener.onRegister(bookId)
              dismiss()
            },
            onError = {
              listener.onError()
              Timber.e(it, it.message)
            },
            onComplete = {}
        )
  }

  private fun update(csrfToken: String,
                     reviewId: Int,
                     bookId: Int,
                     readAt: String,
                     impressions: String,
                     netabare: Int) {

    retrofit.create(ActionService::class.java)
        .update(csrfToken, reviewId, bookId, readAt, impressions, netabare)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onComplete = {
              listener.onRegister(bookId)
              dismiss()
            },
            onError = {
              listener.onError()
              Timber.e(it, it.message)
            }
        )

  }

  private fun updateUnknown(csrfToken: String,
                            reviewId: Int,
                            bookId: Int,
                            impressions: String,
                            netabare: Int) {

    retrofit.create(ActionService::class.java)
        .updateUnknown(csrfToken, reviewId, bookId, "on", impressions, netabare)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onComplete = {
              listener.onRegister(bookId)
              dismiss()
            },
            onError = {
              listener.onError()
              Timber.e(it, it.message)
            }
        )

  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val lp = dialog.window?.attributes
    val metrics = resources.displayMetrics
    lp?.width = metrics.widthPixels
    dialog.window?.attributes = lp
  }

  override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth)
    readAt = DateFormat.format("yyyy/M/d", calendar).toString()
    binding.dialogBookAddReadDate.text = DateFormat.format("yyyy/MM/dd", calendar)
  }

  interface OnAddBookActionListener {
    fun onRegister(bookId: Int)
    fun onUpdate(bookId: Int)
    fun onError()
  }
}
