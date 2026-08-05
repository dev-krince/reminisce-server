package com.krince.reminisce.infra.adapter.out.persistence.childconsent.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "child_consents",
    indexes = [
        Index(name = "idx_child_consents_child_id", columnList = "child_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChildConsentOrmEntity(
    @Id
    @Column(name = "consent_id", nullable = false, unique = true, updatable = false)
    @Comment("동의 고유 식별자 (PK)")
    val consentId: String,

    @Column(name = "child_id", nullable = false, updatable = false)
    @Comment("동의 대상 아이 식별자 (FK 참조)")
    val childId: String,

    @Column(name = "consent_version", nullable = false)
    @Comment("동의서 버전")
    val consentVersion: String,

    @Column(name = "verification_method", nullable = false)
    @Comment("동의 확인 방식")
    val verificationMethod: String,

    @Column(name = "consented_at", nullable = false)
    @Comment("동의 시각")
    val consentedAt: LocalDateTime,

    @Column(name = "withdrawn_at", nullable = true)
    @Comment("동의 철회 시각")
    val withdrawnAt: LocalDateTime? = null,
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
