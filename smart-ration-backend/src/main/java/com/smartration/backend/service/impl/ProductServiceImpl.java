package com.smartration.backend.service.impl;

import com.smartration.backend.dto.ProductRequest;
import com.smartration.backend.dto.ProductResponse;
import com.smartration.backend.entity.Product;
import com.smartration.backend.entity.Role;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.ProductRepository;
import com.smartration.backend.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        productRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(product -> {
                    throw new BadRequestException("Product already exists with name: " + request.getName());
                });

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .unit(request.getUnit())
                .managedByRole(Role.ROLE_ADMIN)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());
        product.setUnit(request.getUnit());

        return mapToResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        productRepository.delete(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .stockQuantity(product.getStockQuantity())
                .unit(product.getUnit())
                .build();
    }
}
