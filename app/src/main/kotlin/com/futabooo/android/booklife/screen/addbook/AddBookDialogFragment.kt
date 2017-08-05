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
import com.futabooo.android.booklife.databinding.DialogBookAddBinding
import com.futabooo.android.booklife.screen.search.ActionService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.Calendar
import javax.inject.Inject
import retrofit2.Retrofit
import timber.log.Timber

class AddBookDialogFragment : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

  @Inject lateinit var retrofit: Retrofit

  lateinit var binding: DialogBookAddBinding

  lateinit var listener: OnAddBookActionListener

  lateinit var readAt: String

  companion object {

    private val EXTRA_CSRF_TOKEN = "csrf_token"
    private val EXTRA_BOOK_USER_ID = "user_id"
    private val EXTRA_BOOK_ID = "book_id"

    fun newInstance(csrfToken: String, userID: Int, bookId: Int): AddBookDialogFragment {
      val dialogFragment = AddBookDialogFragment()
      val bundle = Bundle().apply {
        putString(EXTRA_CSRF_TOKEN, csrfToken)
        putInt(EXTRA_BOOK_USER_ID, userID)
        putInt(EXTRA_BOOK_ID, bookId)
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
    binding = DialogBookAddBinding.inflate(LayoutInflater.from(context), null, false)
    dialog.setContentView(binding.root)

    val calendar = Calendar.getInstance()
    readAt = DateFormat.format("yyyy/M/d", calendar).toString()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    binding.dialogBookAddReadDate.text = DateFormat.format("yyyy/MM/dd", calendar)

    binding.dialogBookAddReadDate.setOnClickListener {
      val datePickerDialog = DatePickerDialog(context, this@AddBookDialogFragment, year, month, dayOfMonth)
      datePickerDialog.show()
    }

    binding.dialogBookAddNegative.setOnClickListener { dismiss() }

    binding.dialogBookAddPositive.setOnClickListener {
      val csrfToken = arguments.getString(EXTRA_CSRF_TOKEN)
      val userId = arguments.getInt(EXTRA_BOOK_USER_ID)
      val bookId = arguments.getInt(EXTRA_BOOK_ID)
      val impressions = binding.dialogBookAddImpressions.text.toString()
      val netabare = if (binding.dialogBookAddSpoiler.isChecked) 1 else 0

      retrofit.create(ActionService::class.java)
          .read(csrfToken!!, userId, bookId, readAt, impressions, netabare)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribeBy(
              onNext = {
                listener.onRegister(bookId)
                dismiss()
              },
              onError = { Timber.e(it) },
              onComplete = {}
          )
    }
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
  }
}
