package com.shakeexpense.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shakeexpense.app.data.database.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY created_at ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members ORDER BY created_at ASC")
    suspend fun getAllMembersSync(): List<FamilyMemberEntity>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): FamilyMemberEntity?

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    fun getMemberByIdFlow(id: String): Flow<FamilyMemberEntity?>

    @Query("SELECT * FROM family_members WHERE family_id = :familyId ORDER BY created_at ASC")
    fun getMembersByFamilyId(familyId: String): Flow<List<FamilyMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<FamilyMemberEntity>)

    @Update
    suspend fun updateMember(member: FamilyMemberEntity)

    @Delete
    suspend fun deleteMember(member: FamilyMemberEntity)

    @Query("SELECT COUNT(*) FROM family_members")
    suspend fun getMemberCount(): Int

    @Query("DELETE FROM family_members")
    suspend fun deleteAllMembers()

    @Query("UPDATE family_members SET privacy_mode = :privacyMode WHERE id = :id")
    suspend fun updateMemberPrivacyMode(id: String, privacyMode: String)

    @Query("""
        UPDATE family_members 
        SET share_transactions = :shareTransactions,
            share_monthly_total = :shareMonthlyTotal,
            share_category_totals = :shareCategoryTotals,
            receive_family_alerts = :receiveFamilyAlerts
        WHERE id = :id
    """)
    suspend fun updatePrivacySettings(
        id: String,
        shareTransactions: Boolean,
        shareMonthlyTotal: Boolean,
        shareCategoryTotals: Boolean,
        receiveFamilyAlerts: Boolean
    )

    @Query("UPDATE family_members SET is_exit_requested = :requested WHERE id = :id")
    suspend fun updateExitRequested(id: String, requested: Boolean)
}
