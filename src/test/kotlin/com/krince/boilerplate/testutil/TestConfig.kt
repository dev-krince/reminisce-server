package com.krince.boilerplate.testutil

import com.krince.boilerplate.infra.adapter.out.persistence.user.UserRepository
import com.krince.boilerplate.infra.security.JwtProvider
import com.krince.boilerplate.testutil.fixture.TestJwtTokenFixture
import com.krince.boilerplate.testutil.fixture.TestUserFixture
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
