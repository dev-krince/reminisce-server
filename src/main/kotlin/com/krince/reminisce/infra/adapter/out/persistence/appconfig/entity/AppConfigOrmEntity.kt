package com.krince.reminisce.infra.adapter.out.persistence.appconfig.entity

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
@Table(name = "app_configs")
@EntityListeners(AuditingEntityListener::class)
class AppConfigOrmEntity(
    @Id
    @Column(name = "config_key", nullable = false, unique = true, updatable = false)
    @Comment("운영 설정 키 (PK)")
    val configKey: String,

    @Column(name = "config_value", nullable = false, columnDefinition = "text")
    @Comment("운영 설정 값 (JSON)")
    val configValue: String,
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
