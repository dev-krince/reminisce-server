package com.krince.reminisce.testutil

import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.infra.security.JwtProvider
import com.krince.reminisce.testutil.fixture.TestJwtTokenFixture
import com.krince.reminisce.testutil.fixture.TestUserFixture
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {

    @Bean
    fun testUserFixture(userRepository: UserRepository): TestUserFixture =
        TestUserFixture(userRepository = userRepository)

    @Bean
    fun testJwtTokenFixture(jwtProvider: JwtProvider): TestJwtTokenFixture =
        TestJwtTokenFixture(jwtProvider = jwtProvider)
}
