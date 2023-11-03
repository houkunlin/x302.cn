/**
 * Copyright (c) 2017 The JNanoID Authors
 * Copyright (c) 2017 Aventrix LLC
 * Copyright (c) 2017 Andrey Sitnik
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package cn.x302.utils

import java.security.SecureRandom
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln

// package com.aventrix.jnanoid.jnanoid;
/**
 * A class for generating unique String IDs.
 *
 *
 * The implementations of the core logic in this class are based on NanoId, a JavaScript
 * library by Andrey Sitnik released under the MIT license. (https://github.com/ai/nanoid)
 *
 * @author David Klebanoff
 */
object NanoIdUtils {
    /**
     * The default random number generator used by this class.
     * Creates cryptographically strong NanoId Strings.
     */
    val DEFAULT_NUMBER_GENERATOR = SecureRandom()

    /**
     * The default alphabet used by this class.
     * Creates url-friendly NanoId Strings using 64 unique symbols.
     */
    val DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

    /**
     * The default size used by this class.
     * Creates NanoId Strings with slightly more unique values than UUID v4.
     */
    const val DEFAULT_SIZE = 29
    /**
     * Static factory to retrieve a url-friendly, pseudo randomly generated, NanoId String.
     *
     *
     * The generated NanoId String will have 21 symbols.
     *
     *
     * The NanoId String is generated using a cryptographically strong pseudo random number
     * generator.
     *
     * @param random   The random number generator.
     * @param alphabet The symbols used in the NanoId String.
     * @param size     The number of symbols in the NanoId String.
     * @return A randomly generated NanoId String.
     */
    @JvmOverloads
    fun randomNanoId(
        random: Random? = DEFAULT_NUMBER_GENERATOR,
        alphabet: CharArray? = DEFAULT_ALPHABET,
        size: Int = DEFAULT_SIZE
    ): String {
        requireNotNull(random) { "random cannot be null." }
        requireNotNull(alphabet) { "alphabet cannot be null." }
        require(!(alphabet.isEmpty() || alphabet.size >= 256)) { "alphabet must contain between 1 and 255 symbols." }
        require(size > 0) { "size must be greater than zero." }
        val mask = (2 shl floor(ln((alphabet.size - 1).toDouble()) / ln(2.0)).toInt()) - 1
        val step = ceil(1.6 * mask * size / alphabet.size).toInt()
        val idBuilder = StringBuilder()
        while (true) {
            val bytes = ByteArray(step)
            random.nextBytes(bytes)
            for (i in 0 until step) {
                val alphabetIndex = bytes[i].toInt() and mask
                if (alphabetIndex < alphabet.size) {
                    idBuilder.append(alphabet[alphabetIndex])
                    if (idBuilder.length == size) {
                        return idBuilder.toString()
                    }
                }
            }
        }
    }
}
