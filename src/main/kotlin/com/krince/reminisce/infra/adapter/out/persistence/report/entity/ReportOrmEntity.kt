package com.krince.reminisce.infra.adapter.out.persistence.report.entity

import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.report.SceneHighlight
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.HomeGuideConverter
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.ParticipationItemsConverter
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.ReportOverallConverter
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.ReportSpeechAnalysesConverter
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.RepresentativeUtteranceConverter
import com.krince.reminisce.infra.adapter.out.persistence.report.converter.SceneHighlightsConverter
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

    @Column(name = "overall", columnDefinition = "text")
    @Convert(converter = ReportOverallConverter::class)
    @Comment("총평 (JSON)")
    val overall: ReportOverall?,

    @Column(name = "participation", columnDefinition = "text")
    @Convert(converter = ParticipationItemsConverter::class)
    @Comment("참여 모습 3항목 (JSON)")
    val participation: List<ParticipationItem>?,

    @Column(name = "speech_analyses", columnDefinition = "text")
    @Convert(converter = ReportSpeechAnalysesConverter::class)
    @Comment("말하기 분석 어휘·표현·논리 (JSON)")
    val speechAnalyses: List<ReportSpeechAnalysis>?,

    @Column(name = "scene_highlights", columnDefinition = "text")
    @Convert(converter = SceneHighlightsConverter::class)
    @Comment("장면별 특징 (JSON)")
    val sceneHighlights: List<SceneHighlight>?,

    @Column(name = "representative_utterance", columnDefinition = "text")
    @Convert(converter = RepresentativeUtteranceConverter::class)
    @Comment("대표 발화 (JSON)")
    val representativeUtterance: RepresentativeUtterance?,

    @Column(name = "home_guide", columnDefinition = "text")
    @Convert(converter = HomeGuideConverter::class)
    @Comment("가정 연계 가이드 (JSON)")
    val homeGuide: HomeGuide?,

    @Column(name = "created_at", nullable = false)
    @Comment("생성 시각")
    val createdAt: LocalDateTime,
)
