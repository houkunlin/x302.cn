package cn.x302.utils

import com.littlenb.snowflake.support.MillisIdGeneratorFactory

val idGeneratorFactory = MillisIdGeneratorFactory(1577808000000L)
val idGenerator = idGeneratorFactory.create(1L)
