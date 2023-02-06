package com.blueland.kimonesheet.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityMainBinding
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.db.entity.FolderEntity
import com.blueland.kimonesheet.global.App
import com.blueland.kimonesheet.repository.MemoRepository
import com.blueland.kimonesheet.view.adapter.ListAdapter
import com.blueland.kimonesheet.viewmodel.MainViewModel
import com.blueland.kimonesheet.viewmodel.factory.ViewModelFactory
import com.blueland.kimonesheet.widget.extension.activityResultLauncher
import com.blueland.kimonesheet.widget.extension.dpToPx
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), ListAdapter.ListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: ViewModelFactory

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val adapter = ListAdapter()

    private var depth = 0
    private var parent: MutableList<MappingDto> = mutableListOf()
    private var isSearch = false
    private fun getParentId(): Long = if (parent.isEmpty()) -1 else parent.last().childId

    companion object {
        const val REQUEST_CODE_INAPP_UPDATE = 1001

        /**
         * MainActivity 시작 메소드
         */
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

    private val launcher = activityResultLauncher {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let { intent ->
                viewModel.saveMemo(
                    pos = intent.getIntExtra("pos", -1),
                    item = intent.getSerializableExtra("item") as MappingDto,
                    parentId = getParentId(),
                )
            }
        }
    }

    override fun initView() {
        super.initView()
        setRecyclerView()
    }

    override fun initViewModel() {
        super.initViewModel()
        viewModelFactory = ViewModelFactory(MemoRepository())
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.isGetAllMemoComplete.observe(this) {
            adapter.items = it as MutableList<MappingDto>
            adapter.notifyDataSetChanged()
        }

        viewModel.isItemInsertComplete.observe(this) {
            adapter.items.add(0, it)
            adapter.notifyItemInserted(0)
        }

        viewModel.isItemUpdateComplete.observe(this) {
            adapter.items.removeAt(it.first)
            adapter.notifyItemRemoved(it.first)
            var pos = 0
            for ((index, item) in adapter.items.withIndex()) {
                if (item.type == 1) {
                    pos = index
                    break
                }
            }
            adapter.items.add(pos, it.second)
            adapter.notifyItemInserted(pos)
        }
        viewModel.isItemBookmarkComplete.observe(this) {
            adapter.items = it as MutableList<MappingDto>
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }

        viewModel.isItemDeleteComplete.observe(this) {
            adapter.items.removeAt(it)
            adapter.notifyItemRemoved(it)
        }
    }

    private var lastTimeBackPressed: Long = 0

    override fun initListener() {
        super.initListener()
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (depth > 0) {
                    parent.removeLast()
                    depth--
                    loadData()
                } else {
                    if (System.currentTimeMillis() - lastTimeBackPressed >= 1500) {
                        lastTimeBackPressed = System.currentTimeMillis()
                        toast(R.string.back_pressed)
                    } else {
                        finish()
                    }
                }
            }
        })

        binding.apply {
            btnSearch.setOnClickListener {
                val keyword = binding.etKeyword.text.toString()
                if (keyword.isNotEmpty()) loadKeywordData()
                else loadData()
            }

            etKeyword.setOnKeyListener { _, keyCode, _ ->
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        btnSearch.performClick()
                    }
                }
                return@setOnKeyListener false
            }

            fbFolder.setOnClickListener {
                showAddFolderDialog()
            }

            fbWrite.setOnClickListener {
                WriteActivity.start(
                    context = this@MainActivity,
                    parentId = getParentId(),
                    launcher = launcher
                )
            }

            fbMain.setOnClickListener {
                toggleFab()
            }
        }
    }

    override fun afterOnCreate() {
        super.afterOnCreate()
        appUpdateCheck()
        loadData()
    }

    /**
     * 상위 아이디로 조회 아이템 조회
     */
    private fun loadData() {
        isSearch = false
        viewModel.getAllMemo(getParentId())
    }

    /**
     * 키워드로 아이템 조회
     */
    private fun loadKeywordData() {
        isSearch = true
        depth = 0
        parent.clear()
        viewModel.getAllMemo(binding.etKeyword.text.toString())
    }

    /**
     * RecyclerView 초기화
     */
    private fun setRecyclerView() {
        binding.apply {
            adapter.listener = this@MainActivity
            recyclerView.adapter = adapter
        }
    }

    /**
     * 플로팅 버튼 UI 처리
     */
    private val aniOpen by lazy { AnimationUtils.loadAnimation(this, R.anim.fab_open) }
    private val aniClose by lazy { AnimationUtils.loadAnimation(this, R.anim.fab_close) }
    private var isFabOpen = false

    private fun toggleFab() {
        binding.apply {
            isFabOpen = if (isFabOpen) {
                fbMain.setImageResource(R.drawable.ic_open)
                fbFolder.startAnimation(aniClose)
                fbWrite.startAnimation(aniClose)
                fbFolder.isClickable = false
                fbWrite.isClickable = false
                false
            } else {
                fbMain.setImageResource(R.drawable.ic_close)
                fbFolder.startAnimation(aniOpen)
                fbWrite.startAnimation(aniOpen)
                fbFolder.isClickable = true
                fbWrite.isClickable = true
                true
            }
        }
    }

    /**
     * 아이템 북마크 클릭
     */
    override fun itemOnBookmark(pos: Int, id: Long, bookmarked: Boolean) {
        viewModel.updateBookmark(id, bookmarked, getParentId())
    }

    /**
     * 아이템 클릭
     */
    override fun itemOnClick(pos: Int, item: MappingDto) {
        Log.d(TAG, "itemOnClick: $item")
        when (item.type) {
            0 -> {
                parent.add(item)
                depth++
                loadData()
            }
            1 -> {
                WriteActivity.start(
                    context = this@MainActivity,
                    pos = pos,
                    id = item.childId,
                    launcher = launcher
                )
            }
        }
    }

    /**
     * 아이템 롱 클릭
     */
    override fun itemOnLongClick(pos: Int, item: MappingDto) {
        when (item.type) {
            0 -> {
                // 폴더
                showFolderLongClickDialog(pos, item)
            }
            1 -> {
                // 메모
                App.instance.showAlertDialog(this, getString(R.string.delete_memo), { _, _ ->
                    viewModel.deleteMemo(pos, item.childId, item.mappingId)
                }, null)
            }
        }
    }

    /**
     * 폴더 추가 팝업
     */
    private fun showAddFolderDialog() {
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = dpToPx(20)
        params.setMargins(margin, margin, margin, margin)

        val etFolder = EditText(this)
        etFolder.hint = getString(R.string.folder_name)
        etFolder.layoutParams = params
        container.addView(etFolder)

        AlertDialog.Builder(this)
            .setTitle(R.string.create_folder)
            .setIcon(R.drawable.ic_folder)
            .setView(container)
            .setPositiveButton(getString(R.string.alert_confirm)) { _, _ ->
                val folder = etFolder.text.toString().trim()
                if (folder.isBlank()) {
                    toast(R.string.input_folder_name)
                    return@setPositiveButton
                }
                viewModel.addFolder(
                    folder = FolderEntity(name = folder),
                    parentId = getParentId()
                )
            }
            .setNegativeButton(getString(R.string.alert_cancel)) { _, _ ->

            }
            .create()
            .show()
    }

    /**
     * 폴더명 수정 팝업
     */
    private fun showModifyFolderDialog(pos: Int, name: String, id: Long) {
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = dpToPx(20)
        params.setMargins(margin, margin, margin, margin)

        val etFolder = EditText(this)
        etFolder.hint = getString(R.string.folder_name)
        etFolder.layoutParams = params
        etFolder.setText(name)
        container.addView(etFolder)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.modify_folder))
            .setIcon(R.drawable.ic_folder)
            .setView(container)
            .setPositiveButton(getString(R.string.alert_confirm)) { _, _ ->
                val folder = etFolder.text.toString().trim()
                if (folder.isBlank()) {
                    toast(R.string.input_folder_name)
                    return@setPositiveButton
                }
                viewModel.updateFolder(
                    pos = pos,
                    id = id,
                    name = name
                )
            }
            .setNegativeButton(getString(R.string.alert_cancel)) { _, _ ->

            }
            .create()
            .show()
    }

    /**
     * 폴더 롱 클릭 선택 팝업. 수정 or 삭제
     */
    private fun showFolderLongClickDialog(pos: Int, item: MappingDto) {
        AlertDialog.Builder(this)
            .setItems(arrayOf(getString(R.string.modify), getString(R.string.delete))) { _, arrPos ->
                when (arrPos) {
                    0 -> {
                        showModifyFolderDialog(pos, item.folder ?: "", item.childId)
                    }
                    1 -> {
                        App.instance.showAlertDialog(this, getString(R.string.delete_folder), { _, _ ->
                            viewModel.deleteFolder(item.childId, item.mappingId, getParentId())
                        }, null)
                    }
                }
            }
            .create()
            .show()
    }

    /**
     * In-App 업데이트 체크
     */
    private fun appUpdateCheck() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // or AppUpdateType.FLEXIBLE
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE, // or AppUpdateType.FLEXIBLE
                    this,
                    REQUEST_CODE_INAPP_UPDATE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INAPP_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                toast(R.string.update_cancel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this)
    }
}