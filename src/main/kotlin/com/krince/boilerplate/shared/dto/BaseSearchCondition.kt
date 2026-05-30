package com.krince.boilerplate.shared.dto

import com.krince.boilerplate.shared.enums.SortColumn
import com.krince.boilerplate.shared.enums.SortDirection

interface BaseSearchCondition {
    val page: Long
    val size: Long
    val keyword: String?
    val sortBy: SortColumn
    val sortDirection: SortDirection
}