package com.smartration.backend.service;

import com.smartration.backend.dto.ProductRequest;
import com.smartration.backend.dto.ProductResponse;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long productId);
}
