package com.shakeexpense.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shakeexpense.app.data.database.entity.FamilyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyBudgetDao {

    @Query("SELECT * FROM family_budgets WHERE family_id = :familyId ORDER BY category_name ASC")
    fun getFamilyBudgetsFlow(familyId: String): Flow<List<FamilyBudgetEntity>>

    @Query("SELECT * FROM family_budgets WHERE family_id = :familyId")
    suspend fun getFamilyBudgetsSync(familyId: String): List<FamilyBudgetEntity>

    @Query("SELECT * FROM family_budgets WHERE family_id = :familyId AND category_name = :categoryName LIMIT 1")
    suspend fun getBudgetForCategory(familyId: String, categoryName: String): FamilyBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: FamilyBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudgets(budgets: List<FamilyBudgetEntity>)

    @Query("DELETE FROM family_budgets WHERE id = :id")
    suspend fun deleteBudget(id: String)

    @Query("DELETE FROM family_budgets WHERE family_id = :familyId AND category_name = :categoryName")
    suspend fun deleteBudgetByCategory(familyId: String, categoryName: String)

    @Query("DELETE FROM family_budgets WHERE family_id = :familyId")
    suspend fun clearBudgetsForFamily(familyId: String)
}
