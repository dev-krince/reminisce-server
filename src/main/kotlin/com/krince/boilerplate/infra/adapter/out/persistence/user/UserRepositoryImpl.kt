package com.krince.boilerplate.infra.adapter.out.persistence.user

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(private val queryFactory: JPAQueryFactory) : UserCustomRepository {
}