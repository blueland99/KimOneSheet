package com.blueland.kimonesheet.view.activity

import android.app.Activity
import androidx.room.Room
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityWriteBinding
import com.blueland.kimonesheet.db.RoomHelper
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast

class WriteActivity : BaseActivity<ActivityWriteBinding>(R.layout.activity_write) {

    private val memoDao by lazy {
        Room.databaseBuilder(this, RoomHelper::class.java, "memo_db")
            .allowMainThreadQueries()
            .build()
            .memoDao()
    }

    private var editMemo: MemoEntity? = null

    override fun initView() {
        super.initView()
        editMemo = intent.getSerializableExtra("memo") as MemoEntity?
        editMemo?.let {
            binding.etTitle.setText(it.title)
            binding.etContent.setText(it.content)
        }
    }

    override fun initListener() {
        super.initListener()
        binding.apply {
            btnSave.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString()
                if (content.isBlank()) {
                    toast("내용을 입력해주세요.")
                    return@setOnClickListener
                }

                editMemo?.let {
                    memoDao.update(
                        MemoEntity(
                            id = it.id,
                            title = title,
                            content = content,
                            modDate = System.currentTimeMillis(),
                            regDate = it.regDate
                        )
                    )
                } ?: run {
                    memoDao.insert(
                        MemoEntity(
                            title = title,
                            content = content
                        )
                    )
                }

                // TODO: 저장
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this)
    }
}