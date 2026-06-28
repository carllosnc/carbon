package com.carbon.launcher.data

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.storage.StorageManager
import android.app.usage.StorageStatsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppModel(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystem: Boolean,
    val versionName: String,
    val sizeMb: String,
    val cacheMb: String,
    val dataMb: String,
    val installDate: String,
    val category: AppCategory,
)

class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    private val packageRules: List<Pair<List<String>, AppCategory>> = listOf(
        listOf("whatsapp", "telegram", "messenger", "discord", "slack", "signal", "teams",
            "line.android", "threema", "kik", "viber", "bbm", "snapchat") to AppCategory.CHATS,
        listOf("instagram", "facebook", "tiktok", "twitter", "x.android", "threads",
            "pinterest", "linkedin", "reddit", "tumblr", "mastodon", "beautyplus",
            "picsart", "snapseed", "vsco", "lightroom") to AppCategory.SOCIAL,
        listOf("nu.production", "nubank", "itau", "bradesco", "santander", "caixa",
            "intermedium", "picpay", "paypal", "mercadopago", "mercadopago.wallet",
            "sicoob", "sicredi", "banco", "c6bank", "sodexomobile", "serasaexperian",
            "alelo", "vr.com", "ticket", "wise", "revolut", "n26", "chime", "venmo",
            "cashapp", "bankofamerica", "wellsfargo", "chase", "capitalone") to AppCategory.FINANCE,
        listOf("amazon.mshop", "mercadolibre", "carrefour", "centauro", "kabum",
            "shopee", "aliexpress", "magazineluiza", "alibaba", "ebay", "etsy",
            "wish", "walmart", "target", "bestbuy", "ikea", "luizalabs",
            "bomnegocio", "olx", "enjoei") to AppCategory.SHOPPING,
        listOf("ifood", "ubereats", "rappi", "foodpanda", "doordash", "grubhub",
            "deliveroo", "seamless", "postmates", "instacart", "pedidosja",
            "anota.ai", "cashin") to AppCategory.FOOD,
        listOf("ubercab", "uber", "lyft", "99", "bolt", "cabify", "didiglobal",
            "moovit", "citymapper", "waze", "transit", "ospasses") to AppCategory.TRAVEL,
        listOf("google.android.apps.docs", "google.android.keep", "google.android.apps.translate",
            "microsoft.office", "microsoft.teams", "adobe.reader", "notion",
            "evernote", "todoist", "trello", "asana", "dropbox", "googledocs",
            "docs.editors", "readera", "wps", "polaris", "office") to AppCategory.PRODUCTIVITY,
        listOf("spotify", "deezer", "youtube", "netflix", "primevideo", "disney",
            "hbo", "twitch", "tidal", "applemusic", "soundcloud", "hulu",
            "crunchyroll", "vimeo", "dazn", "plex", "kodi", "stremio",
            "globoplay", "starplus", "paramount", "max", "prime video") to AppCategory.ENTERTAINMENT,
        listOf("google.android.apps.magazines", "flipboard", "feedly", "inoreader",
            "bbc", "cnn", "nytimes", "guardian", "reddit.news", "g1", "uol",
            "globo") to AppCategory.NEWS,
        listOf("google.android.apps.fitness", "strava", "nike", "adidas",
            "myfitnesspal", "fitbit", "garmin", "headspace", "calm",
            "samsung.health", "whoop", "shealth", "doctordroid") to AppCategory.HEALTH,
        listOf("openai.chatgpt", "anthropic.claude", "deepseek.chat", "x.grok",
            "perplexity", "gemini", "bing", "copilot", "character.ai",
            "poe", "you.com", "mistral", "lechat") to AppCategory.AI,
        listOf("authenticator", "azure.authenticator", "google.android.apps.auth",
            "password", "1password", "lastpass", "bitwarden", "keepass",
            "vpn", "nordvpn", "expressvpn", "surfshark", "protonvpn",
            "safetycore", "vs_sim", "readera") to AppCategory.TOOLS,
    )

    private val gameKeywords = listOf(
        "game", "games", "clash", "candy", "minecraft", "pubg", "freefire",
        "codmobile", "honorofkings", "wildrift", "genshin", "honkai",
        "supercell", "riot", "amongus", "roblox", "fortnite", "playrix",
    )

    private fun isGameWord(pkg: String, label: String): Boolean {
        val wordRegex = Regex("\\b(game|games|clash|candy|minecraft|pubg|freefire|codmobile|honorofkings|wildrift|genshin|honkai|supercell|riot|amongus|roblox|fortnite|playrix)\\b", RegexOption.IGNORE_CASE)
        return wordRegex.containsMatchIn(pkg) || wordRegex.containsMatchIn(label)
    }

    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val ownPackage = context.packageName

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(launcherIntent, PackageManager.GET_META_DATA)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != ownPackage }
            .filter { it.enabled }
            .map { it.toModel() }
            .sortedWith(compareBy({ it.category.order }, { it.label.lowercase() }))
    }

    private fun ApplicationInfo.toModel(): AppModel {
        val label = pm.getApplicationLabel(this).toString()
        val icon = pm.getApplicationIcon(this)
        val versionName = try {
            pm.getPackageInfo(packageName, 0).versionName ?: "—"
        } catch (e: PackageManager.NameNotFoundException) {
            "—"
        }
        val sizeBytes = try {
            sourceDir?.let { java.io.File(it).length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
        val sizeMb = if (sizeBytes > 0) {
            String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        } else "—"
        val (cacheMb, dataMb) = getStorageStats(uid)
        val installDate = try {
            val t = pm.getPackageInfo(packageName, 0).firstInstallTime
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(t))
        } catch (e: Exception) {
            "—"
        }
        return AppModel(
            packageName = packageName,
            label = label,
            icon = icon,
            isSystem = (flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            versionName = versionName,
            sizeMb = sizeMb,
            cacheMb = cacheMb,
            dataMb = dataMb,
            installDate = installDate,
            category = categorize(label),
        )
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        ) == AppOpsManager.MODE_ALLOWED
    }

    private fun getStorageStats(uid: Int): Pair<String, String> {
        if (!hasUsageAccess()) return "—" to "—"
        return try {
            val storageStatsManager =
                context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val storageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolume = storageManager.primaryStorageVolume
            val uuid = storageVolume.uuid?.let { java.util.UUID.fromString(it) }
                ?: StorageManager.UUID_DEFAULT
            val stats = storageStatsManager.queryStatsForUid(uuid, uid)
            val cache = stats.cacheBytes
            val data = stats.dataBytes
            val cacheMb = if (cache > 0) String.format("%.1f MB", cache / (1024.0 * 1024.0)) else "0.0 MB"
            val dataMb = if (data > 0) String.format("%.1f MB", data / (1024.0 * 1024.0)) else "0.0 MB"
            cacheMb to dataMb
        } catch (e: Exception) {
            "—" to "—"
        }
    }

    private fun ApplicationInfo.categorize(label: String): AppCategory {
        val pkg = packageName.lowercase()
        val lbl = label.lowercase()

        if ((flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            return AppCategory.DEBUG
        }

        if (category == ApplicationInfo.CATEGORY_GAME || isGameWord(pkg, lbl)) {
            return AppCategory.GAMES
        }

        for ((patterns, cat) in packageRules) {
            if (patterns.any { pkg.contains(it, ignoreCase = true) }) return cat
        }

        return when (category) {
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE,
            ApplicationInfo.CATEGORY_NEWS -> AppCategory.ENTERTAINMENT
            ApplicationInfo.CATEGORY_MAPS -> AppCategory.TRAVEL
            ApplicationInfo.CATEGORY_ACCESSIBILITY -> AppCategory.TOOLS
            else -> AppCategory.OTHER
        }
    }
}
