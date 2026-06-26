import { NavLink as RouterLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import ProductCard from '../components/ProductCard';
import Reveal from '../components/Reveal';
import SectionHeader from '../components/SectionHeader';
import { deriveCategories } from '../utils/catalog';

export default function HomePage() {
  const featuredProducts = useSelector((state) => state.catalog.items.slice(0, 4));
  const catalogStatus = useSelector((state) => state.catalog.status);
  const categories = deriveCategories(featuredProducts).slice(1, 5);
  const stats = [
    { label: 'Products ready', value: `${featuredProducts.length || 0}+` },
    { label: 'Fresh categories', value: `${categories.length}+` },
    { label: 'Secure login', value: 'On' }
  ];

  return (
    <section className="stack">
      <Reveal className="hero card">
        <div className="hero-copy">
          <span className="eyebrow">Professional storefront</span>
          <h1>Shop products with a fast, polished commerce experience.</h1>
          <p>
            Browse live products, manage cart items, and use the secure account flow.
          </p>
          <div className="hero-actions">
            <RouterLink to="/catalog" className="button primary">
              Shop now
            </RouterLink>
            <RouterLink to="/account" className="button secondary">
              Create account
            </RouterLink>
          </div>
        </div>

        <div className="hero-panel">
          {stats.map((stat) => (
            <div key={stat.label} className="stat-card">
              <strong>{stat.value}</strong>
              <span>{stat.label}</span>
            </div>
          ))}
        </div>
      </Reveal>

      <section className="grid-two">
        <Reveal className="card" delay={60}>
          <SectionHeader
            eyebrow="Benefits"
            title="Why this storefront feels production-ready"
            description="Reusable UI pieces, cleaner state, and a catalog driven by a live API."
          />
          <div className="benefit-list">
            <div>
              <strong>Search-first shopping</strong>
              <p>Search and category filters stay in sync across the app shell.</p>
            </div>
            <div>
              <strong>Fast checkout</strong>
              <p>Add to cart or buy now with a simplified, reusable product card.</p>
            </div>
            <div>
              <strong>Secure account access</strong>
              <p>Register or sign in through the auth service with clean session handling.</p>
            </div>
          </div>
        </Reveal>

        <Reveal className="card" delay={120}>
          <SectionHeader eyebrow="Categories" title="Popular departments" description="Browse the most active product groups from the live catalog." />
          <div className="chip-grid">
            {categories.map((category) => (
              <span key={category} className="category-card">
                {category}
              </span>
            ))}
          </div>
        </Reveal>
      </section>

      <Reveal className="card" delay={180}>
        <SectionHeader
          eyebrow="Featured deals"
          title="Trending products"
          action={<span className="subtle-pill">{catalogStatus === 'loading' ? 'Loading catalog...' : 'Live catalog'}</span>}
        />

        <div className="featured-grid">
          {featuredProducts.map((product, index) => (
            <ProductCard key={product.id} product={product} compact delay={index * 40} />
          ))}
        </div>
      </Reveal>
    </section>
  );
}
