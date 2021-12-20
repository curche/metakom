package xyz.curche.metakom

import xyz.curche.metakom.config.parseConfiguration
import xyz.curche.metakom.config.Config
import xyz.curche.metakom.komga.Komga

fun main(args: Array<String>) {
    val configuration: Config = parseConfiguration()

    val komga = Komga(configuration)

    print(komga.checkUsers())
}