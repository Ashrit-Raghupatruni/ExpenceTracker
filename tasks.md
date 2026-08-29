# ShakeExpense: Freemium & Financial Safety System Implementation Tasks (`tasks.md`)

## Milestone 1: Data Layer & Entities (Schema v5)
- [x] Task 1.1: Create `FinancialProfileEntity` (`user_id`, `monthly_income_cents`, `savings_target_cents`, `billing_cycle_day`, `tier`, `safety_score`, `updated_at`).
- [x] Task 1.2: Create `RecurringPaymentEntity` (`id`, `user_id`, `name`, `amount_cents`, `cadence`, `category_id`, `last_charged_timestamp`, `next_due_timestamp`, `is_auto_detected`, `is_active`, `merchant_key`, `created_at`).
- [x] Task 1.3: Create `FinancialProfileDao` and `RecurringPaymentDao`.
- [x] Task 1.4: Update `AppDatabase` to version 5 with entities and DAOs.
- [x] Task 1.5: Create `FinancialProfileRepository` and `RecurringPaymentRepository`.

---

## Milestone 2: Core Financial Intelligence Engines
- [x] Task 2.1: Implement `SafeToSpendCalculator`:
  - Formula: `CurrentBalance - (UpcomingBills + EMIs + Subscriptions + SavingsTarget)`.
  - Computes `safeToSpendMonthlyCents` and `safeToSpendTodayCents` (`remaining / daysLeftInCycle`).
- [x] Task 2.2: Implement `RecurringPaymentDetector`:
  - Recognizes subscription patterns (Netflix, Spotify, YouTube, Amazon Prime, Google One, Gym, SIP, EMI, Rent, Mobile Recharge).
  - Calculates annualized burden and flags unused subscriptions (>35 days since related activity).
- [x] Task 2.3: Implement `FinancialSafetyScoreEngine` (0–100 Score):
  - Sub-scores: Spending Pace (25 pts), Budget Discipline (25 pts), Subscription Burden (20 pts), Savings Buffer (15 pts), Risk Signals (15 pts).
  - Generates actionable primary recommendations and anomaly risk flags.
- [x] Task 2.4: Implement `AIFinancialInsightsEngine`:
  - Computes comparative month-over-month shifts (e.g., *"Spent ₹1,840 more on food (+27% above normal)"*).
  - Budget runway prediction (e.g., *"At current rate, food budget will exhaust in 4 days; reduce by ₹85/day"*).

---

## Milestone 3: UI Surfaces & Dashboards
- [x] Task 3.1: Add "Safe to Spend Today" signature ambient banner & badge on Tracker Screen.
- [x] Task 3.2: Create "Financial Safety Score" visual widget with 0–100 circular gauge, sub-score breakdown, and primary recommendation in Profile.
- [x] Task 3.3: Create "Subscriptions & Recurring Payments" management section with annualized cost summary and unused warnings.
- [x] Task 3.4: Create "Safe-to-Spend Budget Setup Dialog" on Tracker Screen to configure monthly income, savings targets, and billing cycle start day.

---

## Milestone 4: Verification & Release
- [x] Task 4.1: Write unit tests for `SafeToSpendCalculatorTest`.
- [x] Task 4.2: Run complete unit test suite (`gradlew.bat testDebugUnitTest`) with 100% pass rate.
- [x] Task 4.3: Build signed release APK (`gradlew.bat assembleRelease`) and install on test device.
- [x] Task 4.4: Runtime verification and final status report.

---

## Milestone 5: Monetization & Technical Specification v3.0
- [x] Task 5.1: Create Subscription & Centralized Entitlement Models:
  - Tiers: `FREE` (₹0), `PLUS` (₹59/mo or ₹699/yr), `FAMILY_PRO` (₹99/mo or ₹999/yr).
  - Statuses: `ACTIVE`, `EXPIRED`, `CANCELLED`, `GRACE_PERIOD`, `PAUSED`.
  - `EntitlementManager`: Capabilities `ADVANCED_SEARCH`, `UNUSUAL_SPENDING_ALERTS`, `CSV_EXPORT`, `AI_ASSISTANT`, `EXPENSE_PREDICTION`, `EDITABLE_SPENDING_LIMIT`, `FAMILY_DASHBOARD`, etc.
- [x] Task 5.2: Enforce Strict "No Fake / Demo Data" Rule:
  - Remove sample recurring seeds in `ProfileViewModel`.
  - Display clean empty states when no transactions exist ("No expenses yet", "Not enough data to calculate Financial Safety Score or predict expenses yet").
- [x] Task 5.3: Implement Next-Month Expense Prediction Engine:
  - Level 1: Insufficient data (< 1 month) $\to$ "More spending history is needed".
  - Level 2: Statistical moving average, current run-rate, and active recurring obligations (Confidence: Moderate).
  - Level 3: Time-series regression when >3 months of history exist (Confidence: High).
- [x] Task 5.4: Implement Predicted & User-Editable Monthly Spending Limit:
  - Recommends: Fixed Income - Predicted Expenses $\to$ Recommended Limit & Potential Savings.
  - Allows user to manually change/override limit at any time.
  - Tracks usage percentage (`currentMonthExpenses / activeLimit * 100`).
- [x] Task 5.5: Implement Event-Driven Spending Limit Notification Engine:
  - Configurable warning thresholds: 70%, 80%, 90%, 100%.
  - State tracking prevents duplicate/spam notifications during the month.
  - Monthly reset on 1st of month.
  - Family limit alert for Family Pro when family threshold is crossed.
- [x] Task 5.6: Implement Advanced Search & CSV Export with Entitlement Gating:
  - Search by merchant, category, amount range, date range, type, and keywords.
  - CSV export for transactions with authorized user data scoping.
  - Gated behind PLUS / FAMILY PRO; triggers upgrade prompt for FREE users.
- [x] Task 5.7: Update UI Surfaces:
  - Subscription Plan Card in Profile (`FREE`, `PLUS`, `FAMILY PRO`).
  - Monthly Spending Limit & Progress bar in Tracker.
  - Prediction Card with Confidence indicator.
- [x] Task 5.8: Write comprehensive unit tests for all new engines and run full regression suite.

### Milestone 5 Verification & Clean Compilation Notes
- App Version updated: `versionCode = 2`, `versionName = "2.0.0"`.
- App Info in Profile screen displays `Version 2.0.0 (Build 2) • Release • Personal Financial Safety System`.
- Suppressed legacy deprecation warnings cleanly with `@file:Suppress("DEPRECATION")` across `MainActivity.kt`, `AuthRepository.kt`, and `ShakeSensorService.kt`.
- Compiler warnings: 0 warnings emitted.
- All unit tests (`EntitlementManagerTest`, `NextMonthExpensePredictorTest`, `MonthlySpendingLimitManagerTest`, `AdvancedTransactionSearchUseCaseTest`, `ExportTransactionsCsvUseCaseTest`, `SafeToSpendCalculatorTest`, `FinancialSafetyScoreEngineTest`, `RecurringPaymentDetectorTest`, `ExpenseResetUseCaseTest`) passed cleanly.
- `assembleRelease` succeeded with signed APK created at `app/build/outputs/apk/release/app-release.apk`.

---

## Milestone 6: Safe-to-Spend Fix, Payment Gateway Flow & Database Integrity
- [x] Task 6.1: Fixed Safe-to-Spend Setup Persistence:
  - Resolved `FinancialProfileDao` update mismatch on fresh users by implementing full upsert logic (`insertOrUpdateProfile`).
  - Added default fallback linking between `targetUserId` and `default_local_user` in `FinancialProfileRepositoryImpl`.
  - Immediate reactive recalculation upon saving monthly income and savings target in `TrackerViewModel`.
- [x] Task 6.2: Membership Change Payment Gateway Redirect:
  - Integrated `MembershipPaymentDialog` opening upon selecting `PLUS` or `FAMILY PRO`.
  - Frequency selection: Monthly vs Yearly (15% savings).
  - Gateway trigger: Launches Android UPI Intent (`upi://pay`) with fallback to Play Store subscription gateway.
  - Test activation option for instant verification.
- [x] Task 6.3: Cleaned Profile Screen:
  - Removed in-app Version/Build card from Profile Screen (retained exclusively in Android OS App Info).
- [x] Task 6.4: Database Structural Integrity:
  - Ensured initial profile seeding on database creation and on database open.
  - Safe upsert on budget parameters, safety score, and tier.
  - Installed and verified on mobile device `10BE7A08JP0007U`.

---

## Milestone 7: Full Matrix Alignment & Feature Verification
- [x] Task 7.1: Room Database Version 6 Migration:
  - Added `MIGRATION_5_6` with `monthly_spending_limit_cents` on `financial_profile` and `family_groups`, and `privacy_mode` on `family_members`.
  - Upgraded Room schema version to 6.
- [x] Task 7.2: Entitlement System Gating:
  - Enforced single-source of truth via `EntitlementManager` across ViewModels.
  - Free users strictly gated from AI Financial Assistant, Advanced Search, CSV Export, Predictions, Custom Spending Limits, Unusual Spending Alerts, Family Admin Limits, and Privacy Modes.
  - Paywall dialog displayed upon accessing locked features on Free tier.
- [x] Task 7.3: PLUS Intelligence Engines:
  - Created `AiFinancialAssistantUseCase` for on-device natural language financial intelligence based on Room records.
  - Created `UnusualSpendingDetector` with alerts for duplicate charges within 10 minutes and transaction spikes.
  - Created `FinancialSafetyScoreEngine` for dynamic 0-100 financial health rating (strictly zero demo data).
  - Created `RecurringPaymentDetector` for subscription detection.
  - Integrated `AdvancedTransactionSearchUseCase` live search UI in `SpreadsheetScreen`.
  - Integrated FileProvider-based `ExportTransactionsCsvUseCase` for one-tap CSV sharing.
- [x] Task 7.4: Family Pro Enhancements:
  - Enforced strict 5-member limit in `AddFamilyMemberUseCase` and `JoinFamilyGroupUseCase`.
  - Integrated granular `PrivacyMode` (`PRIVATE`, `SHARED_SUMMARY`, `FULL_SHARED`) to filter member details in family aggregates.
- [x] Task 7.5: Build, Test & APK Delivery:
  - All 84+ automated unit tests passing (`./gradlew testDebugUnitTest`).
  - Signed release APK built (`./gradlew assembleRelease`) and installed via ADB onto device `10BE7A08JP0007U`.

---

## Milestone 8: Final Feature Completion, Schema v8 & Zero Fake Data Integrity
- [x] Task 8.1: Elimination of Static Sub-Score Pills:
  - Replaced hardcoded text badges in `ProfileScreen` with dynamic sub-scores from `FinancialSafetyScoreEngine`: `spendingSubScore`, `budgetSubScore`, `recurringSubScore`, and `riskSignalsSubScore`.
  - Exposed sub-scores through `ProfileState` and populated them reactively in `ProfileViewModel`.
- [x] Task 8.2: Zero-Fake-Data Safe-to-Spend & AI Assistant:
  - Removed ₹50,000 baseline in `SafeToSpendCalculator`; returns ₹0 safe daily and informs user to configure income.
  - Removed phantom ₹15,000 budget assumption in `AiFinancialAssistantUseCase`.
- [x] Task 8.3: Room Schema v8 & Child Exit Persistence:
  - Added `MIGRATION_7_8` adding `is_exit_requested` column on `family_members`.
  - Implemented `RequestChildExitUseCase` to update Room SQLite database via `FamilyRepository`.
  - Added "Exit Requested" tag on `FamilyMemberCard` and parent approval / dismissal buttons.
- [x] Task 8.4: Singleton EntitlementManager & Live Flow Sync:
  - Provided singleton `EntitlementManager` from `ShakeExpenseApp`.
  - Connected `MainActivity` lifecycle coroutine to observe `FinancialProfileDao.getProfileFlow()` and broadcast plan upgrades.
  - Injected singleton `EntitlementManager` into `FamilyViewModel`.
- [x] Task 8.5: Payment Gateway Redirection & Subscription Threshold Alignment:
  - Configured production merchant UPI intent with fallback to Google Play subscriptions.
  - Aligned unused recurring subscription notification threshold math to 45 days matching user alert text.
- [x] Task 8.6: Pipeline Category Name Resolution:
  - Injected `CategoryDao` into `TransactionEventPipeline` to map authentic category names from database.
- [x] Task 8.7: Comprehensive Regression:
  - 95/95 unit tests passing (`./gradlew testDebugUnitTest`).
  - APK built (`./gradlew assembleDebug`) and installed to physical device `10BE7A08JP0007U`.


