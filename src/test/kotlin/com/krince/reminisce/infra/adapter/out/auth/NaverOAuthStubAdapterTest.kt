package com.krince.reminisce.infra.adapter.out.auth

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("NaverOAuthStubAdapter 단위테스트")
class NaverOAuthStubAdapterTest : FunSpec({

    val adapter = NaverOAuthStubAdapter()

    test("인가코드·state와 무관하게 항상 고정된 네이버 테스트 계정 정보를 반환한다") {
        val first = adapter.exchangeCodeForUser("any-code", "any-state")
        val second = adapter.exchangeCodeForUser("다른-코드", "다른-state")

        first.id shouldBe "naver-test-account"
        first.nickname shouldBe "네이버 테스트 계정"
        first.email shouldBe null
        second.id shouldBe first.id
    }
})
