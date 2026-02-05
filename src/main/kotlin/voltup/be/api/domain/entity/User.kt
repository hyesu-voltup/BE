package voltup.be.api.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

/**
 * 사용자 엔티티.
 * 목적: 포인트 룰렛/상품 구매의 주체.
 */
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true, length = 100)
    var loginId: String,

    @Column(nullable = false, length = 100)
    var name: String
) : BaseEntity() {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @OneToOne(mappedBy = "user", orphanRemoval = true)
    var point: Point? = null
        protected set

    /** 포인트 계정 연결 (양방향). 도메인에서만 설정용. */
    fun setPointAccount(account: Point?) {
        this.point = account
    }
}
