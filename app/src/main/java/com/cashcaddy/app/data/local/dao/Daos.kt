package com.cashcaddy.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cashcaddy.app.data.local.entity.BudgetEntity
import com.cashcaddy.app.data.local.entity.BudgetWithCategory
import com.cashcaddy.app.data.local.entity.CategoryEntity
import com.cashcaddy.app.data.local.entity.TransactionEntity
import com.cashcaddy.app.data.local.entity.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Insert
    suspend fun insertAll(entities: List<CategoryEntity>): List<Long>

    @Update
    suspend fun update(entity: CategoryEntity)

    @Delete
    suspend fun delete(entity: CategoryEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun usageCount(categoryId: Long): Int
}

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun observeWithCategory(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionWithCategory?

    @Insert
    suspend fun insert(entity: TransactionEntity): Long

    @Insert
    suspend fun insertAll(entities: List<TransactionEntity>)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}

@Dao
interface BudgetDao {
    @Transaction
    @Query("SELECT * FROM budgets ORDER BY id ASC")
    fun observeWithCategory(): Flow<List<BudgetWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BudgetEntity): Long

    @Update
    suspend fun update(entity: BudgetEntity)

    @Delete
    suspend fun delete(entity: BudgetEntity)

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun count(): Int
}