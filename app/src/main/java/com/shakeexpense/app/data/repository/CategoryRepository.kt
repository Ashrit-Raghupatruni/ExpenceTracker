package com.shakeexpense.app.data.repository

import com.shakeexpense.app.data.database.dao.CategoryDao
import com.shakeexpense.app.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getAllCategoriesSync(): List<CategoryEntity>
    suspend fun getCategoryById(id: Long): CategoryEntity?
    suspend fun getCategoryByName(name: String): CategoryEntity?
    suspend fun addCategory(category: CategoryEntity): Long
}

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    override suspend fun getAllCategoriesSync(): List<CategoryEntity> =
        categoryDao.getAllCategoriesSync()

    override suspend fun getCategoryById(id: Long): CategoryEntity? =
        categoryDao.getCategoryById(id)

    override suspend fun getCategoryByName(name: String): CategoryEntity? =
        categoryDao.getCategoryByName(name)

    override suspend fun addCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)
}
