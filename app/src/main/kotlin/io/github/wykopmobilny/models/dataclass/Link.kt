package io.github.wykopmobilny.models.dataclass

import android.os.Parcel
import android.os.Parcelable
import io.github.wykopmobilny.utils.toPrettyDate
import kotlinx.datetime.Instant

class Link(
    val id: Long,
    val title: String,
    val description: String,
    val tags: String,
    val sourceUrl: String,
    var voteCount: Int,
    var buryCount: Int,
    var comments: MutableList<LinkComment>,
    val commentsCount: Int,
    val relatedCount: Int,
    val author: Author?,
    val fullDate: Instant,
    val fullImage: String?,
    val previewImage: String?,
    val plus18: Boolean,
    val canVote: Boolean,
    val isHot: Boolean,
    var userVote: String?,
    var userFavorite: Boolean,
    val app: String?,
    val violationUrl: String?,
    var gotSelected: Boolean,
    var isBlocked: Boolean = false,
) : Parcelable {

    val url = "https://www.wykop.pl/link/$id"

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(LinkComment.CREATOR)!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Author::class.java.classLoader),
        parcel.readLong().let(Instant::fromEpochMilliseconds),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(tags)
        parcel.writeString(sourceUrl)
        parcel.writeInt(voteCount)
        parcel.writeInt(buryCount)
        parcel.writeTypedList(comments)
        parcel.writeInt(commentsCount)
        parcel.writeInt(relatedCount)
        parcel.writeParcelable(author, flags)
        parcel.writeLong(fullDate.toEpochMilliseconds())
        parcel.writeString(fullImage)
        parcel.writeString(previewImage)
        parcel.writeByte(if (plus18) 1 else 0)
        parcel.writeByte(if (canVote) 1 else 0)
        parcel.writeByte(if (isHot) 1 else 0)
        parcel.writeString(userVote)
        parcel.writeByte(if (userFavorite) 1 else 0)
        parcel.writeString(app)
        parcel.writeByte(if (gotSelected) 1 else 0)
        parcel.writeByte(if (isBlocked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    val date: String
        get() = this.fullDate.toPrettyDate()

    companion object CREATOR : Parcelable.Creator<Link> {
        override fun createFromParcel(parcel: Parcel): Link {
            return Link(parcel)
        }

        override fun newArray(size: Int): Array<Link?> {
            return arrayOfNulls(size)
        }
    }
}
