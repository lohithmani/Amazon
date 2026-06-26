import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { addItem } from '../store';
import { formatCurrency } from '../utils/money';
import Reveal from './Reveal';

export default function ProductCard({ product, compact = false, delay = 0 }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleAddToCart = () => {
    dispatch(addItem(product));
  };

  const handleBuyNow = () => {
    dispatch(addItem(product));
    navigate('/cart');
  };

  return (
    <Reveal delay={delay} className={compact ? 'product-card compact' : 'card product-card'}>
      {product.image ? (
        <div className="product-image-wrap">
          <img className="product-image" src={product.image} alt={product.name} loading="lazy" />
        </div>
      ) : null}
      <div className="product-meta">
        <span className="badge">{product.badge}</span>
        <span>{product.category}</span>
      </div>
      <h3>{product.name}</h3>
      <p>{product.description}</p>
      <div className="product-footer">
        <strong>{formatCurrency(product.price)}</strong>
        <span>★ {product.rating}</span>
      </div>
      <div className="mini-meta">{product.stock} in stock</div>
      <div className="button-row">
        <button type="button" className="button primary" onClick={handleAddToCart}>
          Add to cart
        </button>
        <button type="button" className="button secondary" onClick={handleBuyNow}>
          Buy now
        </button>
      </div>
    </Reveal>
  );
}
