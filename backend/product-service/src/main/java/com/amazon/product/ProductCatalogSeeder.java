package com.amazon.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Component
public class ProductCatalogSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(ProductCatalogSeeder.class);

  private final JdbcTemplate jdbcTemplate;
  private final RestClient restClient;
  private final boolean enabled;
  private final String sourceUrl;

  public ProductCatalogSeeder(
      JdbcTemplate jdbcTemplate,
      @Value("${products.seed.enabled}") boolean enabled,
      @Value("${products.seed.source-url}") String sourceUrl) {
    this.jdbcTemplate = jdbcTemplate;
    this.restClient = RestClient.create();
    this.enabled = enabled;
    this.sourceUrl = sourceUrl;
  }

  @Override
  @Transactional
  public void run(String... args) {
    ensureImageColumn();
    if (!enabled || productCount() > 0) {
      return;
    }

    try {
      DummyJsonCatalog catalog = restClient.get()
          .uri(sourceUrl)
          .retrieve()
          .body(DummyJsonCatalog.class);

      if (catalog == null || catalog.products() == null || catalog.products().isEmpty()) {
        log.warn("Product seed source returned no products: {}", sourceUrl);
        return;
      }

      for (DummyJsonProduct product : catalog.products()) {
        saveProduct(product);
      }
      log.info("Seeded {} products from {}", catalog.products().size(), sourceUrl);
    } catch (RuntimeException ex) {
      log.warn("Unable to seed products from {}. The catalog will remain database-only.", sourceUrl, ex);
    }
  }

  private void ensureImageColumn() {
    jdbcTemplate.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url TEXT");
  }

  private int productCount() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
    return count == null ? 0 : count;
  }

  private void saveProduct(DummyJsonProduct product) {
    Long categoryId = upsertCategory(product.category());
    BigDecimal price = BigDecimal.valueOf(product.price()).setScale(2, RoundingMode.HALF_UP);
    BigDecimal mrp = product.discountPercentage() > 0
        ? price.multiply(BigDecimal.valueOf(1 + product.discountPercentage() / 100)).setScale(2, RoundingMode.HALF_UP)
        : price;

    Long productId = jdbcTemplate.queryForObject("""
            INSERT INTO products (category_id, name, slug, description, brand, sku, price, mrp, image_url, active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
            ON CONFLICT (sku) DO UPDATE SET
              category_id = EXCLUDED.category_id,
              name = EXCLUDED.name,
              slug = EXCLUDED.slug,
              description = EXCLUDED.description,
              brand = EXCLUDED.brand,
              price = EXCLUDED.price,
              mrp = EXCLUDED.mrp,
              image_url = EXCLUDED.image_url,
              active = TRUE,
              updated_at = CURRENT_TIMESTAMP
            RETURNING id
            """,
        Long.class,
        categoryId,
        product.title(),
        uniqueSlug(product.title(), product.id()),
        product.description(),
        product.brand() == null || product.brand().isBlank() ? "Generic" : product.brand(),
        "DUMMYJSON-" + product.id(),
        price,
        mrp,
        product.thumbnail());

    jdbcTemplate.update("""
            INSERT INTO product_ratings (product_id, average_rating, rating_count)
            VALUES (?, ?, 1)
            ON CONFLICT (product_id) DO UPDATE SET
              average_rating = EXCLUDED.average_rating,
              updated_at = CURRENT_TIMESTAMP
            """,
        productId,
        BigDecimal.valueOf(product.rating()).setScale(2, RoundingMode.HALF_UP));

    jdbcTemplate.update("""
            INSERT INTO inventory (product_id, quantity_on_hand, reserved_quantity, reorder_level, warehouse_location)
            VALUES (?, ?, 0, 5, 'external-api')
            ON CONFLICT (product_id) DO UPDATE SET
              quantity_on_hand = EXCLUDED.quantity_on_hand,
              updated_at = CURRENT_TIMESTAMP
            """,
        productId,
        Math.max(product.stock(), 0));
  }

  private Long upsertCategory(String category) {
    String name = category == null || category.isBlank() ? "Uncategorized" : titleCase(category.replace("-", " "));
    return jdbcTemplate.queryForObject("""
            INSERT INTO categories (name, slug, description)
            VALUES (?, ?, ?)
            ON CONFLICT (slug) DO UPDATE SET
              name = EXCLUDED.name,
              description = EXCLUDED.description
            RETURNING id
            """,
        Long.class,
        name,
        slug(name),
        "Imported from external product API");
  }

  private String uniqueSlug(String text, long id) {
    return slug(text) + "-" + id;
  }

  private String slug(String text) {
    String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
    return normalized.isBlank() ? "item" : normalized;
  }

  private String titleCase(String text) {
    String[] words = text.split("\\s+");
    StringBuilder builder = new StringBuilder();
    for (String word : words) {
      if (word.isBlank()) continue;
      if (!builder.isEmpty()) builder.append(' ');
      builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT));
      if (word.length() > 1) {
        builder.append(word.substring(1).toLowerCase(Locale.ROOT));
      }
    }
    return builder.toString();
  }

  record DummyJsonCatalog(List<DummyJsonProduct> products) {
  }

  record DummyJsonProduct(
      long id,
      String title,
      String description,
      String category,
      double price,
      double discountPercentage,
      double rating,
      int stock,
      String brand,
      String thumbnail) {
  }
}
