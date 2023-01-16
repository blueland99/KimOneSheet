package com.blueland.kimonesheet.view.activity

import android.app.Activity
import android.content.Intent
import android.view.KeyEvent
import androidx.room.Room
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityMainBinding
import com.blueland.kimonesheet.db.RoomHelper
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.global.App
import com.blueland.kimonesheet.view.adapter.ListAdapter
import com.blueland.kimonesheet.widget.extension.activityResultLauncher
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), ListAdapter.ListListener {

    private val memoDao by lazy {
        Room.databaseBuilder(this, RoomHelper::class.java, "memo_db")
            .allowMainThreadQueries()
            .build()
            .memoDao()
    }

    private val launcher = activityResultLauncher {
        if (it.resultCode == Activity.RESULT_OK) {
            setMemoList()
        }
    }

    override fun initView() {
        super.initView()
        setMemoList()
    }

    private fun setMemoList() {
        binding.apply {
            val keyword = etKeyword.text.toString().trim()

            val adapter = ListAdapter()
            adapter.items = if (keyword.isBlank()) memoDao.selectAll() else memoDao.selectKeyword(keyword)
            adapter.listener = this@MainActivity
            recyclerView.adapter = adapter
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
}