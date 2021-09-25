package io.github.wykopmobilny.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContextWrapper
import android.net.Uri
import android.provider.OpenableColumns
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.github.wykopmobilny.glide.GlideApp
import io.github.wykopmobilny.utils.api.parseDate
import io.github.wykopmobilny.utils.recyclerview.ViewHolderDependentItemDecorator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import org.ocpsoft.prettytime.PrettyTime
import org.threeten.bp.Duration
import org.threeten.bp.format.DateTimeParseException
import java.util.Date
import java.util.Locale

fun SpannableStringBuilder.appendNewSpan(text: CharSequence, what: Any, flags: Int): SpannableStringBuilder {
    val start = length
    append(text)
    setSpan(what, start, length, flags)
    return this
}

fun View.getActivityContext(): Activity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun RecyclerView.prepare() {
    setItemViewCacheSize(20)
    layoutManager = LinearLayoutManager(context)
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    addItemDecoration(ViewHolderDependentItemDecorator(context))
}

fun RecyclerView.prepareNoDivider() {
    setItemViewCacheSize(20)
    layoutManager = LinearLayoutManager(context)
}

fun ImageView.loadImage(url: String, signature: Int? = null) {
    if (signature == null) {
        GlideApp.with(context)
            .load(url)
            .into(this)
    } else {
        GlideApp.with(context)
            .load(url)
            .apply(
                RequestOptions()
                    .signature(ObjectKey(signature)),
            )
            .into(this)
    }
}

fun String.toPrettyDate(): String {
    return PrettyTime(Locale("pl")).format(parseDate(this))
}
fun Instant.toPrettyDate(): String {
    return PrettyTime(Locale("pl")).format(Date(toEpochMilliseconds()))
}

fun Instant.toDurationPrettyDate(): String {
    val period = this.periodUntil(Clock.System.now(), TimeZone.currentSystemDefault())

    if (period.years > 1 && period.months > 0) {
        return "${period.years} lata ${period.months} mies."
    } else if (period.years > 1 && period.months == 0) {
        return "${period.years} lata"
    } else if (period.years == 1 && period.months > 0) {
        return "${period.years} rok ${period.months} mies."
    } else if (period.years == 1 && period.months == 0) {
        return "${period.years} rok"
    } else if (period.years == 0 && period.months > 0) {
        return "${period.months} mies."
    } else if (period.months == 0 && period.days > 0) {
        return "${period.days} dni"
    } else {
        if (period.days == 0 && period.hours > 0) {
            return "${period.hours} godz."
        } else if (period.hours == 0) {
            return "${period.minutes} min."
        }
    }
    return PrettyTime(Locale("pl")).formatDurationUnrounded(Date(toEpochMilliseconds()))
}

fun Uri.queryFileName(contentResolver: ContentResolver): String {
    var result: String? = null
    if (scheme == "content") {
        result = contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }
    }
    if (result == null) {
        result = path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

/**
 * Parse YouTube timestamp from string to milliseconds value.
 *
 * Original value comes from 't' parameter from youtube url and is given in seconds. YouTube player needs this
 * value in milliseconds.
 *
 * Parameter 't' can be in following formats:
 * '3' we can assume that this values is in seconds
 * '3m', '5m30s', '1h30m30s' needs conversion using ISO 8601.
 *
 * @return time in milliseconds, null if value cannot be converted.
 *
 */
fun String.youtubeTimestampToMsOrNull(): Int? {
    val timestamp = this.toLongOrNull()
    if (timestamp != null) {
        return Duration.ofSeconds(timestamp).toMillis().toInt()
    }

    return try {
        Duration.parse("PT$this").toMillis().toInt()
    } catch (e: DateTimeParseException) {
        null
    }
}
