package xyz.curche.metakom.komga

import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    val id: String,
    val libraryId: String,
    val name: String,
    val created: String?,
    val lastModified: String?,
    val fileLastModified: String,
    val booksCount: Int,
    val metadata: SeriesMetadataDto,
    val booksMetadata: BookMetadataAggregationDto
)

@Serializable
data class SeriesMetadataDto(
    val status: String,
    val created: String?,
    val lastModified: String?,
    val title: String,
    val titleSort: String,
    val summary: String,
    val summaryLock: Boolean,
    val readingDirection: String,
    val readingDirectionLock: Boolean,
    val publisher: String,
    val publisherLock: Boolean,
    val ageRating: Int?,
    val ageRatingLock: Boolean,
    val language: String,
    val languageLock: Boolean,
    val genres: Set<String>,
    val genresLock: Boolean,
    val tags: Set<String>,
    val tagsLock: Boolean
)

@Serializable
data class BookMetadataAggregationDto(
    val authors: List<AuthorDto> = emptyList(),
    val tags: Set<String> = emptySet(),
    val releaseDate: String?,
    val summary: String,
    val summaryNumber: String,

    val created: String,
    val lastModified: String
)

@Serializable
data class AuthorDto(
    val name: String,
    val role: String
)

@Serializable
data class PageWrapperDto<T>(
    val content: List<T>,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Long,
    val numberOfElements: Long,
    val size: Long,
    val totalElements: Long,
    val totalPages: Long
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val roles: List<String>,
)

fun Any.prettyPrint(): String {
    var indentLevel = 0
    val indentWidth = 4

    fun padding() = "".padStart(indentLevel * indentWidth)

    val toString = toString()

    val stringBuilder = StringBuilder(toString.length)

    var i = 0
    while (i < toString.length) {
        when (val char = toString[i]) {
            '(', '[', '{' -> {
                indentLevel++
                stringBuilder.appendLine(char).append(padding())
            }
            ')', ']', '}' -> {
                indentLevel--
                stringBuilder.appendLine().append(padding()).append(char)
            }
            ',' -> {
                stringBuilder.appendLine(char).append(padding())
                // ignore space after comma as we have added a newline
                val nextChar = toString.getOrElse(i + 1) { char }
                if (nextChar == ' ') i++
            }
            else -> {
                stringBuilder.append(char)
            }
        }
        i++
    }

    return stringBuilder.toString()
}
