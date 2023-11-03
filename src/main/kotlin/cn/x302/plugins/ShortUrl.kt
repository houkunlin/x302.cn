package cn.x302.plugins

import cn.x302.utils.DataDiff.set
import cn.x302.utils.NanoIdUtils
import cn.x302.utils.UrlKey
import cn.x302.utils.idGenerator
import kotlinx.coroutines.Dispatchers
import org.apache.commons.codec.digest.DigestUtils
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.time.LocalDateTime

// URL 最大长度
const val URL_MAX_LENGTH = 8192

/**
 * 短链接数据库表映射关系配置
 *
 * @constructor Create empty Short url
 */
object ShortUrl : Table("short_url") {
    // 主键ID
    val id = long("id").clientDefault { idGenerator.nextId() }.uniqueIndex("unique_index")

    // 短链接 KEY
    val urlKey = varchar("url_key", length = 32).index("url_key")

    // 原始链接
    val urlRaw = varchar("url_raw", length = URL_MAX_LENGTH).index("url_raw")

    // 原始链接的 SHA256 值
    val urlSha256Hex = varchar("url_sha256_hex", length = 64).index("url_sha256_hex")

    // 原计划使用的一个短链接过期时间，但是目前未启用这个字段
    val expiredTime = datetime("expired_time").clientDefault { LocalDateTime.now().plusDays(60) }

    // 短链接被访问的次数
    val visitNum = integer("visit_num").default(0)

    // 创建IP
    val createdIp = varchar("created_ip", length = 255).index("created_ip")

    // 创建UA
    val createdUa = varchar("created_ua", length = 255)

    // 创建时间
    val createdTime = datetime("created_time").clientDefault { LocalDateTime.now() }

    // 更新时间
    val updatedTime = datetime("updated_time").clientDefault { LocalDateTime.now() }

    // 主键配置
    override val primaryKey = PrimaryKey(id)
}

/**
 * 短链接的视图类映射对象
 *
 * @property id 主键ID
 * @property urlKey 短链接 KEY
 * @property urlRaw 原始链接
 * @property urlSha256Hex 原始链接的 SHA256 值
 * @property expiredTime 原计划使用的一个短链接过期时间，但是目前未启用这个字段
 * @property visitNum 短链接被访问的次数
 * @property createdIp 创建IP
 * @property createdUa 创建UA
 * @property createdTime 创建时间
 * @property updatedTime 更新时间
 * @constructor Create empty Exposed short url v o
 */
data class ExposedShortUrlVO(
    // 主键ID
    val id: Long,
    // 短链接 KEY
    val urlKey: String,
    // 原始链接
    val urlRaw: String,
    // 原始链接的 SHA256 值
    val urlSha256Hex: String,
    // 原计划使用的一个短链接过期时间，但是目前未启用这个字段
    val expiredTime: LocalDateTime,
    // 短链接被访问的次数
    val visitNum: Int,
    // 创建IP
    val createdIp: String,
    // 创建UA
    val createdUa: String,
    // 创建时间
    val createdTime: LocalDateTime,
    // 更新时间
    val updatedTime: LocalDateTime,
) {
    constructor(resultRow: ResultRow) : this(
        resultRow[ShortUrl.id],
        resultRow[ShortUrl.urlKey],
        resultRow[ShortUrl.urlRaw],
        resultRow[ShortUrl.urlSha256Hex],
        resultRow[ShortUrl.expiredTime],
        resultRow[ShortUrl.visitNum],
        resultRow[ShortUrl.createdIp],
        resultRow[ShortUrl.createdUa],
        resultRow[ShortUrl.createdTime],
        resultRow[ShortUrl.updatedTime]
    )
}

/**
 * 短链接 Service
 *
 * @constructor
 *
 * @param database 数据库操作
 */
class ShortUrlService(database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(ShortUrl)
        }
    }

    /**
     * 获取最后几条数据
     *
     * @param latestLen 最后几条的数据量
     * @return 数据列表
     */
    suspend fun latestList(latestLen: Int): List<ExposedShortUrlVO> = dbQuery {
        ShortUrl.selectAll().orderBy(ShortUrl.id, SortOrder.DESC).limit(latestLen, 0).map { ExposedShortUrlVO(it) }
    }

    /**
     * 创建一个可用的短链接 KEY
     *
     * @param urlSha256Hex 原始链接的 SHA256 值
     * @return 短链接 KEY
     */
    suspend fun createKey(urlSha256Hex: String): String = dbQuery {
        // 创建几个 6 位的随机 KEY，并尝试从中抽取第一个可用的key
        val shortKeys = UrlKey.hexShortKeys(urlSha256Hex).filterNotNull().toSet()
        val findKeys = ShortUrl.select { ShortUrl.urlKey.inList(shortKeys) }
            .orderBy(ShortUrl.id, SortOrder.DESC)
            .map { it[ShortUrl.urlKey] }
            .toSet()

        val (_, _, newKeys) = set(findKeys, shortKeys)
        if (newKeys.isNotEmpty()) {
            return@dbQuery newKeys.first()
        }

        var key: String
        var index = 0
        do {
            ++index
            key = if (index <= 20) {
                UrlKey.uuidRandom8()
            } else if (index <= 40) {
                UrlKey.uuidRandom22()
            } else {
                NanoIdUtils.randomNanoId()
            }
            val count = ShortUrl.select { ShortUrl.urlKey.eq(key) }.count()
        } while (count >= 1)

        return@dbQuery key
    }

    /**
     * Db query
     *
     * @param T
     * @param block
     * @receiver
     * @return
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * 创建一个短链接
     *
     * @param url 链接地址
     * @param ip IP地址
     * @param ua UA内容
     * @return 短链接
     */
    suspend fun create(url: URL, ip: String, ua: String): ExposedShortUrlVO? = dbQuery {
        val urlStr = url.toString()
        val thatUrl = if (urlStr.length < URL_MAX_LENGTH) urlStr else urlStr.substring(0, URL_MAX_LENGTH - 1)
        val hex = DigestUtils.sha256Hex(thatUrl)

        val find = ShortUrl.select { ShortUrl.urlSha256Hex.eq(hex) }
            .orderBy(ShortUrl.id, SortOrder.DESC)
            .limit(1, 0)
            .map { ExposedShortUrlVO(it) }
            .singleOrNull()

        if (find != null) {
            return@dbQuery find
        }

        val key = createKey(hex)

        val id = ShortUrl.insert {
            it[urlKey] = key
            it[urlRaw] = urlStr
            it[urlSha256Hex] = hex
            it[createdIp] = ip
            it[createdUa] = ua
        }[ShortUrl.id]

        val shortUrlVO = ShortUrl.select { ShortUrl.id.eq(id) }.limit(1, 0).map { ExposedShortUrlVO(it) }.singleOrNull()

        shortUrlVO
    }

    /**
     * 根据短链接KEY获取一个短链接信息
     *
     * @param key 短链接KEY
     * @return 短链接
     */
    suspend fun getByUrlKey(key: String): ExposedShortUrlVO? = dbQuery {
        ShortUrl.select { ShortUrl.urlKey.eq(key) }
            .orderBy(ShortUrl.id, SortOrder.DESC)
            .limit(1, 0)
            .map { ExposedShortUrlVO(it) }
            .singleOrNull()
    }

    /**
     * 访问短链接，给短链接的访问次数 + 1
     *
     * @param id 短链接ID
     */
    suspend fun visit(id: Long) {
        dbQuery {
            ShortUrl.update {
                it[visitNum] = visitNum.plus(1)
                this.id.eq(id)
            }
        }
    }

    suspend fun delete(urlKey: String, ip: String) {
        dbQuery {
            ShortUrl.deleteWhere { this.urlKey.eq(urlKey) and this.createdIp.eq(ip) }
        }
    }
}
