import { NavLink } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { addItem, clearCart, createOrder, decrementItem, removeItem, selectCartTotal } from '../store';
import { formatCurrency } from '../utils/money';
import Reveal from '../components/Reveal';
import SectionHeader from '../components/SectionHeader';
import { useState } from 'react';

export default function CartPage() {
  const dispatch = useDispatch();
  const items = useSelector((state) => state.cart.items);
  const total = useSelector(selectCartTotal);
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);
  const orderError = useSelector((state) => state.orders.error);
  const [status, setStatus] = useState('idle');

  const itemCount = items.reduce((count, item) => count + item.quantity, 0);
  const subtotal = total;

  const checkout = async () => {
    if (!items.length || !isAuthenticated) return;
    setStatus('processing');
    try {
      await dispatch(createOrder(items)).unwrap();
      dispatch(clearCart());
      setStatus('completed');
    } catch {
      setStatus('idle');
    }
  };

  return (
    <section className="cart-layout">
      <section className="stack">
        {items.length === 0 ? (
          <Reveal className="card empty-state">
            <h3>Your cart is empty</h3>
            <p>Add products from the catalog to start checkout.</p>
            <NavLink to="/catalog" className="button primary">
              Browse catalog
            </NavLink>
          </Reveal>
        ) : (
          items.map((item, index) => (
            <Reveal key={item.id} delay={index * 30} className="card cart-item">
              <div>
                <h3>{item.name}</h3>
                <p>{formatCurrency(item.price)} each</p>
              </div>
              <div className="quantity-controls">
                <button type="button" className="chip" onClick={() => dispatch(decrementItem(item.id))}>
                  -
                </button>
                <span>{item.quantity}</span>
                <button type="button" className="chip" onClick={() => dispatch(addItem(item))}>
                  +
                </button>
              </div>
              <div className="cart-item-actions">
                <strong>{formatCurrency(item.price * item.quantity)}</strong>
                <button type="button" className="link-button" onClick={() => dispatch(removeItem(item.id))}>
                  Remove
                </button>
              </div>
            </Reveal>
          ))
        )}
      </section>

      <Reveal className="card summary">
        <SectionHeader eyebrow="Checkout" title="Order summary" />
        <div className="summary-row">
          <span>Items</span>
          <span>{itemCount}</span>
        </div>
        <div className="summary-row">
          <span>Subtotal</span>
          <span>{formatCurrency(subtotal)}</span>
        </div>
        <div className="summary-row">
          <span>Total</span>
          <span>{formatCurrency(subtotal)}</span>
        </div>
        {!isAuthenticated ? <p className="error">Sign in before checkout.</p> : null}
        {orderError ? <p className="error">{orderError}</p> : null}
        <button type="button" className="button primary full" onClick={checkout} disabled={!items.length || !isAuthenticated || status === 'processing'}>
          {status === 'processing' ? 'Placing order...' : 'Checkout'}
        </button>
        {status === 'completed' ? <p className="success">Order placed successfully.</p> : null}
      </Reveal>
    </section>
  );
}
