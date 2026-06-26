export function mapApiProduct(product, index = 0) {
  return {
    id: product.id,
    name: product.name,
    category: product.category,
    brand: product.brand || 'Generic',
    price: Math.round(product.price),
    rating: Number(product.rating || 0),
    stock: Number(product.stock || 0),
    badge: product.badge || getProductBadge(product, index),
    description: product.description,
    image: product.image || '',
    images: []
  };
}

export function getProductBadge(product, index = 0) {
  if (product.discountPercentage >= 25) return 'Deal';
  if (product.rating >= 4.8) return 'Top rated';
  if (product.stock <= 10) return 'Limited stock';
  if (index < 6) return 'Trending';
  return 'Popular';
}

export function deriveCategories(products) {
  const uniqueCategories = Array.from(new Set(products.map((product) => product.category).filter(Boolean)));
  return ['All', ...uniqueCategories];
}

export function deriveBrands(products) {
  return ['All', ...Array.from(new Set(products.map((product) => product.brand).filter(Boolean)))];
}

export function sortProducts(products, sortBy) {
  const sorted = [...products];

  if (sortBy === 'price-asc') {
    return sorted.sort((left, right) => left.price - right.price);
  }

  if (sortBy === 'price-desc') {
    return sorted.sort((left, right) => right.price - left.price);
  }

  if (sortBy === 'rating-desc') {
    return sorted.sort((left, right) => right.rating - left.rating);
  }

  return sorted;
}
