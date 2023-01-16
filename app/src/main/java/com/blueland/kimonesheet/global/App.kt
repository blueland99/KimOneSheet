package com.blueland.kimonesheet.global

import android.app.Application
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.blueland.kimonesheet.R

class App : Application() {

    private val TAG = javaClass.simpleName
    private var builder: AlertDialog? = null

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun getInstance(): App {
            if (instance == null) instance = App()
            return instance!!
        }

        fun getGlobalContext(): Context? {
            return instance?.applicationContext
        }
    }

    /**
     * AlertDialog
     * @param context
     * @param message 메세지
     * @param positiveOnClickListener '확인' 이벤트 리스너
     * @param negativeOnClickListener '취소' 이벤트 리스너
     */
    fun showAlertDialog(
        context: Context?, message: String?, positiveOnClickListener: DialogInterface.OnClickListener?, negativeOnClickListener: DialogInterface.OnClickListener?
    ) {
        if (builder != null) {
            builder!!.dismiss()
            builder = null
        }
        builder = AlertDialog.Builder(context!!)
            .setMessage(message)
            .setPositiveButton(R.string.alert_confirm, positiveOnClickListener)
            .setNegativeButton(R.string.alert_cancel, negativeOnClickListener)
            .create()
        builder!!.show()
    }
}