import test from 'node:test';
import assert from 'node:assert/strict';
import { formatCurrency } from '../src/utils/money.js';
import { deriveBrands, deriveCategories, getProductBadge, mapApiProduct, sortProducts } from '../src/utils/catalog.js';

test('formatCurrency formats USD values', () => {
  assert.equal(formatCurrency(1299), '$1,299');
});

test('deriveCategories returns unique category names with All first', () => {
  const categories = deriveCategories([
    { category: 'Home' },
    { category: 'Electronics' },
    { category: 'Home' }
  ]);

  assert.deepEqual(categories, ['All', 'Home', 'Electronics']);
});

test('getProductBadge prioritizes deal and stock signals', () => {
  assert.equal(getProductBadge({ discountPercentage: 30, rating: 4.9, stock: 2 }, 0), 'Deal');
  assert.equal(getProductBadge({ discountPercentage: 0, rating: 4.9, stock: 2 }, 0), 'Top rated');
  assert.equal(getProductBadge({ discountPercentage: 0, rating: 4.1, stock: 2 }, 0), 'Limited stock');
});

test('mapApiProduct normalizes backend product payloads', () => {
  const product = mapApiProduct(
    {
      id: 101,
      name: 'Studio Headphones',
      category: 'Audio',
      brand: 'SoundLab',
      price: 99.4,
      rating: 4.7,
      stock: 18,
      description: 'Premium sound',
      image: 'https://example.com/headphones.png'
    },
    0
  );

  assert.equal(product.id, 101);
  assert.equal(product.name, 'Studio Headphones');
  assert.equal(product.price, 99);
  assert.equal(product.image, 'https://example.com/headphones.png');
  assert.deepEqual(product.images, []);
});

test('deriveBrands returns unique brand names with All first', () => {
  const brands = deriveBrands([{ brand: 'Alpha' }, { brand: 'Beta' }, { brand: 'Alpha' }]);

  assert.deepEqual(brands, ['All', 'Alpha', 'Beta']);
});

test('sortProducts orders products by price and rating', () => {
  const products = [
    { price: 30, rating: 4.2 },
    { price: 10, rating: 4.9 },
    { price: 20, rating: 4.5 }
  ];

  assert.deepEqual(sortProducts(products, 'price-asc').map((item) => item.price), [10, 20, 30]);
  assert.deepEqual(sortProducts(products, 'rating-desc').map((item) => item.rating), [4.9, 4.5, 4.2]);
});

