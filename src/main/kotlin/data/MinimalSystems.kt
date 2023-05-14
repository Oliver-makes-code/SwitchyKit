package de.olivermakesco.switchykit.data

import dev.proxyfox.pluralkt.types.PkColor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinimalSystemJson(
    val tag: String?,
    val members: Array<MinimalMemberJson> = arrayOf(),
    val groups: Array<MinimalGroupJson> = arrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinimalSystemJson

        if (tag != other.tag) return false
        if (!members.contentEquals(other.members)) return false
        return groups.contentEquals(other.groups)
    }

    override fun hashCode(): Int {
        var result = tag?.hashCode() ?: 0
        result = 31 * result + members.contentHashCode()
        result = 31 * result + groups.contentHashCode()
        return result
    }
}

@Serializable
data class MinimalMemberJson(
    val id: String,
    val name: String,
    @SerialName("display_name")
    val displayName: String?,
    val pronouns: String?,
    val color: PkColor?,
    @SerialName("proxy_tags")
    val proxyTags: List<MinimalProxyTag>
)

@Serializable
data class MinimalGroupJson(
    val id: String,
    val members: Array<String> = arrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinimalGroupJson

        if (id != other.id) return false
        return members.contentEquals(other.members)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + members.contentHashCode()
        return result
    }
}

fun Array<MinimalGroupJson>.getById(id: String): MinimalGroupJson? {
    forEach {
        if (it.id == id) return it
    }
    return null
}

@Serializable
data class MinimalProxyTag(
    val prefix: String?,
    val suffix: String?
)
