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
            """(?i)\b(debited|spent|paid|withdrawn|transferred to|payment of|purchase of|sent|autopay debited|vpa debit)\b"""
        )

        // Credit indicator pattern (credited, received, deposited, cashback, refund, sent to you)
        private val CREDIT_PATTERN = Pattern.compile(
            """(?i)\b(credited|received|deposited|added|refund|cashback|to you|salary credited)\b"""
        )

        // Payee/Merchant pattern
        private val MERCHANT_PATTERN = Pattern.compile(
            """(?i)(?:sent to|paid to|transferred to|payment to|to|at|vpa|info|for|from)\s+([A-Za-z0-9\.\-_@\s]{2,35}?)(?:\s+on|\s+ref|\s+upi|\.|\,|$|\s+bal|\s+via|\s+using|\s+avl|\s+acc)"""
        )

        // Account Number / Card Number pattern (e.g. A/c ending XX1234, Card XX5678, acct *9876)
        private val ACCOUNT_NUMBER_PATTERN = Pattern.compile(
            """(?i)(?:a/c|acct|account|card|ac|ending with|ending in|no\.)\s*(?:no\.?\s*)?(?:[x*XN]+)?(\d{3,6})\b|(?:xx|[*]{2,})(\d{3,6})\b"""
        )

        // UPI Reference Number / UTR / RRN pattern
        private val UPI_REF_PATTERN = Pattern.compile(
            """(?i)(?:upi ref|ref no|rrn|utr|txn id|txn no|reference no|upi rrn)\s*[:.]?\s*([0-9]{9,16})\b"""
        )

        // UPI VPA pattern (e.g. user@okhdfcbank, merchant@paytm)
        private val UPI_VPA_PATTERN = Pattern.compile(
            """\b([a-zA-Z0-9.\-_]{2,35}@[a-zA-Z0-9]{2,15})\b"""
        )

        // Non-financial message rejection patterns (OTP, promotional, loan offers)
        private val OTP_PATTERN = Pattern.compile(
            """(?i)\b(otp|one time password|verification code|secret code|auth code|do not share|valid for \d+ min)\b"""
        )

        private val PROMO_PATTERN = Pattern.compile(
            """(?i)\b(pre-approved|apply for loan|congratulations|reward points|special offer|flat \d+% off|loan offer|credit limit increase)\b"""
        )

        // Generic app title names to ignore as payees
        private val GENERIC_TITLES = setOf(
            "phonepe", "gpay", "google pay", "paytm", "bhim", "cred",
            "bank alert", "transaction alert", "upi alert", "sms", "messages", "alert", "notification"
        )

        // Known Bank Names and their detection signatures
        private val BANK_SIGNATURE_MAP = mapOf(
            "HDFC" to "HDFC Bank",
            "ICICI" to "ICICI Bank",
            "SBI" to "State Bank of India",
            "STATE BANK" to "State Bank of India",
            "AXIS" to "Axis Bank",
            "KOTAK" to "Kotak Mahindra Bank",
            "PNB" to "Punjab National Bank",
            "PUNJAB" to "Punjab National Bank",
            "CANARA" to "Canara Bank",
            "BOB" to "Bank of Baroda",
            "BARODA" to "Bank of Baroda",
            "IDFC" to "IDFC FIRST Bank",
            "YES" to "Yes Bank",
            "INDUS" to "IndusInd Bank",
            "UNION" to "Union Bank of India",
            "FED" to "Federal Bank",
            "FEDERAL" to "Federal Bank",
            "RBL" to "RBL Bank",
            "AUBANK" to "AU Small Finance Bank",
            "SCB" to "Standard Chartered",
            "HSBC" to "HSBC Bank",
            "DBS" to "DBS Bank",
            "BANDHAN" to "Bandhan Bank",
            "EQUITAS" to "Equitas Small Finance Bank",
            "UJJIVAN" to "Ujjivan Small Finance Bank",
            "JUPITER" to "Jupiter Money",
            "FI" to "Fi Money",
            "SLICE" to "Slice",
            "PAYTM" to "Paytm Payments Bank",
            "AIRTEL" to "Airtel Payments Bank",
            "PHONEPE" to "PhonePe",
            "GPAY" to "Google Pay",
            "BHIM" to "BHIM UPI",
            "CRED" to "CRED"
        )

        // Known UPI & Digital Payment Apps
        val SUPPORTED_PAYMENT_PACKAGES = setOf(
            "com.phonepe.app",
            "com.phonepe.app.business",
            "com.google.android.apps.nbu.paisa.user",
            "net.one97.paytm",
            "net.one97.paytm.business",
            "in.org.npci.upiapp", // BHIM
            "com.dreamplug.androidapp", // CRED
            "com.amazon.mpay.app",
            "com.naviapp",
            "com.supermoney.app",
            "com.mobikwik_new",
            "com.freecharge.android",
            "com.sliceit",
            "money.jupiter",
            "money.fi",
            "com.fampay.in",
            "com.myairtelapp",
            "com.jio.myjio",
            "com.tatadigital.tcp",
            "com.popclub.app",
            "org.altruist.BajajPay",
            "com.olive.upi"
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

        // Bank / Financial SMS Sender Identifiers (e.g. VK-HDFCBK, ADHDFCBK, AXISBK, SBIINB, PAYTM, etc.)
        val BANK_SMS_SENDER_REGEX = Pattern.compile(
            """(?i)\b([A-Z]{2}-?)?(HDFC|ICICI|SBI|AXIS|KOTAK|PNB|CANARA|BOB|IDFC|YES|INDUS|UNION|FED|RBL|AUBANK|SCB|HSBC|DBS|PAYTM|PHONEPE|BHIM|CRED|GPAY|AMAZONPAY|BARODA|CENTRAL|SYND|VIJAYA|IOB|UCO|ALLAHABAD|BANDHAN|EQUITAS|UJJIVAN|JUPITER|SLICE|FI|MONEY|BANK|ALERTS|TRANS|UPI)[A-Z0-9]*\b"""
        )

        // Financial transaction intent keywords required for SMS parsing
        val FINANCIAL_TRANSACTION_KEYWORDS = listOf(
            "debited", "credited", "spent", "paid", "withdrawn", "received", "transferred", "sent",
            "a/c", "acct", "account", "vpa", "upi", "card", "inr", "rs.", "rs ", "bal", "avl bal"
        )

        // Category Heuristics
        private val FOOD_KEYWORDS = listOf("swiggy", "zomato", "mcdonald", "starbucks", "kfc", "burger", "domino", "pizza", "dine", "cafe", "restaurant", "subway", "chai", "bakery", "food", "eat")
        private val TRANSPORT_KEYWORDS = listOf("uber", "ola", "rapido", "metro", "irctc", "rail", "petrol", "fuel", "shell", "hpcl", "bpcl", "ioc", "parking", "fastag", "toll", "cab")
        private val GROCERIES_KEYWORDS = listOf("blinkit", "zepto", "instamart", "bigbasket", "supermarket", "dmart", "grocery", "nature basket", "spencer", "more retail", "fruits", "vegetables")
        private val BILLS_KEYWORDS = listOf("bescom", "airtel", "jio", "vi", "electricity", "water", "gas", "broadband", "bill", "tatasky", "recharge", "dth", "wifi", "insurance", "rent", "maintenance", "tneb")
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
        if (SUPPORTED_PAYMENT_PACKAGES.any { cleanPkg.contains(it) || it.contains(cleanPkg) }) return true

        // 2. Direct match with verified Banking Apps
        if (SUPPORTED_BANKING_PACKAGES.any { cleanPkg.contains(it) || it.contains(cleanPkg) }) return true

        // 3. SMS Apps: if package is SMS or text contains strong financial signals
        val isSmsApp = SMS_PACKAGES.contains(cleanPkg) || cleanPkg.contains("mms") || cleanPkg.contains("messaging") || cleanPkg.contains("message") || cleanPkg.contains("sms")
        val titleMatchesBank = BANK_SMS_SENDER_REGEX.matcher(title).find()
        val textMatchesBankHeader = BANK_SMS_SENDER_REGEX.matcher(text.take(35)).find()
        val hasFinancialIntent = FINANCIAL_TRANSACTION_KEYWORDS.count { text.lowercase().contains(it) } >= 2
        val hasAmount = AMOUNT_PATTERN.matcher(text).find() || AMOUNT_PATTERN.matcher(title).find()

        if (isSmsApp && (titleMatchesBank || textMatchesBankHeader || (hasFinancialIntent && hasAmount))) {
            return true
        }

        // 4. Fallback for any OEM-customized or cloned UPI app with clear monetary transaction signal
        val combined = "$title $text".lowercase()
        val hasDebitOrCredit = DEBIT_PATTERN.matcher(combined).find() || CREDIT_PATTERN.matcher(combined).find() || combined.contains("sent to") || combined.contains("received from")
        val hasAccountOrUpi = ACCOUNT_NUMBER_PATTERN.matcher(combined).find() || UPI_VPA_PATTERN.matcher(combined).find() || UPI_REF_PATTERN.matcher(combined).find() || combined.contains("upi") || combined.contains("vpa")

        if (hasAmount && hasDebitOrCredit && (hasAccountOrUpi || titleMatchesBank)) {
            return true
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

        // Filter out OTP and promotional messages immediately
        if (OTP_PATTERN.matcher(fullContent).find()) return null
        if (PROMO_PATTERN.matcher(fullContent).find() && !DEBIT_PATTERN.matcher(fullContent).find()) return null

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
        val isSentToYou = lowerContent.contains("to you") || lowerContent.contains("received") || lowerContent.contains("credited")
        val hasDebit = DEBIT_PATTERN.matcher(fullContent).find() || (lowerContent.contains("sent") && !isSentToYou)
        val hasCredit = CREDIT_PATTERN.matcher(fullContent).find() || isSentToYou

        val type = when {
            isSentToYou -> TransactionType.CREDIT
            hasCredit && !hasDebit -> TransactionType.CREDIT
            hasDebit -> TransactionType.DEBIT
            else -> TransactionType.DEBIT
        }

        // 3. Extract Bank Name
        var bankName: String? = null
        for ((signature, name) in BANK_SIGNATURE_MAP) {
            if (title.contains(signature, ignoreCase = true) || text.contains(signature, ignoreCase = true) || packageName.contains(signature.lowercase())) {
                bankName = name
                break
            }
        }
        if (bankName == null) {
            val pkg = packageName.lowercase()
            when {
                pkg.contains("phonepe") -> bankName = "PhonePe"
                pkg.contains("paisa") || pkg.contains("gpay") -> bankName = "Google Pay"
                pkg.contains("paytm") -> bankName = "Paytm"
                pkg.contains("cred") -> bankName = "CRED"
                pkg.contains("amazon") -> bankName = "Amazon Pay"
                pkg.contains("bhim") || pkg.contains("npci") -> bankName = "BHIM UPI"
                pkg.contains("jupiter") -> bankName = "Jupiter Money"
                pkg.contains("fi") -> bankName = "Fi Money"
            }
        }

        // 4. Extract Account / Card Last Digits
        var accountLastDigits: String? = null
        val accountMatcher = ACCOUNT_NUMBER_PATTERN.matcher(fullContent)
        if (accountMatcher.find()) {
            accountLastDigits = accountMatcher.group(1) ?: accountMatcher.group(2)
        }

        // 5. Extract UPI Reference Number (UTR / RRN)
        var upiRef: String? = null
        val upiRefMatcher = UPI_REF_PATTERN.matcher(fullContent)
        if (upiRefMatcher.find()) {
            upiRef = upiRefMatcher.group(1)?.trim()
        }

        // 6. Extract UPI VPA
        var upiVpa: String? = null
        val upiVpaMatcher = UPI_VPA_PATTERN.matcher(fullContent)
        if (upiVpaMatcher.find()) {
            upiVpa = upiVpaMatcher.group(1)?.trim()
        }

        // 7. Extract Merchant / Payee
        var merchant: String? = null
        val merchantMatcher = MERCHANT_PATTERN.matcher(text)
        if (merchantMatcher.find()) {
            val candidate = merchantMatcher.group(1)?.trim()
            if (!candidate.isNullOrBlank() && candidate.length in 2..35 && !candidate.equals("you", ignoreCase = true)) {
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

        // If merchant is still null but we found a UPI VPA, use the UPI VPA as the merchant identifier
        if (merchant.isNullOrBlank() && !upiVpa.isNullOrBlank()) {
            merchant = upiVpa
        }

        // 8. Infer Category ID
        val categoryId = inferCategoryId(fullContent, merchant)

        // 9. Reliability Verification: Must have positive amount, debit/credit, and authentic source/account/bank identifiers
        val hasReliableIdentifiers = bankName != null || accountLastDigits != null || upiVpa != null || upiRef != null ||
                SUPPORTED_PAYMENT_PACKAGES.any { packageName.trim().lowercase().contains(it) || it.contains(packageName.trim().lowercase()) } ||
                SUPPORTED_BANKING_PACKAGES.any { packageName.trim().lowercase().contains(it) || it.contains(packageName.trim().lowercase()) }

        val isReliable = amountCents > 0L && hasReliableIdentifiers

        return ParsedBankTransaction(
            amountCents = amountCents,
            type = type,
            merchantOrPayee = merchant,
            suggestedCategoryId = categoryId,
            timestamp = timestamp,
            rawPackageName = packageName,
            bankName = bankName,
            accountLastDigits = accountLastDigits,
            upiRefNumber = upiRef,
            upiVpa = upiVpa,
            isReliableFinancialTransaction = isReliable
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

