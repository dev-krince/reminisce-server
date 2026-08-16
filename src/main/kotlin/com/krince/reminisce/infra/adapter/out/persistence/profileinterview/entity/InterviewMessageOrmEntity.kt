package com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    name = "profile_interview_messages",
    indexes = [
        Index(name = "idx_profile_interview_messages_interview_id_turn_order", columnList = "interview_id, turn_order"),
    ],
)
class InterviewMessageOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("인터뷰 메시지 고유 식별자 (PK)")
    val id: String,

    @Column(name = "interview_id", nullable = false, updatable = false)
    @Comment("소속 프로필 인터뷰 식별자 (FK 참조)")
    val interviewId: String,

    @Column(name = "speaker", nullable = false, updatable = false)
    @Comment("발화 주체 (QUMI/CHILD)")
    val speaker: String,

    @Column(name = "turn_order", nullable = false, updatable = false)
    @Comment("인터뷰 전체에서 메시지가 발생한 순서")
    val turnOrder: Long,

    @Column(name = "text", nullable = false, columnDefinition = "text", updatable = false)
    @Comment("확정 텍스트")
    val text: String,

    @Column(name = "stt_raw_text", columnDefinition = "text", updatable = false)
    @Comment("아이 발화 STT 원문 (아이 발화에만 저장)")
    val sttRawText: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성 시각")
    val createdAt: LocalDateTime,
)
