package cn.x302.plugins

import cn.x302.exts.getDatabase
import cn.x302.exts.requestIp
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.p
import java.net.URL

fun Application.configureRouting() {
    install(StatusPages) {
        // 配置500
        exception<Throwable> { call, throwable ->
            call.respondHtml {
                body {
                    h1 { +"发现错误" }
                    p { +"错误类型：${throwable.javaClass.name}" }
                    p { +"错误信息：${throwable.message}" }
                    a {
                        href = "/"
                        +"返回首页"
                    }
                }
            }
        }
        // 处理http 404
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondHtml {
                body {
                    h1 { +"404" }
                    a {
                        href = "/"
                        +"返回首页"
                    }
                }
            }
        }
    }
    val database = getDatabase(false)
    val shortUserService = ShortUrlService(database)
    routing {
        get("/") {
            val paramsUrl = call.parameters["url"]
            val urlObj = paramsUrl?.let {
                if (it.isNotBlank()) {
                    try {
                        return@let URL(it)
                    } catch (e: Exception) {
                        throw Exception("URL解析错误，请传入正确的URL", e)
                    }
                }
                return@let null
            }
            val currentIp = call.request.requestIp

            val vo = urlObj?.let {
                shortUserService.create(it, currentIp, call.request.headers["User-Agent"] ?: "")
            }

            val host = call.url { parameters.clear() }

            val voList = shortUserService.latestList(100)
            call.respondTemplate(
                "index.ftl",
                mapOf(
                    "data" to voList,
                    "vo" to vo,
                    "url" to paramsUrl,
                    "host" to host,
                    "jumpUrl" to host + "s/",
                    "currentIp" to currentIp
                )
            )
        }
        get("/s/{key}") {
            val vo = call.parameters["key"]?.let {
                shortUserService.getByUrlKey(it)
            }

            if (vo != null) {
                shortUserService.visit(vo.id)
                call.respondRedirect(vo.urlRaw, false)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post("/s/{key}") {
            if (call.parameters["action"] != "delete") {
                call.respond(HttpStatusCode.OK)
                return@post
            }
            call.parameters["key"]?.let {
                shortUserService.delete(it, call.request.requestIp)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}
