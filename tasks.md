# ShakeExpense: Freemium & Financial Safety System Implementation Tasks (	asks.md)

## Milestone 1: Data Layer & Entities (Schema v5) [COMPLETED]
- [x] Task 1.1: Create FinancialProfileEntity (user_id, monthly_income_cents, savings_target_cents, illing_cycle_day, 	ier, safety_score, updated_at).
- [x] Task 1.2: Create RecurringPaymentEntity (id, user_id, 
ame, mount_cents, cadence, category_id, last_charged_timestamp, 
ext_due_timestamp, is_auto_detected, is_active, merchant_key, created_at).
- [x] Task 1.3: Create FinancialProfileDao and RecurringPaymentDao.
- [x] Task 1.4: Update AppDatabase to version 5 with entities and DAOs.
- [x] Task 1.5: Create FinancialProfileRepository and RecurringPaymentRepository.

---

## Milestone 2: Core Financial Intelligence Engines [COMPLETED]
- [x] Task 2.1: Implement SafeToSpendCalculator:
  - Formula: CurrentBalance - (UpcomingBills + EMIs + Subscriptions + SavingsTarget).
  - Computes safeToSpendMonthlyCents and safeToSpendTodayCents (emaining / daysLeftInCycle).
- [x] Task 2.2: Implement RecurringPaymentDetector:
  - Recognizes subscription patterns (Netflix, Spotify, YouTube, Amazon Prime, Google One, Gym, SIP, EMI, Rent, Mobile Recharge).
  - Calculates annualized burden and flags unused subscriptions (>35 days since related activity).
- [x] Task 2.3: Implement FinancialSafetyScoreEngine (0–100 Score):
  - Sub-scores: Spending Pace (25 pts), Budget Discipline (25 pts), Subscription Burden (20 pts), Savings Buffer (15 pts), Risk Signals (15 pts).
  - Generates actionable primary recommendations and anomaly risk flags.
- [x] Task 2.4: Implement AIFinancialInsightsEngine:
  - Computes comparative month-over-month shifts.
  - Budget runway prediction.

---

## Milestone 3: UI Surfaces & Dashboards [COMPLETED]
- [x] Task 3.1: Add "Safe to Spend Today" signature ambient banner & badge on Tracker Screen.
- [x] Task 3.2: Create "Financial Safety Score" visual widget with 0–100 circular gauge, sub-score breakdown, and primary recommendation in Profile.
- [x] Task 3.3: Create "Subscriptions & Recurring Payments" management section with annualized cost summary and unused warnings.
- [x] Task 3.4: Create "Safe-to-Spend Budget Setup Dialog" on Tracker Screen to configure monthly income, savings targets, and billing cycle start day.

---

## Milestone 4: Verification & Release [COMPLETED]
- [x] Task 4.1: Write unit tests for SafeToSpendCalculatorTest.
- [x] Task 4.2: Run complete unit test suite (gradlew.bat testDebugUnitTest) with 100% pass rate.
- [x] Task 4.3: Build signed release APK (gradlew.bat assembleRelease) and install on test device.
- [x] Task 4.4: Runtime verification and final status report.

---

## Milestone 5: Monetization & Technical Specification v3.0 [COMPLETED]
- [x] Task 5.1: Create Subscription & Centralized Entitlement Models (FREE, PLUS, FAMILY_PRO).
- [x] Task 5.2: Enforce Strict "No Fake / Demo Data" Rule across models and engines.
- [x] Task 5.3: Implement Next-Month Expense Prediction Engine.
- [x] Task 5.4: Implement Predicted & User-Editable Monthly Spending Limit.
- [x] Task 5.5: Implement Event-Driven Spending Limit Notification Engine.
- [x] Task 5.6: Implement Advanced Search & CSV Export with Entitlement Gating.
- [x] Task 5.7: Update UI Surfaces with tiers and prediction cards.
- [x] Task 5.8: Write comprehensive unit tests for all new engines.

---

## Milestone 6: Safe-to-Spend Fix, Payment Gateway Flow & Database Integrity [COMPLETED]
- [x] Task 6.1: Fixed Safe-to-Spend Setup Persistence.
- [x] Task 6.2: Membership Change Payment Gateway Redirect (UPI Intent + Play Store fallback).
- [x] Task 6.3: Cleaned Profile Screen.
- [x] Task 6.4: Database Structural Integrity.

---

## Milestone 7: Full Matrix Alignment & Feature Verification [COMPLETED]
- [x] Task 7.1: Room Database Version 6 Migration.
- [x] Task 7.2: Entitlement System Gating.
- [x] Task 7.3: PLUS Intelligence Engines (AI Assistant, Unusual Spending Detector, Advanced Search, CSV Export).
- [x] Task 7.4: Family Pro Enhancements (5-member cap, PrivacyMode).
- [x] Task 7.5: Build, Test & APK Delivery.

---

## Milestone 8: Final Feature Completion, Schema v8 & Zero Fake Data Integrity [COMPLETED]
- [x] Task 8.1: Elimination of Static Sub-Score Pills.
- [x] Task 8.2: Zero-Fake-Data Safe-to-Spend & AI Assistant.
- [x] Task 8.3: Room Schema v8 & Child Exit Persistence.
- [x] Task 8.4: Singleton EntitlementManager & Live Flow Sync.
- [x] Task 8.5: Payment Gateway Redirection & Subscription Threshold Alignment.
- [x] Task 8.6: Pipeline Category Name Resolution.
- [x] Task 8.7: Comprehensive Regression.

---

## Milestone 9: Collapsible UI, Expense Isolation & Family Limit Sync [COMPLETED]
- [x] Task 9.1: Collapsible UI Controls on Tracker Screen.
- [x] Task 9.2: Master & Individual Help & Documentation Toggles.
- [x] Task 9.3: Personal Expense Tracker Isolation.
- [x] Task 9.4: Family Monthly Limit Fixes & Reactive Cloud Sync.
- [x] Task 9.5: Zero Data Reset & Regression Verification.

---

## Milestone 10: P0 Security & Secret Hardening [COMPLETED]
- [x] Task 10.1: Harden Firestore Security Rules (firestore.rules):
  - Enforce request.auth != null on all collections.
  - /expenses/{expenseUuid}: Strict user ownership (resource.data.userId == request.auth.uid or family member authorization).
  - /families/{familyId}: Authorized family member read, parent-only update/admin controls.
  - /family_members/{memberDocId}: Self-member management and parent governance on roles/exit requests.
- [x] Task 10.2: Remove hardcoded release signing credentials from app/build.gradle.kts:
  - Read from local.properties / environment variables with safe debug defaults.
  - Decouple debug signing from production release signing.

---

## Milestone 11: Architecture, DI & Presentation Layer Decoupling [COMPLETED]
- [x] Task 11.1: Create AppContainer / Dependency Injection structure to cleanly provide Repositories, UseCases, and ViewModels.
- [x] Task 11.2: Decouple MainActivity into a clean application shell and unified navigation host.
- [x] Task 11.3: Refine TrackerViewModel architecture, separating dashboard presentation, transaction streams, and search/insights.

---

## Milestone 12: Design System - Neumorphism + Glassmorphism Tokens & Primitives [COMPLETED]
- [x] Task 12.1: Design Tokens (Color.kt, Typography.kt with Tabular figures, Elevation.kt, Shapes.kt).
- [x] Task 12.2: Glassmorphic Information Primitives (GlassCard, GlassSurface, GlassTopBar, GlassBottomBar).
- [x] Task 12.3: Neumorphic Interactive Primitives (NeuButton, NeuIconButton, NeuSegmentedControl, NeuPill, NeuKeypadButton).
- [x] Task 12.4: Composite Financial Widgets (FinancialMetricCard, SafeToSpendHeroCard, InsightCard, CategoryChip).

---

## Milestone 13: UI Redesign - Home Dashboard & Navigation [COMPLETED]
- [x] Task 13.1: Redesign Tracker Tab into a **Dashboard-First Home Experience**:
  - Level 1: Hero Safe-to-Spend Runway & Monthly Budget Pacing glass card.
  - Level 2: Financial Safety Score & upcoming recurring obligations preview.
  - Level 3: Recent Transactions summary with 1-tap access to the Granular Spreadsheet screen.
  - Level 4: Floating Neumorphic Quick Action button.
- [x] Task 13.2: Floating Glassmorphic Bottom Navigation Bar with tactile Neumorphic active pill indicator.

---

## Milestone 14: Signature Flagship Quick-Entry HUD Redesign [COMPLETED]
- [x] Task 14.1: Translucent frosted glass HUD panel with depth and subtle light border.
- [x] Task 14.2: Large legible amount input with monospace/tabular financial typography.
- [x] Task 14.3: Tactile Neumorphic numeric keypad with haptic press feedback.
- [x] Task 14.4: Category pill selector with category notes text input.

---

## Milestone 15: Family Hub & Profile Redesign [IN PROGRESS / STABLE]
- [x] Task 15.1: Family Hub: Glass summary header, interactive member cards, privacy toggles, shared category budget envelopes with live visual progress.
- [x] Task 15.2: Profile: Grouped settings architecture (ACCOUNT, FINANCIAL, DEVICE, DATA, SUPPORT) reducing cognitive overload.

---

## Milestone 16: Verification, Testing & Build [COMPLETED]
- [x] Task 16.1: Run full unit test suite (./gradlew testDebugUnitTest) - 95/95 passing.
- [x] Task 16.2: Build clean APK (./gradlew assembleRelease).
- [x] Task 16.3: Runtime installation on connected device via adb install -r (data preserved).
