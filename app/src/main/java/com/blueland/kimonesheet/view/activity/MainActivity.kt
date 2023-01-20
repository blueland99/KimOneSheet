package com.blueland.kimonesheet.view.activity

import android.app.Activity
import android.content.Intent
import android.view.KeyEvent
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityMainBinding
import com.blueland.kimonesheet.db.RoomHelper
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.global.App
import com.blueland.kimonesheet.view.adapter.ListAdapter
import com.blueland.kimonesheet.widget.extension.activityResultLauncher
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), ListAdapter.ListListener {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val memoDao by lazy { RoomHelper.getInstance(this).memoDao() }

    private val adapter = ListAdapter()

    private val launcher = activityResultLauncher {
        if (it.resultCode == Activity.RESULT_OK) {
            loadData()
        }
    }

    override fun initView() {
        super.initView()
        setMemoList()
        loadData()
    }

    private fun setMemoList() {
        binding.apply {
            adapter.listener = this@MainActivity
            recyclerView.adapter = adapter
        }
    }

    private fun loadData() {
        binding.apply {
            val keyword = etKeyword.text.toString().trim()
            CoroutineScope(Dispatchers.IO).launch {
                val items = if (keyword.isBlank()) memoDao.selectAll() else memoDao.selectKeyword(keyword)
                runOnUiThread {
                    adapter.setListItems(items)
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.apply {
            btnWrite.setOnClickListener {
                val intent = Intent(this@MainActivity, WriteActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                launcher.launch(intent)
            }

            btnSearch.setOnClickListener {
                setMemoList()
            }

            etKeyword.setOnKeyListener { _, keyCode, _ ->
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        setMemoList()
                    }
                }
                return@setOnKeyListener false
            }
        }
    }

    override fun itemOnClick(pos: Int, item: MemoEntity) {
        val intent = Intent(this@MainActivity, WriteActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("memo", item)
        launcher.launch(intent)
    }

    override fun itemOnLongClick(pos: Int, item: MemoEntity) {
        App.getInstance().showAlertDialog(this, getString(R.string.delete_memo), { _, _ ->
            memoDao.delete(item)
            setMemoList()
        }, null)
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this)
    }

    companion object {
        const val REQUEST_CODE_UPDATE = 1001
    }

    override fun afterOnCreate() {
        super.afterOnCreate()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // or AppUpdateType.FLEXIBLE
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE, // or AppUpdateType.FLEXIBLE
                    this,
                    REQUEST_CODE_UPDATE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                toast("업데이트가 취소 되었습니다.")
            }
        }
    }
}