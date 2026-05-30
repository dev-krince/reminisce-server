package com.krince.boilerplate.infra.adapter.out.persistence.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class UserOrmEntity(
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @Comment("회원 고유 식별자 (PK)")
    val userId: String,

    @Column(nullable = false, unique = true)
    @Comment("로그인 ID")
    val loginId: String,

    @Column(name = "role", nullable = false, unique = false)
    @Comment("역할/권한")
    val role: String,
) {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @Comment("생성일시")
    var createdDate: LocalDateTime? = null

    @Column(name = "modified_date", nullable = false)
    @LastModifiedDate
    @Comment("마지막 수정일시")
    var modifiedDate: LocalDateTime? = null
}