package com.blueland.kimonesheet.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityWriteBinding
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.global.App
import com.blueland.kimonesheet.repository.MemoRepository
import com.blueland.kimonesheet.viewmodel.WriteViewModel
import com.blueland.kimonesheet.viewmodel.factory.ViewModelFactory
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WriteActivity : BaseActivity<ActivityWriteBinding>(R.layout.activity_write) {

    private lateinit var viewModel: WriteViewModel
    private lateinit var viewModelFactory: ViewModelFactory

    private var pos: Int = -1
    private var parentId: Long = -1
    private var id: Long = -1
    private var editMemo: MemoEntity? = null

    companion object {
        /**
         * WriteActivity 시작 메소드
         */
        fun start(context: Context, pos: Int = -1, id: Long = -1, parentId: Long = -1, launcher: ActivityResultLauncher<Intent>) {
            val intent = Intent(context, WriteActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("pos", pos)
            intent.putExtra("id", id)
            intent.putExtra("parent_id", parentId)
            launcher.launch(intent)
        }
    }

    override fun initView() {
        super.initView()
        pos = intent.getIntExtra("pos", -1)
        parentId = intent.getLongExtra("parent_id", -1)
        id = intent.getLongExtra("id", -1)
    }

    override fun initViewModel() {
        super.initViewModel()
        viewModelFactory = ViewModelFactory(MemoRepository())
        viewModel = ViewModelProvider(this, viewModelFactory)[WriteViewModel::class.java]
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.isGetMemoComplete.observe(this) {
            editMemo = it
            binding.apply {
                cbBookmark.isChecked = it.bookmark
                etTitle.setText(it.title)
                etContent.setText(it.content)
            }
        }

        viewModel.isItemUpdateComplete.observe(this) {
            // 수정
            val intent = intent
            intent.putExtra("pos", it.first)
            intent.putExtra("item", it.second)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        viewModel.isItemInsertComplete.observe(this) {
            // 추가
            val intent = intent
            intent.putExtra("item", it)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun afterOnCreate() {
        super.afterOnCreate()
        if (id > 0) {
            viewModel.selectMemoId(id)
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
                    toast(R.string.input_content)
                    return@setOnClickListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    editMemo?.let {
                        viewModel.updateMemo(
                            pos = pos,
                            memo = MemoEntity(
                                id = it.id,
                                title = title,
                                content = content,
                                modDate = System.currentTimeMillis(),
                                regDate = it.regDate,
                                bookmark = cbBookmark.isChecked
                            )
                        )
                    } ?: run {
                        viewModel.addMemo(
                            memo = MemoEntity(
                                title = title,
                                content = content,
                                bookmark = cbBookmark.isChecked
                            ),
                            parentId = parentId
                        )
                    }
                }
            }
        }
    }

    /**
     * 작성중인 내용 안내 팝업
     */
    private fun showTextAlertDialog(isModify: Boolean) {
        App.instance.showAlertDialog(this, getString(if (isModify) R.string.memo_modify_ing else R.string.memo_ing), { _, _ ->
            finish()
        }, null)
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this)
    }
}