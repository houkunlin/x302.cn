package cn.x302.utils

import org.apache.commons.codec.digest.DigestUtils
import java.util.*

fun main() {
    println(UrlKey.uuidRandom8())
    println(UrlKey.uuidRandom22())
    println(NanoIdUtils.randomNanoId())

    val sLongUrl = "http://video.weibo.com/show?fid=1034:c775dfcdd18c16eff10665ff567a9853" //长链接
    println(UrlKey.shortUrl1(sLongUrl).contentDeepToString())
    println(UrlKey.shortUrl2(sLongUrl).contentDeepToString())
    println(UrlKey.shortUrl3(sLongUrl).contentDeepToString())
    println(DigestUtils.sha512Hex(sLongUrl) ?: "")
}

object UrlKey {
    val chars1 = arrayOf(
        "a", "b", "c", "d", "e", "f",
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z"
    )
    var chars2 = arrayOf(
        "a", "b", "c", "d", "e", "f",
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "-", "_"
    )

    fun uuidRandom8(): String {
        val shortBuffer = StringBuffer()
        val uuid = UUID.randomUUID().toString().replace("-", "")
        for (i in 0..7) {
            val str = uuid.substring(i * 4, i * 4 + 4)
            val x = str.toInt(16)
            shortBuffer.append(chars1[x % 0x3E])
        }
        return shortBuffer.toString()
    }

    fun uuidRandom22(): String {
        val shortBuffer = StringBuffer()
        val uuid = UUID.randomUUID().toString().replace("-", "")
        // 每3个十六进制字符转换成为2个字符
        for (i in 0..9) {
            val str = uuid.substring(i * 3, i * 3 + 3)
            val x = str.toInt(16) //转成十六进制
            shortBuffer.append(chars2[x / 0x40]) //除64得到前面6个二进制数的
            shortBuffer.append(chars2[x % 0x40]) //对64求余得到后面6个二进制数1
        }
        //加上后面两个没有改动的
        shortBuffer.append(uuid[30])
        shortBuffer.append(uuid[31])
        return shortBuffer.toString()
    }

    fun shortUrl1(url: String): Array<String?> {
        return hexShortKeys(DigestUtils.md5Hex(url) ?: "")
    }

    fun shortUrl2(url: String): Array<String?> {
        return hexShortKeys(DigestUtils.sha256Hex(url) ?: "")
    }

    fun shortUrl3(url: String): Array<String?> {
        return hexShortKeys(DigestUtils.sha512Hex(url) ?: "")
    }

    fun hexShortKeys(digestHex: String): Array<String?> {
        val resUrl = arrayOfNulls<String>(digestHex.length / 8)

        for (i in resUrl.indices) {
            // 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
            val sTempSubString = digestHex.substring(i * 8, i * 8 + 8)

            // 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 , 如果不用 long ，则会越界
            var lHexLong = (0x3FFFFFFF and sTempSubString.toLong(16).toInt()).toLong()
            var outChars = ""
            for (j in 0..5) {
                // 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
                val index = 0x0000003DL and lHexLong
                // 把取得的字符相加
                outChars += chars1[index.toInt()]
                // 每次循环按位右移 5 位
                lHexLong = lHexLong shr 5
            }
            // 把字符串存入对应索引的输出数组
            resUrl[i] = outChars
        }
        return resUrl
    }
}
