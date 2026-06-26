import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { useSelector } from 'react-redux';
import Reveal from '../components/Reveal';
import SectionHeader from '../components/SectionHeader';
import { formatCurrency } from '../utils/money';
import { fetchOrders } from '../store';

export default function OrdersPage() {
  const dispatch = useDispatch();
  const orders = useSelector((state) => state.orders.items);
  const orderStatus = useSelector((state) => state.orders.status);
  const orderError = useSelector((state) => state.orders.error);
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchOrders());
    }
  }, [dispatch, isAuthenticated]);

  return (
    <section className="stack">
      <Reveal className="card">
        <SectionHeader eyebrow="History" title="Recent orders" description="Track your completed purchases and live order statuses." />
      </Reveal>

      {!isAuthenticated ? (
        <Reveal className="card empty-state">
          <h3>Sign in to view orders</h3>
          <p>Your order history loads from the backend after authentication.</p>
        </Reveal>
      ) : null}

      {orderStatus === 'loading' ? <div className="card">Loading orders...</div> : null}
      {orderError ? <div className="card error">{orderError}</div> : null}

      {isAuthenticated && orders.length === 0 && orderStatus !== 'loading' ? (
        <Reveal className="card empty-state">
          <h3>No orders yet</h3>
          <p>Place an order to see it appear here.</p>
        </Reveal>
      ) : null}

      {orders.map((order, index) => (
        <Reveal key={order.id} delay={index * 30} className="card order-card">
          <div className="order-top">
            <strong>{order.number}</strong>
            <span>{order.status}</span>
          </div>
          <div className="summary-row">
            <span>Placed</span>
            <span>{order.placedAt ? new Date(order.placedAt).toLocaleString() : '-'}</span>
          </div>
          <div className="summary-row">
            <span>Items</span>
            <span>{order.items}</span>
          </div>
          <div className="summary-row">
            <span>Total</span>
            <span>{formatCurrency(order.total)}</span>
          </div>
        </Reveal>
      ))}
    </section>
  );
}
