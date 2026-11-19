package com.madhura;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category, Sort sort);

    List<Product> findBySubcategory(String subcategory, Sort sort);

    List<Product> findByCategoryAndSubcategory(String category, String subcategory, Sort sort);
}
