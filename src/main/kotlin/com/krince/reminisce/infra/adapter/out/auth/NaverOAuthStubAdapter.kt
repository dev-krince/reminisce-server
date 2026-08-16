package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.application.port.out.auth.NaverOAuthPort
import com.krince.reminisce.application.port.out.auth.NaverUserInfo
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["oauth.naver.mode"], havingValue = "stub")
class NaverOAuthStubAdapter : NaverOAuthPort {

    override fun exchangeCodeForUser(authorizationCode: String, state: String): NaverUserInfo =
        NaverUserInfo(
            id = TEST_PROVIDER_ID,
            email = null,
            nickname = TEST_NICKNAME,
        )

    private companion object {
        const val TEST_PROVIDER_ID = "naver-test-account"
        const val TEST_NICKNAME = "네이버 테스트 계정"
    }
}
