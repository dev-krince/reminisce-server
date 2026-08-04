package com.krince.reminisce.infra.adapter.out.persistence.child.entity

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
    name = "children",
    indexes = [
        Index(name = "idx_children_guardian_id", columnList = "guardian_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChildOrmEntity(
    @Id
    @Column(name = "child_id", nullable = false, unique = true, updatable = false)
    @Comment("아이 고유 식별자 (PK)")
    val childId: String,

    @Column(name = "guardian_id", nullable = false, updatable = false)
    @Comment("소유 보호자 회원 식별자 (FK 참조)")
    val guardianId: String,

    @Column(nullable = false)
    @Comment("아이 애칭")
    val nickname: String,

    @Column(name = "birth_year", nullable = false)
    @Comment("출생연도")
    val birthYear: Short,
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
