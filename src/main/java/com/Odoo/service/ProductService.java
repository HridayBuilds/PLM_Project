package com.Odoo.service;

import com.Odoo.dto.*;
import com.Odoo.model.*;
import com.Odoo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final BomRepository bomRepository;

    public Page<ProductDTO> getAllProducts(String search, Pageable pageable) {
        Page<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productRepository.searchProducts(search, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }
        return products.map(this::toDTO);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return toDTO(product);
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = new Product();
        updateProductFromDTO(product, dto);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        log.info("Created product: {} ({})", product.getName(), product.getInternalReference());
        return toDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        updateProductFromDTO(product, dto);
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        log.info("Updated product: {}", product.getId());
        return toDTO(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Deactivated product: {}", id);
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void updateProductFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setInternalReference(dto.getInternalReference());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setUnitOfMeasure(dto.getUnitOfMeasure());
        product.setCost(dto.getCost());
        product.setWeight(dto.getWeight());
        product.setVolume(dto.getVolume());
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setInternalReference(product.getInternalReference());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setCost(product.getCost());
        dto.setWeight(product.getWeight());
        dto.setVolume(product.getVolume());
        dto.setActive(product.getActive());

        // Get associated BOMs count
        List<Bom> boms = bomRepository.findByProductIdAndActiveTrue(product.getId());
        dto.setBomsCount(boms.size());

        return dto;
    }
}
