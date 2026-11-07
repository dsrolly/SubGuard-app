package com.davinder.subguard.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Website links used inside the app. Change BASE only if your website address changes. */
object AppLinks {
    const val BASE = "https://subguard-265d6.web.app"   // no trailing slash

    const val HOME = BASE
    const val DOWNLOAD = "$BASE/index.html#install"
    const val HELP = "$BASE/help.html"
    const val WHATS_NEW = "$BASE/whats-new.html"
    const val BLOG = "$BASE/blog/index.html"
    const val PRIVACY = "$BASE/privacy.html"
    const val TERMS = "$BASE/terms.html"
    const val LICENSES = "$BASE/licenses.html"
    const val DELETE_ACCOUNT = "$BASE/delete-account.html"

    // Direct help articles (optional shortcuts from other screens)
    const val HELP_REMINDERS = "$BASE/help/renewal-reminders.html"
    const val HELP_BACKUP = "$BASE/help/backup-restore-export.html"
    const val HELP_CANCEL = "$BASE/help/cancel-a-subscription.html"
    const val HELP_PASSWORD = "$BASE/help/change-password.html"

    fun open(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
