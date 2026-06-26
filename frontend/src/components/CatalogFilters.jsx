export default function CatalogFilters({
  categories,
  brands,
  category,
  brand,
  sortBy,
  ratingFloor,
  priceCap,
  onCategoryChange,
  onBrandChange,
  onSortChange,
  onRatingChange,
  onPriceCapChange
}) {
  return (
    <div className="catalog-filters">
      <label className="field">
        <span>Category</span>
        <select className="input" value={category} onChange={(event) => onCategoryChange(event.target.value)}>
          {categories.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label className="field">
        <span>Brand</span>
        <select className="input" value={brand} onChange={(event) => onBrandChange(event.target.value)}>
          {brands.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label className="field">
        <span>Sort</span>
        <select className="input" value={sortBy} onChange={(event) => onSortChange(event.target.value)}>
          <option value="featured">Featured</option>
          <option value="price-asc">Price: low to high</option>
          <option value="price-desc">Price: high to low</option>
          <option value="rating-desc">Rating: high to low</option>
        </select>
      </label>

      <label className="field">
        <span>Min rating</span>
        <select className="input" value={ratingFloor} onChange={(event) => onRatingChange(Number(event.target.value))}>
          <option value="0">Any</option>
          <option value="4">4.0+</option>
          <option value="4.5">4.5+</option>
          <option value="4.8">4.8+</option>
        </select>
      </label>

      <label className="field">
        <span>Price cap: ${priceCap}</span>
        <input
          className="range"
          type="range"
          min="50"
          max="5000"
          step="50"
          value={priceCap}
          onChange={(event) => onPriceCapChange(Number(event.target.value))}
        />
      </label>
    </div>
  );
}
