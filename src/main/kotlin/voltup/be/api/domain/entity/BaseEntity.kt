package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

/**
 * 모든 엔티티의 공통 필드 (생성/수정 시각).
 * 목적: 중복 제거 및 감사(Audit) 정보 보장.
 */
@MappedSuperclass
abstract class BaseEntity {

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    /** 엔티티 수정 시 updatedAt 갱신용. 서비스/도메인에서 호출. */
    fun touch() {
        updatedAt = LocalDateTime.now()
    }
}
