package io.github.wykopmobilny.models.dataclass

import kotlinx.datetime.Instant

open class Notification(
    val id: Long,
    val author: Author?,
    val body: String,
    val date: Instant?,
    val type: String,
    val url: String?,
    var new: Boolean,
) {

    var visible = true
    val tag by lazy { this.body.substringAfter("#").substringBefore(" ") }

    override fun equals(other: Any?): Boolean {
        return if (other !is Notification) false
        else (other.id == id)
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}
