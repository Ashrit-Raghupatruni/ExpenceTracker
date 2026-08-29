# 📱 ShakeExpense — Personal & Family Financial Safety System

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin%201.9-blue.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Offline--First-orange.svg)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite%20v8-lightgrey.svg)](https://developer.android.com/training/data-storage/room)
[![Tests](https://img.shields.io/badge/Tests-95%20Passing%20(100%25)-success.svg)](https://github.com/Ashrit-Raghupatruni/ExpenceTracker)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

**ShakeExpense** is an ultra-fast, privacy-first personal and family financial safety system for Android. Built with Jetpack Compose and Room SQLite, it combines instantaneous gesture-based tracking (**Shake Anywhere HUD**), **100% on-device UPI/bank notification parsing**, a high-density **spreadsheet data grid**, and an enterprise-grade **Family Pro collaboration suite**.

---

## 🌟 Key Features & Highlights

### 📳 1. Shake-to-Log Translucent Overlay HUD
- Gently shake your device twice from **any app or the Android Home Screen**.
- An ultra-fast, frosted translucent overlay HUD (~90% backdrop dimming) pops up instantly.
- Features a quick category chip selector (Food, Transport, Groceries, Bills, Shopping, Entertainment, Others) and an integrated numeric keypad for frictionless logging under 3 seconds.
- Integrated with `ScreenStateReceiver` to automatically disable accelerometer sampling when the screen is turned off for zero idle battery drain.

### 🏦 2. 100% On-Device Bank Notification Detection
- Automatically parses incoming transaction SMS and notifications from major Indian banks and payment gateways (HDFC, SBI, ICICI, Axis, GPay, PhonePe, Paytm, CRED, Navi, BHIM, etc.).
- **Strict Privacy Guarantee**: 100% on-device regex extraction; raw message content or SMS bodies are **never** transmitted to cloud servers.
- Deduplication engine ($T \le 180\text{s}$, exact amount match) prevents double-logging across concurrent SMS and app push notifications.

### 🛡️ 3. Safe-to-Spend Daily Runway (Zero Fake Data)
- Dynamically computes your true disposable spending allowance for the day:
  $$\text{Safe Today} = \frac{\text{Monthly Income} - (\text{Upcoming Bills} + \text{Recurring Subscriptions} + \text{Savings Goal}) - \text{Spent So Far}}{\text{Days Remaining in Cycle}}$$
- **Strict Zero-Fake-Data Guarantee**: If no income is configured, safely shows ₹0 with a setup prompt instead of arbitrary baseline assumptions.

### 📊 4. Dynamic Financial Safety Score (0–100)
- Real-time algorithmic safety rating derived strictly from actual transactions:
  - **Spending Sub-Score**: Evaluated against discretionary category ratios (Shopping, Entertainment, Others).
  - **Budget Sub-Score**: Current month debit usage vs. planned monthly ceiling.
  - **Recurring Sub-Score**: Fixed subscription & bill obligations burden.
  - **Risk Signals Sub-Score**: Detection of budget overruns and unusual spending spikes.
- Returns a clean empty state when no records exist.

### 🤖 5. AI Financial Assistant & Predictions
- **Conversational Financial Q&A**: Ask natural language questions like *"Can I afford ₹2,500 this weekend?"* or *"How much did I spend on food this month?"*.
- **Next-Month Predictive Forecasting**: Uses moving averages and statistical regression based on verified transaction history to forecast upcoming obligations with confidence ratings.

### 🔔 6. Event-Driven Spending Limit Alerts & Anti-Spam
- Proactive heads-up push notifications triggered as soon as spending crosses **70%**, **80%**, **90%**, or **100%** of your monthly spending limit.
- **Anti-Spam Intelligence**: Alerts fire at most **once** per threshold per calendar month to eliminate notification fatigue. State automatically resets on the 1st of every month.

### 👨‍👩‍👧‍👦 7. Family Pro Collaboration & Child Exit Governance
- **Hard Cap of 5 Members**: Enforces family integrity on creation and join.
- **Shared Collective Budgets**: Set category spending caps for the whole household with live aggregated progress bars.
- **Granular Member Privacy Controls**: Each member can independently toggle `Share Transactions` (hide individual line items), `Share Monthly Total`, `Share Category Totals`, and `Receive Alerts`.
- **Persistent Child Exit Request Flow**: Children cannot detach unilaterally; exit requests are persisted in Room SQLite and marked with an amber badge awaiting explicit Parent approval.

### 📊 8. High-Density Spreadsheet Data Grid & CSV Export
- Comprehensive tabular ledger displaying Date, Category, Custom Payee/Merchant, Type (Debit/Credit), and Amount.
- Instant search and multi-parameter filtering across keywords, categories, and date ranges.
- One-tap CSV export via Android FileProvider and system share sheet.

### 🗑️ 9. Scoped Expense Reset & Cloud Deletion Tombstones
- Reset transactions by **Today**, **This Week**, or **Specific Date** (Material 3 DatePicker).
- Scoped strictly to your authenticated User ID, preserving Google accounts, family groups, and categories.
- Persistent deletion tombstones in `SyncEngine` prevent two-way sync from resurrecting deleted records.

---

## 💎 Subscription Tiers Matrix

ShakeExpense implements a centralized, singleton-driven entitlement architecture (`EntitlementManager`) with real-time reactive plan broadcasting:

| Feature / Capability | FREE (₹0) | PLUS (₹59/mo or ₹699/yr) | FAMILY PRO (₹99/mo or ₹999/yr) |
| :--- | :---: | :---: | :---: |
| **Instant Shake Overlay HUD** | ✅ | ✅ | ✅ |
| **Manual Expense Logging** | ✅ | ✅ | ✅ |
| **On-Device Bank SMS / UPI Detection** | ✅ | ✅ | ✅ |
| **Basic Spreadsheet & 6-Month History** | ✅ | ✅ | ✅ |
| **Safe-to-Spend Daily Runway** | ❌ | ✅ | ✅ |
| **Financial Safety Score (0–100)** | ❌ | ✅ | ✅ |
| **AI Financial Assistant (Q&A)** | ❌ | ✅ | ✅ |
| **Next-Month Expense Prediction** | ❌ | ✅ | ✅ |
| **70%, 80%, 90%, 100% Spending Alerts** | ❌ | ✅ | ✅ |
| **Recurring / Subscription Detector** | ❌ | ✅ | ✅ |
| **Advanced Search & CSV Export** | ❌ | ✅ | ✅ |
| **Shared Family Budgets** | ❌ | ❌ | ✅ |
| **5 Family Member Accounts** | ❌ | ❌ | ✅ |
| **Granular Privacy Controls** | ❌ | ❌ | ✅ |
| **Child Exit Governance & Approval** | ❌ | ❌ | ✅ |

---

## 🏗️ Technical Architecture & Tech Stack

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │ Translucent Overlay   │ │ Main Spreadsheet /    │ │ Bank Prompt   │ │
│  │ (QuickEntryActivity)  │ │ Family Hub (MainAct)  │ │ Heads-up HUD  │ │
│  └───────────┬───────────┘ └───────────┬───────────┘ └───────┬───────┘ │
│              │                         │                     │         │
│              ▼                         ▼                     ▼         │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │ QuickEntryViewModel   │ │ Tracker / Family VM   │ │ NotifPromptVM │ │
│  └───────────┬───────────┘ └───────────┬───────────┘ └───────┬───────┘ │
└──────────────┼─────────────────────────┼─────────────────────┼─────────┘
               │                         │                     │
┌──────────────▼─────────────────────────▼─────────────────────▼─────────┐
│                           REPOSITORY LAYER                             │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ ExpenseRepository │ CategoryRepository │ FamilyRepository        │  │
│  └───────────┬─────────────────────────┬────────────────────────────┘  │
└──────────────┼─────────────────────────┼───────────────────────────────┘
               │                         │
┌──────────────▼─────────────────────────▼───────────────────────────────┐
│                          SERVICES & DATA LAYER                         │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────┐ │
│  │ Room Database (SQLite)│ │ Sensor Service        │ │ Notification  │ │
│  │ Schema Version 8      │ │ Accelerometer Vector  │ │ Listener Svc  │ │
│  └───────────▲───────────┘ └───────────────────────┘ └───────────────┘ │
│              │                                                         │
│  ┌───────────▼──────────────────────────────────────────────────────┐  │
│  │ WorkManager Offline Sync Engine <──> Firebase Cloud Firestore    │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

- **Languages & Frameworks**: Kotlin 1.9, Jetpack Compose, Kotlin Coroutines, StateFlow.
- **Local Persistence**: Android Jetpack Room SQLite (Version 8) with automated migrations (`MIGRATION_5_6`, `MIGRATION_6_7`, `MIGRATION_7_8`).
- **Cloud Backend & Sync**: Firebase Authentication (Google Sign-In), Cloud Firestore with security rules, offline-first WorkManager synchronization.
- **Hardware Integration**: Android `SensorManager` (Accelerometer vector acceleration), `NotificationListenerService`.
- **Target SDK**: Min SDK 26 (Android 8.0), Target SDK 34 (Android 14).

---

## 🗄️ Database Schema Evolution (Room v8)

1. **`expenses`**: UUID, User ID, Category ID, Amount (integer cents/paise), Type (Debit/Credit), Source (Manual/Bank/Sync), Bank Ref, Timestamps, Sync Status.
2. **`categories`**: Default pre-populated categories with custom HEX colors and ordering.
3. **`family_members`**: ID, Family ID, Name, Role (Parent/Child), Device ID, `privacy_mode`, `share_transactions`, `share_monthly_total`, `share_category_totals`, `receive_family_alerts`, and `is_exit_requested`.
4. **`family_groups`**: Family ID, Family Name, Owner UID, Invite Code (`SHK-XXXX`), `monthly_spending_limit_cents`.
5. **`family_budgets`**: ID, Family ID, Category Name, Limit Cents, Updated Timestamp.
6. **`financial_profile`**: User ID, Monthly Income, Savings Target, Billing Cycle Day, Tier (`FREE`, `PLUS`, `FAMILY_PRO`), Spending Limit Cents.
7. **`recurring_payments`**: User ID, Name, Amount, Cadence, Last Charged, Next Due, Active status.

---

## 🛠️ Build & Installation Guide

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 34
- JDK 17 or JDK 21

### 1. Clone the Repository
```bash
git clone https://github.com/Ashrit-Raghupatruni/ExpenceTracker.git
cd ExpenceTracker
```

### 2. Run Automated Unit Tests
```bash
./gradlew testDebugUnitTest
```
*(All 95 unit tests execute and pass with 100% success rate).*

### 3. Assemble Debug APK
```bash
./gradlew assembleDebug
```
Output location: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Install onto Connected Android Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.shakeexpense.app/.ui.tracker.MainActivity
```

---

## 🛡️ Required Android Permissions & Setup

To unlock the full capabilities of ShakeExpense:
1. **Shake Anywhere (Display Over Other Apps)**:
   - Required to project the translucent Quick Entry overlay HUD from any external app or the Home Screen.
   - Configurable in **Profile $\to$ Permissions & Setup**.
2. **Bank Notification Detection (Notification Access)**:
   - Required to capture financial transaction alerts locally on-device.
   - Configurable in **Profile $\to$ Permissions & Setup**.

---

## 📚 Project Documentation Links

- **System Health Report**: [`HEALTH.md`](HEALTH.md)
- **User Guide & FAQ**: [`UserGuide.md`](UserGuide.md)
- **Development Roadmap & Tracker**: [`documentation/Tracker.md`](documentation/Tracker.md)
- **Living Consistency Audit**: [`documentation/Uptodate.md`](documentation/Uptodate.md)
- **Database Schema Specification**: [`documentation/schema.md`](documentation/schema.md)
- **Technical Specification**: [`documentation/TechSpec.md`](documentation/TechSpec.md)
- **Product Requirements Document (PRD)**: [`documentation/PRD.md`](documentation/PRD.md)
- **User Journeys & State Transitions**: [`documentation/Appflow.md`](documentation/Appflow.md)
- **UI/UX Design Tokens & System**: [`documentation/Design.md`](documentation/Design.md)

---

## 📄 License
Proprietary. Developed by Antigravity AI & the ShakeExpense Team.
