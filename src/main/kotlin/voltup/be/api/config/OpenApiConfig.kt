package voltup.be.api.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger(OpenAPI 3) 설정.
 * 목적: API 문서 제목·설명 설정.
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("포인트 룰렛 API")
                .description("포인트 룰렛 참여 및 상품 구매 API (Spring Boot 3.x, Kotlin)")
                .version("1.0.0")
        )
}
