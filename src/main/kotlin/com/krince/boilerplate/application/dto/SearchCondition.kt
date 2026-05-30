package com.krince.boilerplate.application.dto

import com.krince.boilerplate.shared.dto.BaseSearchCondition
import com.krince.boilerplate.shared.enums.SortColumn
import com.krince.boilerplate.shared.enums.SortDirection

class SearchCondition(
    override val page: Long,
    override val size: Long,
    override val keyword: String?,
    override val sortBy: SortColumn,
    override val sortDirection: SortDirection,
) : BaseSearchCondition