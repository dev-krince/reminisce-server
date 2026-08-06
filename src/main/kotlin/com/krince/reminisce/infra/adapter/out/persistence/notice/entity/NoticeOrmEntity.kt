package com.krince.reminisce.infra.adapter.out.persistence.notice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "notices")
@EntityListeners(AuditingEntityListener::class)
class NoticeOrmEntity(
    @Id
    @Column(name = "notice_id", nullable = false, unique = true, updatable = false)
    val noticeId: String,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, columnDefinition = "text")
    val content: String,

    @Column(nullable = false)
    val status: String,
) {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    var createdDate: LocalDateTime? = null
}
