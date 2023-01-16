package com.blueland.kimonesheet.view.activity

import android.app.Activity
import android.content.Intent
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityMainBinding
import com.blueland.kimonesheet.model.MemoModel
import com.blueland.kimonesheet.view.adapter.ListAdapter
import com.blueland.kimonesheet.widget.extension.activityResultLauncher
import java.util.*

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), ListAdapter.ListListener {

    private val launcher = activityResultLauncher {
        if (it.resultCode == Activity.RESULT_OK) {
            // TODO: DB 메모 리스트 가져오기 (갱신)
        }
    }

    override fun initView() {
        super.initView()
        binding.apply {
            val adapter = ListAdapter()
            // TODO: DB 메모 리스트 가져오기
            adapter.items = listOf(MemoModel("제목", "내용", Date(System.currentTimeMillis())), MemoModel("제목", "내용", Date(System.currentTimeMillis())))
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
                // TODO: 검색
            }
        }
    }

    override fun itemOnClick(pos: Int, item: MemoModel) {
        // TODO: 수정 화면 이동
    }
}