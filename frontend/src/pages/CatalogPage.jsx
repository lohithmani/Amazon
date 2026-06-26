import { useMemo } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setBrand, setCategory, setMinRating, setPriceCap, setSortBy } from '../store';
import ProductCard from '../components/ProductCard';
import Reveal from '../components/Reveal';
import SectionHeader from '../components/SectionHeader';
import CatalogFilters from '../components/CatalogFilters';
import { deriveBrands, deriveCategories, sortProducts } from '../utils/catalog';

export default function CatalogPage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector((state) => state.catalog);
  const query = useSelector((state) => state.ui.query);
  const category = useSelector((state) => state.ui.category);
  const brand = useSelector((state) => state.ui.brand);
  const sortBy = useSelector((state) => state.ui.sortBy);
  const minRating = useSelector((state) => state.ui.minRating);
  const priceCap = useSelector((state) => state.ui.priceCap);
  const categories = useMemo(() => deriveCategories(items), [items]);
  const brands = useMemo(() => deriveBrands(items), [items]);

  const products = useMemo(() => {
    const filtered = items.filter((item) => {
      const matchesCategory = category === 'All' || item.category === category;
      const matchesBrand = brand === 'All' || item.brand === brand;
      const matchesRating = item.rating >= minRating;
      const matchesPrice = item.price <= priceCap;
      const search = query.trim().toLowerCase();
      const matchesQuery =
        search.length === 0 ||
        item.name.toLowerCase().includes(search) ||
        item.description.toLowerCase().includes(search);
      return matchesCategory && matchesBrand && matchesRating && matchesPrice && matchesQuery;
    });
    return sortProducts(filtered, sortBy);
  }, [brand, category, items, minRating, priceCap, query, sortBy]);

  return (
    <section className="stack">
      <Reveal className="card">
        <SectionHeader
          eyebrow="Catalog"
          title="Browse the product wall"
          description={`${products.length} products matched your filters.`}
        />

        <CatalogFilters
          categories={categories}
          brands={brands}
          category={category}
          brand={brand}
          sortBy={sortBy}
          ratingFloor={minRating}
          priceCap={priceCap}
          onCategoryChange={(value) => dispatch(setCategory(value))}
          onBrandChange={(value) => dispatch(setBrand(value))}
          onSortChange={(value) => dispatch(setSortBy(value))}
          onRatingChange={(value) => dispatch(setMinRating(value))}
          onPriceCapChange={(value) => dispatch(setPriceCap(value))}
        />
      </Reveal>

      {status === 'loading' ? <div className="card">Loading catalog...</div> : null}
      {error ? <div className="card error">{error}</div> : null}

      <section className="product-grid">
        {products.map((product, index) => (
          <ProductCard key={product.id} product={product} delay={index * 40} />
        ))}
      </section>
    </section>
  );
}
