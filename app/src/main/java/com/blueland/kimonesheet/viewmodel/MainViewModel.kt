package com.blueland.kimonesheet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.db.entity.FolderEntity
import com.blueland.kimonesheet.repository.MemoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val memoRepository: MemoRepository) : ViewModel() {

    val isGetAllMemoComplete = MutableLiveData<List<MappingDto>>()
    val isItemInsertComplete = MutableLiveData<MappingDto>()
    val isItemUpdateComplete = MutableLiveData<Pair<Int, MappingDto>>()
    val isItemBookmarkComplete = MutableLiveData<List<MappingDto>>()
    val isItemDeleteComplete = MutableLiveData<Int>()

    fun getAllMemo(parentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.selectMemo(parentId).let {
                isGetAllMemoComplete.postValue(it)
            }
        }
    }

    fun getAllMemo(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.selectMemo(keyword).let {
                isGetAllMemoComplete.postValue(it)
            }
        }
    }

    fun addFolder(folder: FolderEntity, parentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.insertFolder(folder, parentId).let {
                isItemInsertComplete.postValue(it)
            }
        }
    }

    fun updateFolder(pos: Int, id: Long, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.updateFolder(id, name).let {
                isItemUpdateComplete.postValue(Pair(pos, it))
            }
        }
    }

    fun updateBookmark(id: Long, bookmarked: Boolean, parentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.updateBookmark(id, bookmarked).let {
                memoRepository.selectMemo(parentId).let {
                    isItemBookmarkComplete.postValue(it)
                }
            }
        }
    }

    fun saveMemo(pos: Int, item: MappingDto, parentId: Long) {
        if (pos == -1) {
            getAllMemo(parentId)
        } else {
            isItemUpdateComplete.postValue(Pair(pos, item))
        }
    }

    fun deleteMemo(pos: Int, id: Long, mappingId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.deleteMemo(id, mappingId).let {
                isItemDeleteComplete.postValue(pos)
            }
        }
    }

    fun deleteFolder(id: Long, mappingId: Long, parentId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            memoRepository.deleteFolder(id, mappingId, parentId).let {
                getAllMemo(parentId)
            }
        }
    }
}