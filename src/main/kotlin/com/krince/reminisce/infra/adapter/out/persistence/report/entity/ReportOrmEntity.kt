package com.krince.reminisce.infra.adapter.out.persistence.report.entity

import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.ThinkingElementsConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    name = "reports",
    indexes = [
        Index(name = "idx_reports_session_id", columnList = "session_id", unique = true),
    ],
)
class ReportOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("리포트 고유 식별자 (PK)")
    val id: String,

    @Column(name = "session_id", nullable = false, updatable = false)
    @Comment("말하기 세션 식별자 (FK 참조)")
    val sessionId: String,

    @Column(name = "summary", nullable = false, columnDefinition = "text", updatable = false)
    @Comment("스텁 생성 요약문")
    val summary: String,

    @Column(name = "strengths", nullable = false, columnDefinition = "text", updatable = false)
    @Convert(converter = ThinkingElementsConverter::class)
    @Comment("세션에서 확인된 사고 요소 (JSON)")
    val strengths: List<ThinkingElement>,

    @Column(name = "next_focus", nullable = false, columnDefinition = "text", updatable = false)
    @Convert(converter = ThinkingElementsConverter::class)
    @Comment("아직 보여주지 않은 사고 요소 (JSON)")
    val nextFocus: List<ThinkingElement>,

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("생성 시각")
    val createdAt: LocalDateTime,
)
