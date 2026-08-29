# Data Models & Persistence Schema (schema.md)

**Project Name:** ShakeExpense  
**Document Status:** Decided / Living Document  
**Version:** 1.2.0  
**Last Updated:** 2026-08-24  

---

## 1. Persistence Overview

ShakeExpense stores all data locally on-device using SQLite via Android Jetpack Room with offline-first synchronization capabilities for family groups.

**Database Name:** `shake_expense.db`  
**Initial Schema Version:** `2`

---

## 2. Entity Relational Diagram (ERD)

```
┌──────────────────────────────────────┐
│           family_members             │
├──────────────────────────────────────┤
│ PK  id            TEXT NOT NULL      │
│     family_id     TEXT NOT NULL      │
│     name          TEXT NOT NULL      │
│     role          TEXT NOT NULL      │ -- 'PARENT' | 'CHILD'
│     device_id     TEXT NOT NULL      │
│     created_at    INTEGER NOT NULL   │
└──────────────────┬───────────────────┘
                   │ 1
                   │
                   │ N
┌──────────────────▼───────────────────┐       ┌──────────────────────────────────────┐
│              expenses                │       │             categories               │
├──────────────────────────────────────┤       ├──────────────────────────────────────┤
│ PK  id            INTEGER AUTOINCR   │       │ PK  id            INTEGER AUTOINCR   │
│     uuid          TEXT NOT NULL UNIQ │       │     name          TEXT NOT NULL UNIQ │
│ FK  user_id       TEXT NOT NULL      ├───────┤     color_hex     TEXT NOT NULL      │
│ FK  category_id   INTEGER NOT NULL   │  N  1 │     is_default    INTEGER (0/1)      │
│     amount_cents  INTEGER NOT NULL   │       │     display_order INTEGER NOT NULL   │
│     type          TEXT NOT NULL      │       └──────────────────────────────────────┘
│     source        TEXT NOT NULL      │ -- 'MANUAL_SHAKE' | 'BANK_NOTIF' | 'SYNC'
│     custom_name   TEXT NULLABLE      │
│     bank_ref      TEXT NULLABLE      │
│     timestamp     INTEGER NOT NULL   │
│     updated_at    INTEGER NOT NULL   │
│     sync_status   TEXT NOT NULL      │ -- 'PENDING' | 'SYNCED'
└──────────────────────────────────────┘
```

---

## 3. Database Table Definitions

### 3.1. `categories` Table (`CategoryEntity`)

| Column | SQL Type | Kotlin Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `Long` | `PRIMARY KEY AUTOINCREMENT` | Unique category identifier |
| `name` | `TEXT` | `String` | `NOT NULL UNIQUE` | Category display name |
| `color_hex` | `TEXT` | `String` | `NOT NULL` | Visual color accent (`#RRGGBB`) |
| `is_default` | `INTEGER` | `Boolean` | `NOT NULL DEFAULT 1` | `1` for built-in category |
| `display_order` | `INTEGER` | `Int` | `NOT NULL DEFAULT 0` | Ordering on the quick-entry overlay |

#### Pre-populated Seed Categories:
```sql
INSERT INTO categories (name, color_hex, is_default, display_order) VALUES
('Food', '#F59E0B', 1, 1),
('Transport', '#3B82F6', 1, 2),
('Groceries', '#10B981', 1, 3),
('Bills', '#8B5CF6', 1, 4),
('Shopping', '#F43F5E', 1, 5),
('Entertainment', '#A855F7', 1, 6),
('Others', '#64748B', 1, 99);
```

---

### 3.2. `family_members` Table (`FamilyMemberEntity`)

| Column | SQL Type | Kotlin Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `String` | `PRIMARY KEY` | Member UUID / User ID |
| `family_id` | `TEXT` | `String` | `NOT NULL` | Shared family group ID |
| `name` | `TEXT` | `String` | `NOT NULL` | Member display name (e.g., "Rahul") |
| `role` | `TEXT` | `String` | `NOT NULL CHECK(role IN ('PARENT', 'CHILD'))` | Permission role |
| `device_id` | `TEXT` | `String` | `NOT NULL` | Unique installation / hardware ID |
| `created_at`| `INTEGER` | `Long` | `NOT NULL` | Timestamp member joined |

---

### 3.3. `expenses` Table (`ExpenseEntity`)

| Column | SQL Type | Kotlin Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `Long` | `PRIMARY KEY AUTOINCREMENT` | Local autoincrement ID |
| `uuid` | `TEXT` | `String` | `NOT NULL UNIQUE` | Globally unique ID for sync |
| `user_id` | `TEXT` | `String` | `NOT NULL, FK -> family_members(id)` | Author of the expense |
| `category_id` | `INTEGER` | `Long` | `NOT NULL, FK -> categories(id)` | Foreign key reference to category |
| `amount_cents`| `INTEGER` | `Long` | `NOT NULL CHECK (amount_cents > 0)` | Minor currency units (cents/paise) |
| `type` | `TEXT` | `String` | `NOT NULL DEFAULT 'DEBIT'` | `DEBIT` or `CREDIT` |
| `source` | `TEXT` | `String` | `NOT NULL DEFAULT 'MANUAL_SHAKE'` | `MANUAL_SHAKE`, `BANK_NOTIF`, `SYNC` |
| `custom_name` | `TEXT` | `String?` | `NULLABLE` | Custom label if "Others" or bank merchant |
| `bank_ref` | `TEXT` | `String?` | `NULLABLE` | Bank sender / reference info |
| `timestamp` | `INTEGER` | `Long` | `NOT NULL` | Epoch millisecond of transaction |
| `updated_at` | `INTEGER` | `Long` | `NOT NULL` | Timestamp for LWW sync |
| `sync_status`| `TEXT` | `String` | `NOT NULL DEFAULT 'PENDING'` | `PENDING` or `SYNCED` |

#### Database Indices:
```sql
CREATE INDEX idx_expenses_timestamp ON expenses(timestamp DESC);
CREATE INDEX idx_expenses_user ON expenses(user_id);
CREATE INDEX idx_expenses_sync_status ON expenses(sync_status);
```

---

## 4. Key DAO SQL Queries

### 4.1. Tabular Spreadsheet Stream Query
```sql
SELECT 
    e.id AS expense_id,
    e.uuid AS expense_uuid,
    e.user_id AS user_id,
    m.name AS user_name,
    e.amount_cents AS amount_cents,
    e.type AS transaction_type,
    e.source AS transaction_source,
    e.timestamp AS timestamp,
    e.custom_name AS custom_name,
    c.id AS category_id,
    c.name AS category_name,
    c.color_hex AS category_color,
    e.sync_status AS sync_status
FROM expenses e
INNER JOIN categories c ON e.category_id = c.id
LEFT JOIN family_members m ON e.user_id = m.id
ORDER BY e.timestamp DESC;
```

### 4.2. Member Specific Expense Summary Query (Parental Control)
Computes cumulative total spent and transaction count for a single member:
```sql
SELECT 
    e.user_id AS user_id,
    m.name AS user_name,
    SUM(CASE WHEN e.type = 'DEBIT' THEN e.amount_cents ELSE 0 END) AS total_debit_cents,
    SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount_cents ELSE 0 END) AS total_credit_cents,
    COUNT(e.id) AS transaction_count
FROM expenses e
INNER JOIN family_members m ON e.user_id = m.id
WHERE e.user_id = :targetUserId;
```

### 4.3. Member Category Breakdown Query
```sql
SELECT 
    c.id AS category_id,
    c.name AS category_name,
    c.color_hex AS category_color,
    SUM(e.amount_cents) AS category_total_cents,
    COUNT(e.id) AS transaction_count
FROM expenses e
INNER JOIN categories c ON e.category_id = c.id
WHERE e.user_id = :targetUserId AND e.type = 'DEBIT'
GROUP BY c.id
ORDER BY category_total_cents DESC;
```

### 4.4. Pending Offline Sync Queue Query
```sql
SELECT * FROM expenses 
WHERE sync_status = 'PENDING' 
ORDER BY updated_at ASC;
```

---

## 5. Domain Models (Proposed)

```kotlin
enum class TransactionType { DEBIT, CREDIT }
enum class TransactionSource { MANUAL_SHAKE, BANK_NOTIFICATION, SYNC }
enum class SyncStatus { PENDING, SYNCED }
enum class FamilyRole { PARENT, CHILD }

data class FamilyMember(
    val id: String,
    val familyId: String,
    val name: String,
    val role: FamilyRole,
    val deviceId: String
)

data class Expense(
    val id: Long = 0,
    val uuid: String,
    val userId: String,
    val userName: String? = null,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val amountCents: Long,
    val type: TransactionType = TransactionType.DEBIT,
    val source: TransactionSource = TransactionSource.MANUAL_SHAKE,
    val customName: String? = null,
    val bankRef: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    val displayCategory: String
        get() = if (categoryName.equals("Others", ignoreCase = true) && !customName.isNullOrBlank()) {
            customName
        } else {
            categoryName
        }

    val formattedAmount: Double
        get() = amountCents / 100.0
}

data class MemberSpendingSummary(
    val userId: String,
    val userName: String,
    val role: FamilyRole,
    val totalDebitCents: Long,
    val totalCreditCents: Long,
    val transactionCount: Int,
    val categoryBreakdown: List<CategorySubtotal>
)

data class CategorySubtotal(
    val categoryId: Long,
    val categoryName: String,
    val colorHex: String,
    val totalCents: Long,
    val count: Int
)

enum class Cadence { MONTHLY, QUARTERLY, YEARLY }
enum class SubscriptionTier { FREE, PREMIUM, FAMILY }

data class RecurringPayment(
    val id: Long = 0,
    val name: String,
    val amountCents: Long,
    val cadence: Cadence = Cadence.MONTHLY,
    val categoryId: Long = 4L, // Bills default
    val lastChargedTimestamp: Long,
    val nextDueTimestamp: Long,
    val isAutoDetected: Boolean = true,
    val isActive: Boolean = true,
    val merchantKey: String? = null
)

data class FinancialSafetyScore(
    val score: Int, // 0 to 100
    val spendingPaceScore: Int,
    val budgetScore: Int,
    val subscriptionBurdenScore: Int,
    val savingsBufferScore: Int,
    val riskSignalsScore: Int,
    val riskFlags: List<String>,
    val safeToSpendTodayCents: Long,
    val safeToSpendMonthlyCents: Long,
    val primaryOpportunity: String
)
```

---

## 6. Financial Safety & Recurring Tables (v2.0 Proposed)

### 6.1. `recurring_payments` Table
```sql
CREATE TABLE recurring_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    amount_cents INTEGER NOT NULL,
    cadence TEXT NOT NULL DEFAULT 'MONTHLY',
    category_id INTEGER NOT NULL DEFAULT 4,
    last_charged_timestamp INTEGER NOT NULL,
    next_due_timestamp INTEGER NOT NULL,
    is_auto_detected INTEGER NOT NULL DEFAULT 1,
    is_active INTEGER NOT NULL DEFAULT 1,
    merchant_key TEXT,
    created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
);
```

### 6.2. `financial_profile` Table
```sql
CREATE TABLE financial_profile (
    user_id TEXT PRIMARY KEY,
    monthly_income_cents INTEGER NOT NULL DEFAULT 0,
    savings_target_cents INTEGER NOT NULL DEFAULT 0,
    billing_cycle_day INTEGER NOT NULL DEFAULT 1,
    tier TEXT NOT NULL DEFAULT 'FREE',
    safety_score INTEGER NOT NULL DEFAULT 80,
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
);
```

---

## 8. Schema Version 7 & 8 Enhancements (Family Budgets, Granular Privacy & Child Exit Persistence)

### 8.1. `family_budgets` Table (`FamilyBudgetEntity`) — Added in v7
```sql
CREATE TABLE IF NOT EXISTS family_budgets (
    id TEXT PRIMARY KEY NOT NULL,
    family_id TEXT NOT NULL,
    category_name TEXT NOT NULL,
    limit_cents INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

| Column | SQL Type | Kotlin Type | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `TEXT` | `String` | `PRIMARY KEY` | Unique budget identifier (`fambudget_<uuid>`) |
| `family_id` | `TEXT` | `String` | `NOT NULL` | Associated family group ID |
| `category_name` | `TEXT` | `String` | `NOT NULL` | Budget category (e.g., "Food", "Groceries") |
| `limit_cents` | `INTEGER` | `Long` | `NOT NULL` | Monthly spending cap in integer paise/cents |
| `updated_at` | `INTEGER` | `Long` | `NOT NULL` | Epoch timestamp of last modification |

---

### 8.2. `family_members` Table Schema Evolution (v6, v7, v8)
```sql
-- Version 6: Added high-level privacy mode
ALTER TABLE family_members ADD COLUMN privacy_mode TEXT NOT NULL DEFAULT 'FULL_SHARED';

-- Version 7: Added granular per-member privacy settings
ALTER TABLE family_members ADD COLUMN share_transactions INTEGER NOT NULL DEFAULT 1;
ALTER TABLE family_members ADD COLUMN share_monthly_total INTEGER NOT NULL DEFAULT 1;
ALTER TABLE family_members ADD COLUMN share_category_totals INTEGER NOT NULL DEFAULT 1;
ALTER TABLE family_members ADD COLUMN receive_family_alerts INTEGER NOT NULL DEFAULT 1;

-- Version 8: Added child exit request persistence
ALTER TABLE family_members ADD COLUMN is_exit_requested INTEGER NOT NULL DEFAULT 0;
```

| New Column | SQL Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `privacy_mode` | `TEXT` | `'FULL_SHARED'` | Enum: `PRIVATE`, `SHARED_SUMMARY`, `FULL_SHARED` |
| `share_transactions` | `INTEGER` (Boolean) | `1` | If 0, child/member individual line items are hidden from parents |
| `share_monthly_total` | `INTEGER` (Boolean) | `1` | If 0, individual monthly totals are masked in family breakdown |
| `share_category_totals` | `INTEGER` (Boolean) | `1` | If 0, category distribution is hidden |
| `receive_family_alerts` | `INTEGER` (Boolean) | `1` | Controls delivery of collective family limit push notifications |
| `is_exit_requested` | `INTEGER` (Boolean) | `0` | Flagged when child member submits exit request awaiting parent approval |

---

### 8.3. Monthly Spending Limits Evolution
```sql
-- Version 6: Added customizable monthly spending limits
ALTER TABLE financial_profile ADD COLUMN monthly_spending_limit_cents INTEGER NOT NULL DEFAULT 0;
ALTER TABLE family_groups ADD COLUMN monthly_spending_limit_cents INTEGER NOT NULL DEFAULT 0;
```

---

### 8.4. Migration Definitions in Code (`AppDatabase.kt`)
```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE financial_profile ADD COLUMN monthly_spending_limit_cents INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE family_groups ADD COLUMN monthly_spending_limit_cents INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE family_members ADD COLUMN privacy_mode TEXT NOT NULL DEFAULT 'FULL_SHARED'")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS family_budgets (
                id TEXT PRIMARY KEY NOT NULL,
                family_id TEXT NOT NULL,
                category_name TEXT NOT NULL,
                limit_cents INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("ALTER TABLE family_members ADD COLUMN share_transactions INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE family_members ADD COLUMN share_monthly_total INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE family_members ADD COLUMN share_category_totals INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE family_members ADD COLUMN receive_family_alerts INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE family_members ADD COLUMN is_exit_requested INTEGER NOT NULL DEFAULT 0")
    }
}
```

---

## 9. Cross References
- System Health: [Health.md](Health.md)
- Requirements: [PRD.md](PRD.md)
- User flows: [Appflow.md](Appflow.md)
- Visual presentation: [Design.md](Design.md)
- Technical architecture: [TechSpec.md](TechSpec.md)
- Development principles: [Rules.md](Rules.md)
- Implementation status: [Tracker.md](Tracker.md)
- Consistency checkpoint: [Uptodate.md](Uptodate.md)

