package cn.x302.exts

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return format(DateTimeFormatter.ofPattern(pattern))
}
