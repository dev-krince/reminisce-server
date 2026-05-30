package com.krince.boilerplate.infra.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class QueryDSLConfig {

    @Bean
    fun jpaQueryFactory(@Lazy entityManager: EntityManager): JPAQueryFactory = JPAQueryFactory(entityManager)
}