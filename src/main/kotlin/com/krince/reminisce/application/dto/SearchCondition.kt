package com.krince.reminisce.application.dto

import com.krince.reminisce.shared.dto.BaseSearchCondition
import com.krince.reminisce.shared.enums.SortColumn
import com.krince.reminisce.shared.enums.SortDirection

class SearchCondition(
    override val page: Long,
    override val size: Long,
    override val keyword: String?,
    override val sortBy: SortColumn,
    override val sortDirection: SortDirection,
) : BaseSearchCondition