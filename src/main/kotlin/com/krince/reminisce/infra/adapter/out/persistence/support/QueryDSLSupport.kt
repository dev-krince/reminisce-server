package com.krince.reminisce.infra.adapter.out.persistence.support

import com.krince.reminisce.shared.enums.SortColumn
import com.krince.reminisce.shared.enums.SortDirection
import com.krince.reminisce.shared.enums.SortSpec
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.BooleanPath
import com.querydsl.core.types.dsl.ComparableExpressionBase
import com.querydsl.core.types.dsl.DateTimePath
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.core.types.dsl.StringExpression
import com.querydsl.core.types.dsl.StringPath
import java.time.LocalDateTime

object QueryDSLSupport {
    fun getOffset(page: Long, size: Long): Long = (page - 1) * size

    fun getOrderSpecifiers(sortSpecs: List<SortSpec>): Array<OrderSpecifier<*>> =
        sortSpecs.map { getOrderSpecifier(it.sortDirection, it.sortBy) }.toTypedArray()

    private fun toOrder(sortDirection: SortDirection): Order = when (sortDirection) {
        SortDirection.ASC, SortDirection.DEFAULT -> Order.ASC
        SortDirection.DESC -> Order.DESC
    }

    private fun getOrderSpecifier(sortDirection: SortDirection, sortBy: SortColumn): OrderSpecifier<*> {
        val order = toOrder(sortDirection)
        return ascDesc(order, SortColumnQueryPaths.path(sortBy))
    }

    private fun <T : Comparable<T>> ascDesc(order: Order, path: ComparableExpressionBase<T>): OrderSpecifier<T> =
        if (order == Order.ASC) path.asc() else path.desc()

    fun keywordCondition(keyword: String?, path: StringExpression): BooleanExpression? {
        if (keyword.isNullOrBlank()) return null
        return path.containsIgnoreCase(keyword)
    }

    fun keywordCondition(keyword: String?, path: NumberPath<Double>): BooleanExpression? {
        if (keyword.isNullOrBlank()) return null

        val asText = Expressions.stringTemplate("cast({0} as string)", path)

        return keywordCondition(keyword, asText)
    }

    fun eqIfNotNull(value: Long?, path: NumberPath<Long>): BooleanExpression? {
        if (value == null) return null
        return path.eq(value)
    }

    fun eqIfNotNull(value: Double?, path: NumberPath<Double>): BooleanExpression? {
        if (value == null) return null
        return path.eq(value)
    }

    fun eqIfNotNull(value: Boolean?, path: BooleanPath): BooleanExpression? {
        if (value == null) return null
        return path.eq(value)
    }
}