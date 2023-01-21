package com.blueland.kimonesheet.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityMainBinding
import com.blueland.kimonesheet.db.RoomHelper
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.db.entity.FolderEntity
import com.blueland.kimonesheet.global.App
import com.blueland.kimonesheet.view.adapter.ListAdapter
import com.blueland.kimonesheet.widget.extension.activityResultLauncher
import com.blueland.kimonesheet.widget.extension.dpToPx
import com.blueland.kimonesheet.widget.extension.hideSoftKeyboard
import com.blueland.kimonesheet.widget.extension.toast
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : BaseActivity<ActivityMainBinding>(com.blueland.kimonesheet.R.layout.activity_main), ListAdapter.ListListener {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val helper by lazy { RoomHelper.getInstance(this) }

    private val adapter = ListAdapter()

    private var depth = 0
    private var parent: MutableList<MappingDto> = mutableListOf()

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
                        toast("'뒤로' 버튼을 한번 더 누르면 종료됩니다.")
                    } else {
                        finish()
                    }
                }
            }
        })

        binding.apply {
            btnSearch.setOnClickListener {
                loadKeywordData()
            }

            etKeyword.setOnKeyListener { _, keyCode, _ ->
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        loadKeywordData()
                    }
                }
                return@setOnKeyListener false
            }

            fbFolder.setOnClickListener {
                showAddFolderPopup()
            }

            fbWrite.setOnClickListener {
                val intent = Intent(this@MainActivity, WriteActivity::class.java)
                intent.putExtra("parentId", if (parent.isEmpty()) -1 else parent.last().childId)
                intent.putExtra("depth", depth)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                launcher.launch(intent)
            }

            fbMain.setOnClickListener {
                toggleFab()
            }
        }
    }

    private
    val aniOpen by lazy { AnimationUtils.loadAnimation(this, com.blueland.kimonesheet.R.anim.fab_open) }

    private
    val aniClose by lazy { AnimationUtils.loadAnimation(this, com.blueland.kimonesheet.R.anim.fab_close) }

    private
    var isFabOpen = false

    private fun toggleFab() {
        binding.apply {
            isFabOpen = if (isFabOpen) {
                fbMain.setImageResource(com.blueland.kimonesheet.R.drawable.ic_open)
                fbFolder.startAnimation(aniClose)
                fbWrite.startAnimation(aniClose)
                fbFolder.isClickable = false
                fbWrite.isClickable = false
                false
            } else {
                fbMain.setImageResource(com.blueland.kimonesheet.R.drawable.ic_close)
                fbFolder.startAnimation(aniOpen)
                fbWrite.startAnimation(aniOpen)
                fbFolder.isClickable = true
                fbWrite.isClickable = true
                true
            }
        }
    }

    private fun setMemoList() {
        binding.apply {
            adapter.listener = this@MainActivity
            recyclerView.adapter = adapter
        }
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val items = helper.mappingDao().select(if (parent.isEmpty()) -1 else parent.last().childId)
            runOnUiThread {
                adapter.setListItems(items)
            }
        }
    }

    private fun loadKeywordData() {
        depth = 0
        parent.clear()
        CoroutineScope(Dispatchers.IO).launch {
            val items = helper.mappingDao().select(binding.etKeyword.text.toString().trim())
            runOnUiThread {
                adapter.setListItems(items)
            }
        }
    }

    private fun showAddFolderPopup() {
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val margin = dpToPx(20)
        params.setMargins(margin, margin, margin, margin)

        val etFolder = EditText(this)
        etFolder.hint = "폴더명"
        etFolder.layoutParams = params
        container.addView(etFolder)

        AlertDialog.Builder(this)
            .setTitle("폴더 생성")
            .setIcon(com.blueland.kimonesheet.R.drawable.ic_folder)
            .setView(container)
            .setPositiveButton(getString(com.blueland.kimonesheet.R.string.alert_confirm)) { _, _ ->
                val folder = etFolder.text.toString().trim()
                if (folder.isBlank()) {
                    toast("폴더명을 입력하세요.")
                    return@setPositiveButton
                }
                addFolder(folder)
            }
            .setNegativeButton(getString(com.blueland.kimonesheet.R.string.alert_cancel)) { _, _ ->

            }
            .create()
            .show()
    }

    private fun addFolder(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            helper.folderDao().insert(FolderEntity(name = name))
            helper.folderDao().getLastId().let {
                if (it.isNotEmpty()) {
                    helper.mappingDao().insertFolder(
                        depth = depth,
                        parentId = if (parent.isEmpty()) -1 else parent.last().childId,
                        childId = it[0]
                    )
                    val items = helper.mappingDao().select(if (parent.isEmpty()) -1 else parent.last().childId)
                    runOnUiThread {
                        adapter.setListItems(items)
                    }
                }
            }
        }
    }

    private fun updateBookmark(id: Long, bookmarked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            helper.memoDao().updateBookmark(id, bookmarked)
            val items = helper.mappingDao().select(if (parent.isEmpty()) -1 else parent.last().childId)
            runOnUiThread {
                adapter.setListItems(items)
            }
        }
    }

    private fun delete(type: Int, mappingId: Long, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            helper.mappingDao().delete(mappingId)
            when (type) {
                0 -> helper.folderDao().delete(id)
                1 -> helper.memoDao().delete(id)
            }
            val items = helper.mappingDao().select(if (parent.isEmpty()) -1 else parent.last().childId)
            runOnUiThread {
                adapter.setListItems(items)
            }
        }
    }

    override fun itemOnBookmark(id: Long, bookmarked: Boolean) {
        updateBookmark(id, bookmarked)
    }

    override fun itemOnClick(item: MappingDto) {
        when (item.type) {
            0 -> {
                parent.add(item)
                depth++
                loadData()
                Log.d(TAG, "itemOnClick: $item")
            }
            1 -> {
                val intent = Intent(this@MainActivity, WriteActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("id", item.childId)
                launcher.launch(intent)
            }
        }
    }

    override fun itemOnLongClick(item: MappingDto) {
        when (item.type) {
            0 -> {
                App.getInstance().showAlertDialog(this, getString(com.blueland.kimonesheet.R.string.delete_folder), { _, _ ->
                    delete(item.type, item.mappingId, item.childId)
                }, null)
            }
            1 -> {
                App.getInstance().showAlertDialog(this, getString(com.blueland.kimonesheet.R.string.delete_memo), { _, _ ->
                    delete(item.type, item.mappingId, item.childId)
                }, null)
            }
        }
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