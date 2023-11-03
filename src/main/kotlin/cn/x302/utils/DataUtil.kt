package cn.x302.utils

import com.google.common.collect.Sets
import java.util.stream.Collectors


/**
 * 集合更新差异对比计算
 *
 * @author HouKunLin
 */
object DataDiff {
    /**
     * 对比集合差异
     *
     * @param dbKeys  数据库集合
     * @param newKeys 新的集合
     * @param <K>     对象类型
     * @return 集合差异结果</K>
     */
    operator fun <K> set(dbKeys: Set<K>, newKeys: Set<K>): View<K> {
        // 计算公共参数
        val commonIds: Sets.SetView<K> = Sets.intersection(dbKeys, newKeys)
        val deleteKeys = if (dbKeys.size > commonIds.size) {
            // DB数据大于公共数据，因此DB有数据需要删除
            Sets.difference(dbKeys, commonIds)
        } else {
            emptySet()
        }
        val newKeys1 = if (newKeys.size > commonIds.size) {
            // 新的数据大于公共数据，表示有新的数据需要保存
            Sets.difference(newKeys, commonIds)
        } else {
            emptySet()
        }

        return View(commonIds, deleteKeys, newKeys1)
    }

    /**
     * 对比集合差异
     *
     * @param dbList  数据库集合
     * @param newList 新的集合
     * @param getKey  获取对象的 KEY
     * @param <V>     对象类型
     * @param <K>     对象类型
     * @return 集合差异结果 </K></V>
     */
    fun <K, V> diffByKey(dbList: List<V>, newList: List<V>, getKey: java.util.function.Function<V, K>): View<K> {
        val newIds = newList.stream().map<K>(getKey).collect(Collectors.toSet())
        val dbIds = dbList.stream().map<K>(getKey).collect(Collectors.toSet())
        return set(dbIds, newIds)
    }

    data class View<K>(
        /**
         * 公共数据
         */
        val commonKeys: Set<K>,
        /**
         * 需要从DB删除的数据
         */
        val deleteKeys: Set<K>,
        /**
         * 需要保存的新KEY
         */
        val newKeys: Set<K>
    )
}

