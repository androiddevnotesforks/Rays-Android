package com.skyd.rays.util.share.app

import android.content.Context
import android.net.Uri
import com.skyd.rays.util.share.ShareUtil

class TelegramAppInfo : IAppInfo {
    override val packageName: String
        get() = "org.telegram.messenger"

    override fun share(
        context: Context,
        topActivityFullName: String,
        uris: List<Uri>,
        mimetype: String?,
    ): Boolean {
        if (!topActivityFullName.startsWith("org.telegram")) return false
        ShareUtil.startShare(
            context = context,
            uris = uris,
            mimetype = mimetype,
            packageName = packageName,
            className = "org.telegram.ui.LaunchActivity"
        )
        return true
    }
}