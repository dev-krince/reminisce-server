package com.krince.reminisce.infra.adapter.out.persistence.message.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    name = "messages",
    indexes = [
        Index(name = "idx_messages_session_id_turn_order", columnList = "session_id, turn_order"),
    ],
)
class MessageOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("메시지 고유 식별자 (PK)")
    val id: String,

    @Column(name = "session_id", nullable = false, updatable = false)
    @Comment("소속 세션 식별자 (FK 참조)")
    val sessionId: String,

    @Column(name = "scene_id", nullable = false, updatable = false)
    @Comment("메시지가 발생한 장면 식별자 (FK 참조)")
    val sceneId: String,

    @Column(name = "speaker_type", nullable = false, updatable = false)
    @Comment("발화 주체 (child/character/system)")
    val speakerType: String,

    @Column(name = "turn_order", nullable = false, updatable = false)
    @Comment("세션 전체에서 메시지가 발생한 순서")
    val turnOrder: Long,

    @Column(name = "text", nullable = false, columnDefinition = "text", updatable = false)
    @Comment("확정 텍스트")
    val text: String,

    @Column(name = "stt_raw_text", columnDefinition = "text", updatable = false)
    @Comment("아이 발화 STT 원문 (아이 발화에만 저장)")
    val sttRawText: String?,

    @Column(name = "audio_url", columnDefinition = "text", updatable = false)
    @Comment("아이 발화 음성 파일 URL (아이 발화에만 저장, 선택)")
    val audioUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성 시각")
    val createdAt: LocalDateTime,
)
