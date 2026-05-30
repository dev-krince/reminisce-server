package com.krince.boilerplate.infra.adapter.out.persistence.support

import com.krince.boilerplate.infra.adapter.out.persistence.user.entity.QUserOrmEntity
import com.krince.boilerplate.shared.enums.SortColumn
import com.querydsl.core.types.dsl.ComparableExpressionBase

object SortColumnQueryPaths {
    val u: QUserOrmEntity = QUserOrmEntity("u")

    fun path(sortBy: SortColumn): ComparableExpressionBase<*> = when (sortBy) {
        SortColumn.USER_CREATED_DATE -> u.createdDate
        SortColumn.USER_MODIFIED_DATE -> u.modifiedDate
    }
}