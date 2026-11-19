package com.madhura;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Map<String, List<String>> CATEGORIES = Map.of(
            "Sarees", List.of(),
            "Jewellery", List.of("Necklace", "Earrings", "Bangles"),
            "Embroidery designs", List.of(),
            "Cloth pieces", List.of(),
            "Women's essentials", List.of());

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    // ---------- public: read only ----------
    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category,
                              @RequestParam(required = false) String sub) {
        Sort newest = Sort.by(Sort.Direction.DESC, "createdAt", "id");
        boolean hasCat = category != null && !category.isBlank();
        boolean hasSub = sub != null && !sub.isBlank();
        if (hasCat && hasSub) return repo.findByCategoryAndSubcategory(category, sub, newest);
        if (hasCat) return repo.findByCategory(category, newest);
        if (hasSub) return repo.findBySubcategory(sub, newest);
        return repo.findAll(newest);
    }

    @GetMapping("/{id}")
    public Product one(@PathVariable Long id) {
        return find(id);
    }

    // ---------- admin only (token checked in WebConfig) ----------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody ProductRequest r) {
        Product p = new Product();
        apply(p, r);
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody ProductRequest r) {
        Product p = find(id);
        apply(p, r);
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.delete(find(id));
    }

    private Product find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(404, "Product not found."));
    }

    private void apply(Product p, ProductRequest r) {
        String name = r.name() == null ? "" : r.name().trim();
        String category = r.category() == null ? "" : r.category();
        String sub = "Jewellery".equals(category) && r.subcategory() != null ? r.subcategory() : "";
        String desc = r.description() == null ? "" : r.description().trim();
        String img = r.imageUrl() == null ? "" : r.imageUrl().trim();

        if (name.isEmpty() || name.length() > 120) throw new ApiException(400, "Name is required (max 120 characters).");
        if (!CATEGORIES.containsKey(category)) throw new ApiException(400, "Choose a valid category.");
        if ("Jewellery".equals(category) && !CATEGORIES.get("Jewellery").contains(sub))
            throw new ApiException(400, "Choose Necklace, Earrings or Bangles.");
        if (r.price() == null || !Double.isFinite(r.price()) || r.price() < 0 || r.price() >= 100_000_000)
            throw new ApiException(400, "Enter a valid price.");
        if (desc.length() > 1000) throw new ApiException(400, "Description is too long (max 1000 characters).");
        if (img.length() > 1000 || (!img.isEmpty() && !img.matches("(?i)^https?://.+")))
            throw new ApiException(400, "Image link must start with http:// or https://");

        p.setName(name);
        p.setCategory(category);
        p.setSubcategory(sub);
        p.setPrice(BigDecimal.valueOf(r.price()));
        p.setDescription(desc);
        p.setImageUrl(img);
        p.setInStock(r.inStock() == null || r.inStock());
    }
}
