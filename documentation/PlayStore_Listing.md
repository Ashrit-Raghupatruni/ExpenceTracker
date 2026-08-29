# Google Play Store Listing Specification & Metadata

## 1. Store Metadata

- **App Name:** ShakeExpense: Smart Budget & Family Sync
- **Short Description (Max 80 chars):** Shake to log expenses instantly. Smart UPI detection, spreadsheet & family sync.
- **Category:** Finance / Personal Finance & Budgeting
- **Content Rating:** Everyone / 3+

---

## 2. Full Description (Google Play Formatted)

**ShakeExpense** revolutionizes daily expense tracking by making budgeting fast, effortless, and family-friendly. Never forget to log a purchase again—simply **shake your phone** from any screen to bring up an instant, frosted floating entry pad!

---

### ⚡ WHY YOU'LL LOVE SHAKEXPENSE

• **⚡ Instant Shake-to-Log:** Shake your phone anywhere to open a transparent floating keypad. Pick a category, tap the amount, and save in under 2 seconds.
• **📊 High-Density Spreadsheet View:** Clean, professional spreadsheet grid with live search, filters, category color coding, and instant debit/credit balance totals.
• **👨‍👩‍👧‍👦 Real-Time Family & Parental Sync:** Create a family group with a 6-digit invite code or QR scan. Monitor child pocket money spending, aggregate family expenses, and manage budgets in real time.
• **💳 Smart On-Device UPI & Bank Detection:** Automatically detects debit/credit SMS notifications from major Indian banks (HDFC, SBI, ICICI, Axis, GPay, PhonePe, Paytm) and prompts one-tap confirmation—100% on-device and private.
• **📅 Monthly History & Category Insights:** Visual category breakdown with spending percentages and expandable multi-month transaction histories.
• **🎨 Modern Design & Theming:** Custom dark, light, and system-adaptive themes built with modern Jetpack Compose.
• **🔒 100% Offline-First & Private:** Works completely offline without internet. All data is securely stored in local Room SQLite, with optional encrypted Google Cloud sync.

---

### 🛡️ PRIVACY & SECURITY FIRST
• No data selling or third-party ads.
• On-device financial notification parsing (your SMS never leaves your device).
• Encrypted cloud backup and multi-device synchronization powered by Google Firebase.

---

## 3. Graphic Assets & Specs

| Asset | Dimensions | Format | Status |
|---|---|---|---|
| **App Icon** | 512 x 512 px | PNG (32-bit, alpha) | Ready (`ic_launcher` / `ic_launcher_round`) |
| **Feature Graphic** | 1024 x 500 px | JPG or 24-bit PNG | Specification Ready |
| **Phone Screenshots** | Min 1080 x 1920 px (16:9 / 20:9) | PNG | 5+ Verified Device Screenshots (`screenshot_tracker_release2.png`, `screenshot_family_release.png`, `screenshot_profile_ready.png`, etc.) |

---

## 4. Play Console Closed Testing Guide

1. **Upload Artifact:**
   - Upload the production-signed AAB: `app/build/outputs/bundle/release/app-release.aab` to **Closed testing (Alpha / Beta)** track.
2. **Release Notes (en-US):**
   ```text
   • Initial release of ShakeExpense!
   • Shake-to-log floating HUD for rapid 2-second expense entry.
   • High-density spreadsheet and category breakdown dashboard.
   • Family Hub with real-time multi-device cloud synchronization.
   • Privacy-first on-device bank notification parser.
   • Monthly historical spending summaries & dark/light themes.
   ```
3. **Tester List:**
   - Add tester email addresses to the Closed Testing Track.
   - Share opt-in URL with test group.
