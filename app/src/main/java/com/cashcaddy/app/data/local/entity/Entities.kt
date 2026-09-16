package com.cashcaddy.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.cashcaddy.app.data.model.MoneyType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val type: String,
    val isActive: Boolean = true,
    val isSuggested: Boolean = false,
    val sortOrder: Int = 0,
) {
    val moneyType: MoneyType get() = MoneyType.fromStorage(type)
}

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("occurredAt")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val title: String,
    val categoryId: Long,
    val type: String,
    val occurredAt: Long,
) {
    val moneyType: MoneyType get() = MoneyType.fromStorage(type)
}

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId")],
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val limitMinor: Long,
    val yearMonth: String,
)

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
)

data class BudgetWithCategory(
    @Embedded val budget: BudgetEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
)