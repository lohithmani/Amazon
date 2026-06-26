import { NavLink, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Outlet } from 'react-router-dom';
import { selectCartCount, setQuery, signOut } from '../store';
import { setApiToken } from '../services/api';
import { useBootstrapCatalog } from '../effects/useBootstrapCatalog';

const navItems = [
  { to: '/', label: 'Home' },
  { to: '/catalog', label: 'Catalog' },
  { to: '/orders', label: 'Orders' },
  { to: '/account', label: 'Account' }
];

export default function MarketplaceLayout() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const cartCount = useSelector(selectCartCount);
  const query = useSelector((state) => state.ui.query);
  const auth = useSelector((state) => state.auth);

  useBootstrapCatalog();

  const handleSearch = (event) => {
    event.preventDefault();
    navigate('/catalog');
  };

  const handleSignOut = () => {
    setApiToken(null);
    dispatch(signOut());
    navigate('/');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to="/" className="brand">
          <span className="brand-mark">Amazon Pro</span>
          <span className="brand-subtitle">Retail workspace</span>
        </NavLink>

        <form className="search-bar" onSubmit={handleSearch}>
          <label className="sr-only" htmlFor="store-search">
            Search products
          </label>
          <input
            id="store-search"
            className="input search-input"
            placeholder="Search products, brands, categories..."
            value={query}
            onChange={(event) => dispatch(setQuery(event.target.value))}
          />
          <button type="submit" className="button search-button">
            Search
          </button>
        </form>

        <div className="topbar-actions">
          <div className="account-chip">
            <span>{auth.isAuthenticated ? 'Signed in as' : 'Account'}</span>
            <strong>{auth.user ? auth.user.fullName : 'Secure access'}</strong>
          </div>
          {auth.isAuthenticated ? (
            <button type="button" className="button secondary" onClick={handleSignOut}>
              Sign out
            </button>
          ) : (
            <NavLink to="/account" className="button secondary">
              Sign in / Register
            </NavLink>
          )}
          <NavLink to="/orders" className="button secondary">
            Orders
          </NavLink>
          <NavLink to="/cart" className="cart-chip">
            Cart <strong>{cartCount}</strong>
          </NavLink>
        </div>
      </header>

      <nav className="nav">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
