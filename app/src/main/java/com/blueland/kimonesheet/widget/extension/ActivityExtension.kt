package com.blueland.kimonesheet.widget.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Activity 전용 - 유틸성 확장 함수를 정의
 */

/**
 * 특정 Activity를 실행시키고, 해당 Activity가 종료되었을 때 callback을 받는다.
 * Deprecated된 startActivityForResult 대용
 */
internal fun AppCompatActivity.activityResultLauncher(onResultActivity: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult(), onResultActivity)
}

// Activity Intent
internal fun AppCompatActivity.startActivity(context: Context, activity: Class<*>) {
    startActivity(Intent(context, activity).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
}

// vertical RecyclerView
internal fun RecyclerView.showVertical(context: Context) {
    this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
}

// horizontal RecyclerView
internal fun RecyclerView.showHorizontal(context: Context) {
    this.layoutManager =
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
}

internal fun showSoftKeyboard(view: View) {
    val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

internal fun hideSoftKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    activity.currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}