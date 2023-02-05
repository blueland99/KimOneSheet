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
import com.blueland.kimonesheet.R
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

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main), ListAdapter.ListListener {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val helper by lazy { RoomHelper.getInstance(this) }

    private val adapter = ListAdapter()

    private var depth = 0
    private var parent: MutableList<MappingDto> = mutableListOf()
    private var isSearch = false

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
                    parentId = if (parent.isEmpty()) -1 else parent.last().childId,
                    launcher = launcher
                )
            }

            fbMain.setOnClickListener {
                toggleFab()
            }
        }
    }

    private
    val aniOpen by lazy { AnimationUtils.loadAnimation(this, R.anim.fab_open) }

    private
    val aniClose by lazy { AnimationUtils.loadAnimation(this, R.anim.fab_close) }

    private
    var isFabOpen = false

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

    private fun setMemoList() {
        binding.apply {
            adapter.listener = this@MainActivity
            recyclerView.adapter = adapter
        }
    }

    private fun loadData() {
        isSearch = false
        CoroutineScope(Dispatchers.IO).launch {
            val items = helper.mappingDao().select(if (parent.isEmpty()) -1 else parent.last().childId)
            runOnUiThread {
                adapter.setListItems(items)
            }
        }
    }

    private fun loadKeywordData() {
        isSearch = true
        depth = 0
        parent.clear()
        CoroutineScope(Dispatchers.IO).launch {
            val items = helper.mappingDao().select(binding.etKeyword.text.toString())
            runOnUiThread {
                adapter.setListItems(items)
            }
        }
    }

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
                addFolder(folder)
            }
            .setNegativeButton(getString(R.string.alert_cancel)) { _, _ ->

            }
            .create()
            .show()
    }

    private fun showModifyFolderDialog(name: String, id: Int) {
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
                updateFolder(folder, id)
            }
            .setNegativeButton(getString(R.string.alert_cancel)) { _, _ ->

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
                        parentId = if (parent.isEmpty()) -1 else parent.last().childId,
                        childId = it[0]
                    )
                    loadData()
                }
            }
        }
    }

    private fun updateFolder(name: String, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            helper.folderDao().updateFolder(name, id)
            loadData()
        }
    }

    private fun updateBookmark(id: Int, bookmarked: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            helper.memoDao().updateBookmark(id, bookmarked)
            if (isSearch) loadKeywordData() else loadData()
        }
    }

    private fun delete(type: Int, mappingId: Int, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            when (type) {
                0 -> {
                    helper.mappingDao().deleteMapping(mappingId)
                    helper.mappingDao().updateMapping(id, if (parent.isEmpty()) -1 else parent.last().childId)
                    helper.folderDao().delete(id)
                }
                1 -> {
                    helper.mappingDao().deleteMapping(mappingId)
                    helper.memoDao().delete(id)
                }
            }
            loadData()
        }
    }

    override fun itemOnBookmark(id: Int, bookmarked: Boolean) {
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
                WriteActivity.start(
                    context = this@MainActivity,
                    id = item.childId,
                    launcher = launcher
                )
            }
        }
    }

    override fun itemOnLongClick(item: MappingDto) {
        when (item.type) {
            0 -> {
                // 폴더
                showFolderLongClickDialog(item)
            }
            1 -> {
                // 메모
                App.getInstance().showAlertDialog(this, getString(R.string.delete_memo), { _, _ ->
                    delete(item.type, item.mappingId, item.childId)
                }, null)
            }
        }
    }

    private fun showFolderLongClickDialog(item: MappingDto) {
        AlertDialog.Builder(this)
            .setItems(arrayOf(getString(R.string.modify), getString(R.string.delete))) { _, pos ->
                when (pos) {
                    0 -> {
                        showModifyFolderDialog(item.folder ?: "", item.childId)
                    }
                    1 -> {
                        App.getInstance().showAlertDialog(this, getString(R.string.delete_folder), { _, _ ->
                            delete(item.type, item.mappingId, item.childId)
                        }, null)
                    }
                }
            }
            .create()
            .show()
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
                toast(R.string.update_cancel)
            }
        }
    }
}