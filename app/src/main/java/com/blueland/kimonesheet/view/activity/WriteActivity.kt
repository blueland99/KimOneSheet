package com.blueland.kimonesheet.view.activity

import android.app.Activity
import androidx.activity.OnBackPressedCallback
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityWriteBinding
import com.blueland.kimonesheet.db.RoomHelper
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.global.App
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WriteActivity : BaseActivity<ActivityWriteBinding>(R.layout.activity_write) {

    private val helper by lazy { RoomHelper.getInstance(this) }

    private var parentId: Long = -1
    private var editMemo: MemoEntity? = null

    override fun initView() {
        super.initView()
        parentId = intent.getLongExtra("parent_id", -1)
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
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val title = etTitle.text.toString().trim()
                    val content = etContent.text.toString()

                    editMemo?.let {
                        if (it.title != title || it.content != content) showTextAlertDialog(true)
                        else finish()
                    } ?: run {
                        if (title.isNotEmpty() || content.isNotEmpty()) showTextAlertDialog(false)
                        else finish()
                    }
                }
            })

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

    private fun showTextAlertDialog(isModify: Boolean) {
        App.getInstance().showAlertDialog(this, getString(if (isModify) R.string.memo_modify_ing else R.string.memo_ing), { _, _ ->
            finish()
        }, null)
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this)
    }
}