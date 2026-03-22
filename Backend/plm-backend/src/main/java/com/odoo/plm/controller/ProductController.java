package com.odoo.plm.controller;

import com.odoo.plm.dto.request.product.CreateProductRequest;
import com.odoo.plm.dto.request.product.ProductSearchRequest;
import com.odoo.plm.dto.response.product.ProductListResponse;
import com.odoo.plm.dto.response.product.ProductResponse;
import com.odoo.plm.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> activateProduct(@PathVariable UUID id) {
        ProductResponse response = productService.activateProduct(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ProductListResponse response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ProductListResponse> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ProductListResponse response = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<ProductListResponse> searchProducts(@Valid @RequestBody ProductSearchRequest request) {
        ProductListResponse response = productService.searchProducts(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{productName}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'ADMIN')")
    public ResponseEntity<List<ProductResponse>> getVersionHistory(@PathVariable String productName) {
        List<ProductResponse> response = productService.getProductVersionHistory(productName);
        return ResponseEntity.ok(response);
    }
}
