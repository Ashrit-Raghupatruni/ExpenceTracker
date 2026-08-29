package com.shakeexpense.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shakeexpense.app.data.database.entity.FamilyGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGroupDao {

    @Query("SELECT * FROM family_groups LIMIT 1")
    fun getActiveFamilyGroupFlow(): Flow<FamilyGroupEntity?>

    @Query("SELECT * FROM family_groups LIMIT 1")
    suspend fun getActiveFamilyGroup(): FamilyGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyGroup(familyGroup: FamilyGroupEntity)

    @Query("DELETE FROM family_groups")
    suspend fun clearFamilyGroups()

    @Query("DELETE FROM family_groups WHERE familyId = :familyId")
    suspend fun deleteFamilyGroup(familyId: String)

    @Query("UPDATE family_groups SET monthly_spending_limit_cents = :limitCents WHERE familyId = :familyId")
    suspend fun updateSpendingLimit(familyId: String, limitCents: Long)
}
