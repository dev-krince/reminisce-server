package com.krince.reminisce.shared.dto

import com.krince.reminisce.shared.enums.SortColumn
import com.krince.reminisce.shared.enums.SortDirection

interface BaseSearchCondition {
    val page: Long
    val size: Long
    val keyword: String?
    val sortBy: SortColumn
    val sortDirection: SortDirection
}