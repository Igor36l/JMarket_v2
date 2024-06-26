package com.bigproject.client_view.client.productClient;

import com.bigproject.client_view.client.reviewClient.exception.BadRequestException;
import com.bigproject.client_view.controllers.payload.NewProductPayload;
import com.bigproject.client_view.controllers.payload.UpdateProductPayload;
import com.bigproject.client_view.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
public class RestClientProductsRestClient implements ProductRestClient {

    private final static ParameterizedTypeReference<List<Product>> PRODUCT_TYPE_REFERENCE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    @Override
    public List<Product> findAllProducts(String filter) {
        return this.restClient
                .get()
                .uri("/catalogue-api/products?filter={filter}",filter)
                .retrieve()
                .body(PRODUCT_TYPE_REFERENCE);
    }

    @Override
    public Product createProduct(String title, String details, String imageFileName, String ownerProduct) {
        try {
            return this.restClient
                    .post()
                    .uri("catalogue-api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NewProductPayload(title, details, imageFileName, ownerProduct))
                    .retrieve()
                    .body(Product.class);
        }catch (HttpClientErrorException.BadRequest exception){
           ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
          throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public Optional<Product> findProduct(int productId) {
        try {
            return Optional.ofNullable(this.restClient.get()
                    .uri("catalogue-api/products/{productId}", productId)
                    .retrieve()
                    .body(Product.class));
        } catch (HttpClientErrorException.NotFound exception){
            return Optional.empty();
        }
    }

    @Override
    public void updateProduct(int productId, String title, String details) {
        try {
            this.restClient
                    .patch()
                    .uri("catalogue-api/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UpdateProductPayload(title, details, 0))
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.BadRequest exception){
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public void updateProduct(int productId, Integer averageRating) {
        try {
            this.restClient
                    .patch()
                    .uri("catalogue-api/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UpdateProductPayload(null,null, averageRating))
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.BadRequest exception){
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
          this.restClient.delete()
                    .uri("catalogue-api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound exception){
            throw new NoSuchElementException(exception);
        }
    }

}
