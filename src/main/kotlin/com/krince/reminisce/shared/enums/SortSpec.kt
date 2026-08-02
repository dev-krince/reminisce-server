package com.krince.reminisce.shared.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "SortSpec", description = "목록 다중 정렬의 한 단계. GET API에서는 `sort=SortColumn.SortDirection` 쿼리 파라미터로 전달하며, 앞에 올수록 ORDER BY 우선순위가 높습니다.")
class SortSpec(
    @field:Schema(
        description = "정렬 기준 컬럼. 허용 값은 `SortColumn` 스키마(enum)와 동일합니다.",
        example = "FACILITY_NAME",
        implementation = SortColumn::class,
        required = true,
    )
    val sortBy: SortColumn,

    @field:Schema(
        description = "정렬 방향. 허용 값은 `SortDirection` 스키마(enum)와 동일합니다.",
        example = "DESC",
        implementation = SortDirection::class,
        required = true,
    )
    val sortDirection: SortDirection = SortDirection.DEFAULT,
)