package com.krince.reminisce.infra.adapter.out.persistence.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_provider_provider_id", columnNames = ["provider", "provider_id"]),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class UserOrmEntity(
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @Comment("회원 고유 식별자 (PK)")
    val userId: String,

    @Column(nullable = true, unique = true)
    @Comment("이메일 (로컬 로그인 식별자)")
    val email: String?,

    @Column(nullable = true)
    @Comment("비밀번호 (BCrypt 해시)")
    val password: String?,

    @Column(nullable = false)
    @Comment("닉네임")
    val nickname: String,

    @Column(nullable = false)
    @Comment("인증 제공자")
    val provider: String,

    @Column(name = "role", nullable = false, unique = false)
    @Comment("역할/권한")
    val role: String,

    @Column(name = "provider_id", nullable = true)
    @Comment("소셜 제공자의 사용자 식별자")
    val providerId: String? = null,
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
