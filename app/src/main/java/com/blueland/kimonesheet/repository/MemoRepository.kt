package com.blueland.kimonesheet.repository

import com.blueland.kimonesheet.db.dao.MappingDto
import com.blueland.kimonesheet.db.entity.FolderEntity
import com.blueland.kimonesheet.db.entity.MemoEntity
import com.blueland.kimonesheet.global.App

class MemoRepository {
    private val memoDao = App.helper.memoDao()
    private val folderDao = App.helper.folderDao()
    private val mappingDao = App.helper.mappingDao()

    suspend fun selectMemoId(id: Long): MemoEntity {
        return memoDao.select(id)[0]
    }

    suspend fun selectMemo(parentId: Long): List<MappingDto> {
        return mappingDao.select(parentId)
    }

    suspend fun selectMemo(keyword: String): List<MappingDto> {
        return mappingDao.select(keyword)
    }

    suspend fun insertMemo(memo: MemoEntity, parentId: Long): MappingDto {
        memoDao.insert(memo).let { id ->
            mappingDao.insertMemo(
                parentId = parentId,
                childId = id
            ).let { it ->
                mappingDao.selectMappingId(it).let {
                    return it[0]
                }
            }
        }
    }

    suspend fun updateMemo(id: Long, memo: MemoEntity): MappingDto {
        memoDao.update(memo).let {
            mappingDao.selectMemoId(id).let {
                return it[0]
            }
        }
    }

    suspend fun updateBookmark(id: Long, bookmarked: Boolean): MappingDto {
        memoDao.updateBookmark(id, bookmarked).let {
            mappingDao.selectMemoId(id).let {
                return it[0]
            }
        }
    }

    suspend fun insertFolder(folder: FolderEntity, parentId: Long): MappingDto {
        folderDao.insert(folder).let { id ->
            mappingDao.insertFolder(
                parentId = parentId,
                childId = id
            ).let { it ->
                mappingDao.selectMappingId(it).let {
                    return it[0]
                }
            }
        }
    }

    suspend fun updateFolder(id: Long, name: String): MappingDto {
        folderDao.updateFolder(name, id).let {
            mappingDao.selectFolderId(id).let {
                return it[0]
            }
        }
    }

    suspend fun deleteFolder(id: Long, mappingId: Long, parentId: Long) {
        mappingDao.deleteMapping(mappingId).let {
            mappingDao.updateMapping(id, parentId).let {
                folderDao.delete(id)
            }
        }
    }

    suspend fun deleteMemo(id: Long, mappingId: Long) {
        mappingDao.deleteMapping(mappingId).let {
            memoDao.delete(id)
        }
    }
}