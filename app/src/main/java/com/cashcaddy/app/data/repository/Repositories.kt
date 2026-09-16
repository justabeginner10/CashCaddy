package com.cashcaddy.app.data.repository

import com.cashcaddy.app.data.local.dao.BudgetDao
import com.cashcaddy.app.data.local.dao.CategoryDao
import com.cashcaddy.app.data.local.dao.TransactionDao
import com.cashcaddy.app.data.local.entity.BudgetEntity
import com.cashcaddy.app.data.local.entity.BudgetWithCategory
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionEntity
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    fun observeActive(): Flow<List<CategoryEntity>> = dao.observeActive()
    suspend fun getAll(): List<CategoryEntity> = dao.getAll()
    suspend fun insert(entity: CategoryEntity): Long = dao.insert(entity)
    suspend fun update(entity: CategoryEntity) = dao.update(entity)
    suspend fun delete(entity: CategoryEntity) = dao.delete(entity)
    suspend fun usageCount(id: Long): Int = dao.usageCount(id)
}

class TransactionRepository(private val dao: TransactionDao) {
    fun observeWithCategory(): Flow<List<TransactionWithCategory>> = dao.observeWithCategory()
    suspend fun getById(id: Long): TransactionWithCategory? = dao.getById(id)
    suspend fun insert(entity: TransactionEntity): Long = dao.insert(entity)
    suspend fun update(entity: TransactionEntity) = dao.update(entity)
    suspend fun delete(entity: TransactionEntity) = dao.delete(entity)
}

class BudgetRepository(private val dao: BudgetDao) {
    fun observeWithCategory(): Flow<List<BudgetWithCategory>> = dao.observeWithCategory()
    suspend fun insert(entity: BudgetEntity): Long = dao.insert(entity)
    suspend fun update(entity: BudgetEntity) = dao.update(entity)
    suspend fun delete(entity: BudgetEntity) = dao.delete(entity)
}