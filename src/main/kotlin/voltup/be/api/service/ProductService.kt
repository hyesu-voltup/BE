package voltup.be.api.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voltup.be.api.domain.entity.Product
import voltup.be.api.dto.request.ProductCreateRequest
import voltup.be.api.dto.response.ProductResponse
import voltup.be.api.exception.ErrorCode
import voltup.be.api.exception.NotFoundException
import voltup.be.api.repository.ProductRepository

/**
 * 상품 등록·조회 서비스.
 */
@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    @Transactional
    fun create(request: ProductCreateRequest): ProductResponse {
        val product = Product(
            name = request.name,
            pointPrice = request.pointPrice,
            stock = request.stock
        )
        val saved = productRepository.save(product)
        return ProductResponse.from(saved)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<ProductResponse> =
        productRepository.findAll().map { ProductResponse.from(it) }

    @Transactional(readOnly = true)
    fun getById(productId: Long): ProductResponse {
        val product = findProductById(productId)
        return ProductResponse.from(product)
    }

    /** 내부/주문용: 엔티티 조회 (락 없음). */
    fun findProductById(productId: Long): Product {
        return productRepository.findById(productId)
            .orElseThrow { NotFoundException(ErrorCode.NOT_FOUND_PRODUCT) }
    }
}
