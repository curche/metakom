package xyz.curche.metakom

import xyz.curche.metakom.config.parseConfiguration
import xyz.curche.metakom.config.Config

fun main(args: Array<String>) {
    val configuration: Config = parseConfiguration()
    print(configuration)
}