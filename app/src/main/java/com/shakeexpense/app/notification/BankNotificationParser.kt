package com.shakeexpense.app.notification

import com.shakeexpense.app.domain.model.TransactionType
import java.util.regex.Pattern

class BankNotificationParser {

    companion object {
        // Amount regex pattern: captures ₹123, Rs. 123.50, INR 5,000, etc.
        private val AMOUNT_PATTERN = Pattern.compile(
            """(?i)(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?)|([\d,]+(?:\.\d{1,2})?)\s*(?:rs\.?|inr|₹)"""
        )

        // Debit indicator pattern (debited, paid, sent to X, spent, etc.)
        private val DEBIT_PATTERN = Pattern.compile(
            """(?i)\b(debited|spent|paid|withdrawn|transferred to|payment of|purchase of)\b"""
        )

        // Credit indicator pattern (credited, received, deposited, cashback, refund, sent to you)
        private val CREDIT_PATTERN = Pattern.compile(
            """(?i)\b(credited|received|deposited|added|refund|cashback|to you)\b"""
        )

        // Payee/Merchant pattern
        private val MERCHANT_PATTERN = Pattern.compile(
            """(?i)(?:to|at|vpa|info|for|from)\s+([A-Za-z0-9\.\-_@\s]{2,30}?)(?:\s+on|\s+ref|\s+upi|\.|\,|$|\s+bal|\s+via|\s+using|\s+avl|\s+acc)"""
        )

        // Generic app title names to ignore as payees
        private val GENERIC_TITLES = setOf(
            "phonepe", "gpay", "google pay", "paytm", "bhim", "cred",
            "bank alert", "transaction alert", "upi alert", "sms", "messages"
        )

        // Known UPI & Digital Payment Apps
        val SUPPORTED_PAYMENT_PACKAGES = setOf(
            "com.phonepe.app",
            "com.google.android.apps.nbu.paisa.user",
            "net.one97.paytm",
            "in.org.npci.upiapp", // BHIM
            "com.dreamplug.androidapp", // CRED
            "com.amazon.mpay.app",
            "com.naviapp",
            "com.supermoney.app",
            "com.mobikwik_new",
            "com.freecharge.android",
            "com.sliceit",
            "money.jupiter",
            "money.fi"
        )

        // Known Banking Apps
        val SUPPORTED_BANKING_PACKAGES = setOf(
            "com.snapwork.hdfc",
            "com.hdfcbank.payzapp",
            "com.csam.icici.bank.imobile",
            "com.icicibank.pockets",
            "com.sbi.lotusintouch",
            "com.sbi.upi",
            "com.sbi.SBIFreedomPlus",
            "com.axis.mobile",
            "com.msf.kbank.mobile",
            "com.kotak.cherry",
            "com.pnb.one",
            "com.bankofbaroda.mconnect",
            "com.canarabank.ai1mobile",
            "com.canarabank.mobility",
            "com.infrasofttech.unionfirst",
            "com.idfcfirstbank.optimus",
            "com.indusind.mobile",
            "com.yesbank",
            "com.federalbank.fedmobile",
            "com.rblbank.mobank",
            "com.aubank.aubankapp",
            "com.sc.expresspay",
            "hk.com.hsbc.hsbchkmobile",
            "com.dbs.in.digitalbank"
        )

        // System / OEM SMS Apps
        val SMS_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.oneplus.mms",
            "com.heytap.mms",
            "com.coloros.mms",
            "com.oppo.mms",
            "com.android.mms",
            "com.miui.smsextra",
            "com.vivo.mms"
        )

        // Bank / Financial SMS Sender Identifiers (e.g. VK-HDFCBK, AXISBK, SBIINB, PAYTM, etc.)
        val BANK_SMS_SENDER_REGEX = Pattern.compile(
            """(?i)\b([A-Z]{2}-)?(HDFC|ICICI|SBI|AXIS|KOTAK|PNB|CANARA|BOB|IDFC|YES|INDUS|UNION|FED|RBL|AUBANK|SCB|HSBC|DBS|PAYTM|PHONEPE|BHIM|CRED|GPAY|AMAZONPAY|BARODA|CENTRAL|SYND|VIJAYA|IOB|UCO|ALLAHABAD|BANDHAN|EQUITAS|UJJIVAN|JUPITER|SLICE|FI|MONEY|BANK|ALERTS|TRANS|UPI)[A-Z0-9]*\b"""
        )

        // Financial transaction intent keywords required for SMS parsing
        val FINANCIAL_TRANSACTION_KEYWORDS = listOf(
            "debited", "credited", "spent", "paid", "withdrawn", "received", "transferred",
            "a/c", "acct", "account", "vpa", "upi", "card", "inr", "rs.", "rs ", "bal", "avl bal"
        )

        // Category Heuristics
        private val FOOD_KEYWORDS = listOf("swiggy", "zomato", "mcdonald", "starbucks", "kfc", "burger", "domino", "pizza", "dine", "cafe", "restaurant", "subway", "chai", "bakery")
        private val TRANSPORT_KEYWORDS = listOf("uber", "ola", "rapido", "metro", "irctc", "rail", "petrol", "fuel", "shell", "hpcl", "bpcl", "ioc", "parking", "fastag", "toll", "cab")
        private val GROCERIES_KEYWORDS = listOf("blinkit", "zepto", "instamart", "bigbasket", "supermarket", "dmart", "grocery", "nature basket", "spencer", "more retail", "fruits", "vegetables")
        private val BILLS_KEYWORDS = listOf("bescom", "airtel", "jio", "vi", "electricity", "water", "gas", "broadband", "bill", "tatasky", "recharge", "dth", "wifi", "insurance", "rent", "maintenance")
        private val SHOPPING_KEYWORDS = listOf("amazon", "flipkart", "myntra", "zara", "h&m", "ajio", "nykaa", "retail", "meesho", "croma", "reliance", "lifestyle", "westside", "shopping")
        private val ENTERTAINMENT_KEYWORDS = listOf("pvr", "inox", "bookmyshow", "netflix", "spotify", "prime", "hotstar", "steam", "cinema", "movie", "playstation", "gaming", "disney", "youtube")
    }

    /**
     * Strictly verifies whether the notification is from a recognized Financial App or Bank SMS sender.
     * Rejects social media, shopping apps, messaging apps, and generic notifications.
     */
    fun isSupportedFinancialNotification(packageName: String, title: String, text: String): Boolean {
        val cleanPkg = packageName.trim().lowercase()

        // 1. Direct match with verified Payment / UPI Apps
        if (SUPPORTED_PAYMENT_PACKAGES.contains(cleanPkg)) return true

        // 2. Direct match with verified Banking Apps
        if (SUPPORTED_BANKING_PACKAGES.contains(cleanPkg)) return true

        // 3. SMS Apps: only if sender/title is a verified Bank/UPI sender code AND text contains transaction indicators
        if (SMS_PACKAGES.contains(cleanPkg) || cleanPkg.contains("mms") || cleanPkg.contains("messaging")) {
            val titleMatchesBank = BANK_SMS_SENDER_REGEX.matcher(title).find()
            val textMatchesBankHeader = BANK_SMS_SENDER_REGEX.matcher(text.take(30)).find()
            val hasFinancialIntent = FINANCIAL_TRANSACTION_KEYWORDS.count { text.lowercase().contains(it) } >= 2

            if ((titleMatchesBank || textMatchesBankHeader) && hasFinancialIntent) {
                return true
            }
        }

        return false
    }

    fun parse(
        text: String,
        title: String = "",
        packageName: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): ParsedBankTransaction? {
        // Strict Source Validation: Ignore unrelated apps (WhatsApp, shopping apps, games, etc.)
        if (packageName.isNotBlank() && !isSupportedFinancialNotification(packageName, title, text)) {
            return null
        }

        val fullContent = if (title.isNotBlank()) "$title. $text" else text
        if (fullContent.isBlank()) return null

        // 1. Extract Amount
        val amountMatcher = AMOUNT_PATTERN.matcher(fullContent)
        if (!amountMatcher.find()) return null

        val amountStr = (amountMatcher.group(1) ?: amountMatcher.group(2))
            ?.replace(",", "")
            ?.trim() ?: return null

        val amountCents = try {
            val amountDouble = amountStr.toDouble()
            if (amountDouble <= 0.0) return null
            Math.round(amountDouble * 100.0)
        } catch (e: Exception) {
            return null
        }

        // 2. Extract Transaction Type
        val lowerContent = fullContent.lowercase()
        val isSentToYou = lowerContent.contains("to you") || lowerContent.contains("received")
        val hasDebit = DEBIT_PATTERN.matcher(fullContent).find() || (lowerContent.contains("sent") && !isSentToYou)
        val hasCredit = CREDIT_PATTERN.matcher(fullContent).find() || isSentToYou

        val type = when {
            isSentToYou -> TransactionType.CREDIT
            hasCredit && !hasDebit -> TransactionType.CREDIT
            hasDebit -> TransactionType.DEBIT
            else -> TransactionType.DEBIT
        }

        // 3. Extract Merchant / Payee
        var merchant: String? = null
        val merchantMatcher = MERCHANT_PATTERN.matcher(text)
        if (merchantMatcher.find()) {
            val candidate = merchantMatcher.group(1)?.trim()
            if (!candidate.isNullOrBlank() && candidate.length in 2..30 && !candidate.equals("you", ignoreCase = true)) {
                merchant = candidate
            }
        }

        // If body didn't yield a valid merchant and title is a person/merchant name, use title
        if (merchant.isNullOrBlank() && title.isNotBlank()) {
            val cleanTitle = title.trim()
            if (!GENERIC_TITLES.contains(cleanTitle.lowercase()) && cleanTitle.length in 2..35) {
                merchant = cleanTitle
            }
        }

        // 4. Infer Category ID
        val categoryId = inferCategoryId(fullContent, merchant)

        return ParsedBankTransaction(
            amountCents = amountCents,
            type = type,
            merchantOrPayee = merchant,
            suggestedCategoryId = categoryId,
            timestamp = timestamp,
            rawPackageName = packageName
        )
    }

    fun inferCategoryId(text: String, merchant: String?): Long {
        val searchCorpus = "${text.lowercase()} ${merchant?.lowercase() ?: ""}"

        return when {
            FOOD_KEYWORDS.any { searchCorpus.contains(it) } -> 1L // Food
            TRANSPORT_KEYWORDS.any { searchCorpus.contains(it) } -> 2L // Transport
            GROCERIES_KEYWORDS.any { searchCorpus.contains(it) } -> 3L // Groceries
            BILLS_KEYWORDS.any { searchCorpus.contains(it) } -> 4L // Bills
            SHOPPING_KEYWORDS.any { searchCorpus.contains(it) } -> 5L // Shopping
            ENTERTAINMENT_KEYWORDS.any { searchCorpus.contains(it) } -> 6L // Entertainment
            else -> 7L // Others
        }
    }
}
