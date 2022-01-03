package xyz.curche.metakom.anilist

import kotlinx.serialization.Serializable

data class ALSearchResult(
    val anilist_id: Int,
    val mal_id: Int?,
    val title_romaji: String,
    val title_english: String,
    val format: String,
    val publication_status: String,
    val country_of_origin: String,
    val start_date_fuzzy: String,
    val isAdult: Boolean,
)

@Serializable
data class ALResponse<T>(
    val data: T,
)

@Serializable
data class ALMedia(
    val Media: ALMetadata,
)

@Serializable
data class ALMetadata(
    val id: Int,
    val idMal: Int?,
    val title: ALTitle,
    val format: String,
    val status: String,
    val description: String,
    val countryOfOrigin: String,
    val source: String,
    val genres: List<String>,
    val staff: ALStaff,
    val isAdult: Boolean,
    val siteUrl: String,
    val chapters: Int?,
    val volumes: Int?,
    val tags: List<ALTag>,
)

@Serializable
data class ALTitle(
    val english: String?,
    val romaji: String,
)

@Serializable
data class ALStaff(
    val edges: List<ALStaffEdge>,
)

@Serializable
data class ALStaffEdge(
    val node: ALStaffNode,
    val role: String,
)

@Serializable
data class ALStaffNode(
    val name: ALStaffName,
    val languageV2: String,
)

@Serializable
data class ALStaffName(
    val full: String,
)

@Serializable
data class ALTag(
    val name: String,
    val category: String,
    val isGeneralSpoiler: Boolean,
    val rank: Int,
)