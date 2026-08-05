package com.krince.reminisce.testutil

import com.krince.reminisce.application.port.out.auth.PasswordEncoderPort
import com.krince.reminisce.infra.adapter.out.persistence.child.ChildRepository
import com.krince.reminisce.infra.adapter.out.persistence.childconsent.ChildConsentRepository
import com.krince.reminisce.infra.adapter.out.persistence.user.UserRepository
import com.krince.reminisce.infra.security.JwtProvider
import com.krince.reminisce.testutil.fixture.TestAuthUserFixture
import com.krince.reminisce.testutil.fixture.TestChildConsentFixture
import com.krince.reminisce.testutil.fixture.TestChildFixture
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
    fun testChildFixture(childRepository: ChildRepository): TestChildFixture =
        TestChildFixture(childRepository = childRepository)

    @Bean
    fun testChildConsentFixture(childConsentRepository: ChildConsentRepository): TestChildConsentFixture =
        TestChildConsentFixture(childConsentRepository = childConsentRepository)

    @Bean
    fun testAuthUserFixture(
        userRepository: UserRepository,
        passwordEncoderPort: PasswordEncoderPort,
    ): TestAuthUserFixture =
        TestAuthUserFixture(userRepository = userRepository, passwordEncoderPort = passwordEncoderPort)

    @Bean
    fun testJwtTokenFixture(jwtProvider: JwtProvider): TestJwtTokenFixture =
        TestJwtTokenFixture(jwtProvider = jwtProvider)
}
