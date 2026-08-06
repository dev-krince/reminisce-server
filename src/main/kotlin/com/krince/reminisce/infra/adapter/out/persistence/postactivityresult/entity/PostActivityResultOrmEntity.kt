package com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.entity

import com.krince.reminisce.infra.adapter.out.persistence.postactivityresult.converter.SubmittedOrderConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
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
@Table(name = "post_activity_results")
@EntityListeners(AuditingEntityListener::class)
class PostActivityResultOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("후활동 결과 고유 식별자 (PK)")
    val id: String,

    @Column(name = "session_id", nullable = false, updatable = false)
    @Comment("말하기 세션 식별자 (FK 참조)")
    val sessionId: String,

    @Column(name = "submitted_order", columnDefinition = "text")
    @Convert(converter = SubmittedOrderConverter::class)
    @Comment("제출된 카드 순서 (JSON)")
    val submittedOrder: List<String>?,

    @Column(name = "is_order_correct", nullable = true)
    @Comment("순서 정답 여부")
    val isOrderCorrect: Boolean?,

    @Column(name = "attempt_count", nullable = false)
    @Comment("시도 횟수")
    val attemptCount: Int,

    @Column(name = "retelling_text", columnDefinition = "text", nullable = true)
    @Comment("재구성 발화 텍스트")
    val retellingText: String? = null,

    @Column(name = "completed_at", nullable = true)
    @Comment("완료 시각")
    val completedAt: LocalDateTime? = null,
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
