package cn.x302

import cn.x302.plugins.configureHTTP
import cn.x302.plugins.configureMonitoring
import cn.x302.plugins.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureRouting()
}
