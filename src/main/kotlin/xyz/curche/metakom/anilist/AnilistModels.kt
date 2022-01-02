package xyz.curche.metakom.anilist

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