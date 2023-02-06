package com.blueland.kimonesheet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.db.entity.FolderEntity
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.repository.MemoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WriteViewModel(private val memoRepository: MemoRepository) : ViewModel() {

    val isGetMemoComplete = MutableLiveData<MemoEntity>()
    val isItemInsertComplete = MutableLiveData<MappingDto>()
    val isItemUpdateComplete = MutableLiveData<Pair<Int, MappingDto>>()

    fun selectMemoId(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.selectMemoId(id).let {
                isGetMemoComplete.postValue(it)
            }
        }
    }

    fun addMemo(memo: MemoEntity, parentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.insertMemo(memo, parentId).let {
                isItemInsertComplete.postValue(it)
            }
        }
    }

    fun updateMemo(pos: Int, memo: MemoEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.updateMemo(memo.id!!, memo).let {
                isItemUpdateComplete.postValue(Pair(pos, it))
            }
        }
    }
}