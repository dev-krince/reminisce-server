package com.krince.reminisce.infra.adapter.out.persistence.ttscache.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "tts_cache")
@EntityListeners(AuditingEntityListener::class)
class TtsCacheOrmEntity(
    @Id
    @Column(name = "cache_key", nullable = false, unique = true, updatable = false)
    @Comment("합성 캐시 키 (voiceProfile + text 해시, PK)")
    val cacheKey: String,

    @Column(name = "voice_profile", nullable = true, updatable = false)
    @Comment("합성에 사용된 보이스 프로필")
    val voiceProfile: String?,

    @Column(name = "file_url", nullable = false, updatable = false)
    @Comment("합성 결과 오디오 파일 URL")
    val fileUrl: String,
) {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @Comment("생성일시")
    var createdDate: LocalDateTime? = null
}
