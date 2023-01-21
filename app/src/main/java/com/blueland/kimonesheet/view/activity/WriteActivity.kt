package com.blueland.kimonesheet.view.activity

import android.app.Activity
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityWriteBinding
import com.blueland.kimonesheet.db.RoomHelper
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WriteActivity : BaseActivity<ActivityWriteBinding>(R.layout.activity_write) {

    private val helper by lazy { RoomHelper.getInstance(this) }

    private var parentId: Long = -1
    private var depth: Int = 0
    private var editMemo: MemoEntity? = null

    override fun initView() {
        super.initView()
        parentId = intent.getLongExtra("parentId", -1)
        depth = intent.getIntExtra("depth", 0)
        intent.getLongExtra("id", -1).let { id ->
            if (id > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    helper.memoDao().select(id).let {
                        if (it.isNotEmpty()) {
                            editMemo = it[0]
                            runOnUiThread {
                                binding.apply {
                                    cbBookmark.isChecked = it[0].bookmark
                                    etTitle.setText(it[0].title)
                                    etContent.setText(it[0].content)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.apply {
            btnSave.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString()
                if (content.isBlank()) {
                    toast("내용을 입력하세요.")
                    return@setOnClickListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    editMemo?.let {
                        helper.memoDao().update(
                            MemoEntity(
                                id = it.id,
                                title = title,
                                content = content,
                                modDate = System.currentTimeMillis(),
                                regDate = it.regDate,
                                bookmark = cbBookmark.isChecked
                            )
                        )
                    } ?: run {
                        helper.memoDao().insert(
                            MemoEntity(
                                title = title,
                                content = content,
                                bookmark = cbBookmark.isChecked
                            )
                        )
                        helper.memoDao().getLastId().let {
                            if (it.isNotEmpty()) {
                                helper.mappingDao().insertMemo(
                                    depth = depth,
                                    parentId = parentId,
                                    childId = it[0]
                                )
                            }
                        }
                    }
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