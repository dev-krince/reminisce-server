package com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.entity

import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.infra.adapter.out.persistence.utteranceanalysis.converter.DetectedElementsConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "utterance_analyses",
    indexes = [
        Index(name = "idx_utterance_analyses_message_id", columnList = "message_id"),
    ],
)
class UtteranceAnalysisOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("발화 분석 고유 식별자 (PK)")
    val id: String,

    @Column(name = "message_id", nullable = false, updatable = false)
    @Comment("분석 대상 아이 메시지 식별자 (FK 참조)")
    val messageId: String,

    @Column(name = "child_intent", nullable = false, updatable = false)
    @Comment("이번 발화의 중심 의도")
    val childIntent: String,

    @Column(name = "main_point", columnDefinition = "text", updatable = false)
    @Comment("아이 말의 핵심 뜻")
    val mainPoint: String?,

    @Column(name = "detected_elements", nullable = false, columnDefinition = "text", updatable = false)
    @Convert(converter = DetectedElementsConverter::class)
    @Comment("이번 발화에서 확인된 사고 요소와 근거")
    val detectedElements: List<DetectedElement>,

    @Column(name = "utterance_validity", nullable = false, updatable = false)
    @Comment("이번 발화의 유효성")
    val utteranceValidity: String,
)
