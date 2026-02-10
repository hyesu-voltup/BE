package voltup.be.api.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import voltup.be.api.domain.entity.Product

/**
 * Product 엔티티 저장소.
 * 목적: 상품 조회/저장. 주문 시 재고 차감을 위해 비관적 락 사용.
 *
 * 배포 시 500 방지: products 테이블에 deleted 컬럼이 있어야 합니다.
 * 기존 DB에는 ALTER TABLE products ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE; 실행 필요.
 */
interface ProductRepository : JpaRepository<Product, Long> {

    fun findAllByDeletedFalse(): List<Product>
    fun findByIdAndDeletedFalse(productId: Long): Product?

    /**
     * 비관적 락으로 상품 조회 (주문 시 동시 재고 차감 방지). 삭제된 상품 제외.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.deleted = false")
    fun findByIdAndDeletedFalseForUpdate(@Param("productId") productId: Long): Product?
}
