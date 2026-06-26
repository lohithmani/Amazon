import { Route, Routes } from 'react-router-dom';
import MarketplaceLayout from '../components/MarketplaceLayout';
import AccountPage from '../pages/AccountPage';
import CartPage from '../pages/CartPage';
import CatalogPage from '../pages/CatalogPage';
import HomePage from '../pages/HomePage';
import OrdersPage from '../pages/OrdersPage';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<MarketplaceLayout />}>
        <Route index element={<HomePage />} />
        <Route path="catalog" element={<CatalogPage />} />
        <Route path="cart" element={<CartPage />} />
        <Route path="orders" element={<OrdersPage />} />
        <Route path="account" element={<AccountPage />} />
      </Route>
    </Routes>
  );
}
