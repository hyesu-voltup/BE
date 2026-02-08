package voltup.be.api.service.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.dto.response.ProductResponse
import voltup.be.api.exception.ErrorCode
import voltup.be.api.exception.NotFoundException
import voltup.be.api.repository.ProductRepository

/**
 * 어드민 상품 수정 서비스.
 */
@Service
class AdminProductService(
    private val productRepository: ProductRepository
) {

    @Transactional
    fun update(productId: Long, name: String?, pointPrice: Long?, stock: Int?): ProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NotFoundException(ErrorCode.NOT_FOUND_PRODUCT) }
        name?.let { product.name = it }
        pointPrice?.let { require(it >= 0) { "가격은 0 이상이어야 합니다." }; product.pointPrice = it }
        stock?.let { require(it >= 0) { "재고는 0 이상이어야 합니다." }; product.stock = it }
        product.touch()
        return ProductResponse.from(product)
    }
}
