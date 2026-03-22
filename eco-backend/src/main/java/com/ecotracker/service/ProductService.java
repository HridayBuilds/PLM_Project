package com.ecotracker.service;

import com.ecotracker.dto.*;
import com.ecotracker.model.*;
import com.ecotracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final BomRepository bomRepository;

    // ==================== Products ====================

    public ProductDTO createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        product.setReference(dto.getReference());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setType(dto.getType() != null ? dto.getType() : Product.ProductType.PRODUCT);
        product.setUom(dto.getUom() != null ? dto.getUom() : "unit");
        product.setCost(dto.getCost());
        product.setActive(true);

        if (dto.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        log.info("Created product: {}", product.getReference());
        return mapToDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getType() != null) product.setType(dto.getType());
        if (dto.getUom() != null) product.setUom(dto.getUom());
        if (dto.getCost() != null) product.setCost(dto.getCost());
        if (dto.getActive() != null) product.setActive(dto.getActive());

        if (dto.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        return mapToDTO(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
        log.info("Deactivated product: {}", product.getReference());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductByReference(String reference) {
        Product product = productRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> listProducts(String search, Long categoryId, Boolean activeOnly, Pageable pageable) {
        Page<Product> products;

        if (search != null && !search.isEmpty()) {
            products = productRepository.search(search, activeOnly != null && activeOnly, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else if (activeOnly != null && activeOnly) {
            products = productRepository.findByActiveTrue(pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String query, int limit) {
        return productRepository.findByNameContainingIgnoreCaseOrReferenceContainingIgnoreCase(
                        query, query, PageRequest.of(0, limit))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================== Categories ====================

    public ProductCategoryDTO createCategory(CategoryCreateDTO dto) {
        ProductCategory category = new ProductCategory();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            ProductCategory parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        log.info("Created category: {}", category.getName());
        return mapCategoryToDTO(category);
    }

    public ProductCategoryDTO updateCategory(Long id, CategoryUpdateDTO dto) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (dto.getName() != null) category.setName(dto.getName());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("Category cannot be its own parent");
            }
            ProductCategory parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return mapCategoryToDTO(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        log.info("Deleted category: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getAllCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductCategoryDTO getCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapCategoryToDTO(category);
    }

    // ==================== Mapping Methods ====================

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setReference(product.getReference());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setType(product.getType());
        dto.setUom(product.getUom());
        dto.setCost(product.getCost());
        dto.setActive(product.isActive());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Check if product has BOM
        dto.setHasBom(bomRepository.existsByProductId(product.getId()));

        return dto;
    }

    private ProductCategoryDTO mapCategoryToDTO(ProductCategory category) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        // Recursively map children
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(this::mapCategoryToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
