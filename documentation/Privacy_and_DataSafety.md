# ShakeExpense: Privacy Policy & Google Play Data Safety Documentation

**Effective Date:** August 24, 2026  
**Application Name:** ShakeExpense - Smart Expense Tracker & Family Budget  
**Developer:** ShakeExpense Team  
**Contact / Privacy Inquiry:** privacy@shakeexpense.app  

---

## 1. Overview & Privacy Philosophy
ShakeExpense is built with an **Offline-First & Privacy-By-Design Architecture**. All daily expenses, categorized spending logs, and local budgets are stored securely on your local Android device in a private SQLite database (Room).

- **No Third-Party Advertising / Data Brokers:** ShakeExpense does not sell, rent, or monetize your personal or financial data.
- **On-Device Financial Parsing:** Bank & UPI notification detection operates **100% on-device** using strictly local regular expression matching. Raw notifications, bank account numbers, OTPs, and personal messages are never transmitted over the internet or logged to remote servers.
- **End-to-End Encrypted Cloud Sync (Optional):** Multi-device synchronization and Family Sharing use Firebase Authentication and Google Cloud Firestore with HTTPS/TLS in-transit encryption and isolated tenant data isolation.

---

## 2. Google Play Data Safety Declarations

| Data Category | Data Type | Purpose | Shared / Collected | Optional / Required |
|---|---|---|---|---|
| **Personal Info** | Name | User Profile & Family identification | Collected (stored in Firestore if family sync enabled) | Optional (Guest mode available) |
| **Personal Info** | Email address | Authentication & account recovery | Collected (Google Sign-In via Firebase Auth) | Optional |
| **Personal Info** | User IDs (Firebase UID) | Account ownership & multi-device sync | Collected | Optional |
| **Financial Info** | Purchase & Expense history | Personal budget tracking, summaries & family synchronization | Collected & stored locally / Firestore | Required for core functionality |
| **App Info & Performance** | Crash logs & diagnostic metrics | App stability and performance optimization | Collected anonymously | Optional |

### Security Practices
- **Data Encrypted in Transit:** All network requests between your device and Google Cloud Firestore / Firebase Auth utilize HTTPS (TLS 1.3).
- **Data Deletion Mechanism:** Users can delete individual expenses, detach family members, or sign out and request total account deletion by sending a request to `privacy@shakeexpense.app` or via the in-app delete controls.
- **Family Isolation:** Cloud Firestore security rules strictly isolate family groups so no unauthorized user can access another family's budget records.

---

## 3. Permissions Justification (Google Play Policy Compliance)

1. **`SYSTEM_ALERT_WINDOW` (Display over other apps):**
   - *Reason:* Allows the floating Quick Expense Entry HUD to appear instantly over other applications upon an accelerometer shake gesture.
   - *User Control:* Users are prompted to explicitly grant this permission with an in-app toggle and guided settings flow.

2. **`BIND_NOTIFICATION_LISTENER_SERVICE` (Notification Access):**
   - *Reason:* Parses bank debit/credit confirmation SMS notifications strictly on-device to suggest one-tap expense logging.
   - *Privacy Guarantee:* Only debit/credit notifications containing financial keywords (INR, Rs, Debited, Credited) are processed locally. Non-banking notifications are immediately discarded.

3. **`FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`:**
   - *Reason:* Maintains background shake sensor detection when the device screen is active, ensuring instant HUD response. Automatically pauses when the screen turns off to preserve battery.

4. **`VIBRATE` & `POST_NOTIFICATIONS`:**
   - *Reason:* Provides tactile haptic feedback upon quick entry save and delivers sync & export notifications.
