package xyz.curche.metakom.config

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

data class Config(
    val baseUrl: String,
    val username: String,
    val password: String,
)

fun isComment(line: String) = line.startsWith("#") || line.startsWith(";")

fun toKeyValuePair(line: String) = line.split(Regex("[\\s=]"), 2).let {
    Pair(it[0], if (it.size == 1 ) "" else it[1])
}

fun toConfig(hashMap: HashMap<String, String>) = Config(
    hashMap["BASE_URL"] ?: "http://localhost:8080",
    hashMap["USERNAME"] ?: "demo",
    hashMap["PASSWORD"] ?: "demo"
)

fun parseConfiguration(): Config {
    val lines = Files.readAllLines(Paths.get("config.txt"), StandardCharsets.UTF_8)
    val keyValuePairs = lines.map{ it.trim() }
        .filterNot { it.isEmpty() }
        .filterNot { isComment(it) }
        .map { toKeyValuePair(it) }

    val configurationMap = hashMapOf<String, String>()
    for (pair in keyValuePairs) {
        val (key, value) = pair
        when {
            key.contains("URL", ignoreCase = true) -> configurationMap.put("BASE_URL", value)
            key.contains("USERNAME", ignoreCase = true) -> configurationMap.put("USERNAME", value)
            key.contains("PASSWORD", ignoreCase = true) -> configurationMap.put("PASSWORD", value)
            else -> println("Encountered unexpected pair $key = $value")
        }
    }

    return toConfig(configurationMap)
}