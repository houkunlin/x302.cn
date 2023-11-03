package cn.x302.exts

import io.ktor.server.plugins.*
import io.ktor.server.request.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

private val logger: Logger = LoggerFactory.getLogger(ApplicationRequest::class.java)
private val IP_KEYS = arrayOf("X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP")
private val DEFAULT_LOCAL_IP6 = arrayOf("0:0:0:0:0:0:0:1", "::1")
val IP_PATTERN = Pattern.compile("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}")

val ApplicationRequest.requestIp: String
    get() {
        var ip: String? = null
        var hasIp = false
        for (ipKey in IP_KEYS) {
            ip = headers[ipKey]
            hasIp = hasIp(ip)
            if (hasIp) {
                break
            }
        }
        if (!hasIp) {
            ip = origin.remoteAddress
        }
        if (logger.isDebugEnabled) {
            logger.debug(
                "{} {} for proxy ip or real ip: {}",
                httpMethod.value,
                uri,
                ip
            )
        }
        return if (ip == null) {
            ""
        } else {
            obtainIp(ip)
        }
    }


/**
 * 判断IP是否为空。是否拥有IP
 *
 * @param ip IP内容
 * @return 结果
 */
private fun hasIp(ip: String?): Boolean {
    if (ip == null) {
        return false
    }
    return ip.isNotBlank() && !"unknown".equals(ip, ignoreCase = true)
}

/**
 * 获取IP信息（去除本机的IPv6地址），只取第一个IP信息
 *
 * @param ip IP内容（有可能是以逗号分隔的字符串）
 * @return IP信息
 */
private fun obtainIp(ip: String): String {
    var realIp = ip
    if (realIp.contains(",")) {
        realIp = ip.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }
    for (defaultLocalIp in DEFAULT_LOCAL_IP6) {
        if (defaultLocalIp == realIp) {
            return "127.0.0.1"
        }
    }
    if (IP_PATTERN.matcher(realIp).matches()) {
        return realIp
    }
    if (logger.isDebugEnabled) {
        logger.debug("real ip {} is not ip, return ip 0.0.0.0", realIp)
    }
    return "0.0.0.0"
}
