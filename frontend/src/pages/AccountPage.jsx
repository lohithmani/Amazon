import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { NavLink, useNavigate } from 'react-router-dom';
import { loginUser, registerUser, signOut } from '../store';
import { setApiToken } from '../services/api';
import Reveal from '../components/Reveal';
import SectionHeader from '../components/SectionHeader';

export default function AccountPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const auth = useSelector((state) => state.auth);
  const [mode, setMode] = useState(auth.isAuthenticated ? 'profile' : 'login');
  const [loginForm, setLoginForm] = useState({
    email: '',
    password: ''
  });
  const [registerForm, setRegisterForm] = useState({
    fullName: '',
    email: '',
    password: ''
  });

  useEffect(() => {
    if (auth.isAuthenticated) {
      setMode('profile');
      return;
    }

    setMode('login');
  }, [auth.isAuthenticated]);

  const handleLogin = async (event) => {
    event.preventDefault();
    try {
      await dispatch(loginUser(loginForm)).unwrap();
    } catch {
      // Redux state surfaces the error banner.
    }
  };

  const handleRegister = async (event) => {
    event.preventDefault();
    try {
      await dispatch(registerUser(registerForm)).unwrap();
      setRegisterForm({ fullName: '', email: '', password: '' });
    } catch {
      // Redux state surfaces the error banner.
    }
  };

  const handleSignOut = () => {
    setApiToken(null);
    dispatch(signOut());
    navigate('/');
  };

  if (auth.isAuthenticated) {
    return (
      <section className="grid-two">
        <Reveal className="card stack">
          <SectionHeader eyebrow="Account" title={auth.user ? auth.user.fullName : 'Account'} description="Signed-in profile details and session state." />
          <div className="summary-row">
            <span>Email</span>
            <span>{auth.user ? auth.user.email : '-'}</span>
          </div>
          <div className="summary-row">
            <span>Roles</span>
            <span>{auth.user ? auth.user.roles.join(', ') : '-'}</span>
          </div>
          <div className="summary-row">
            <span>Token type</span>
            <span>{auth.tokenType}</span>
          </div>
          <button type="button" className="button primary" onClick={handleSignOut}>
            Sign out
          </button>
        </Reveal>

        <Reveal className="card stack" delay={80}>
          <SectionHeader
            eyebrow="Shopping shortcuts"
            title="Continue shopping"
            description="Use the catalog to search products, then add items to cart and complete checkout."
          />
          <div className="hero-actions">
            <NavLink to="/catalog" className="button secondary">
              Browse catalog
            </NavLink>
            <NavLink to="/orders" className="button secondary">
              View orders
            </NavLink>
          </div>
        </Reveal>
      </section>
    );
  }

  return (
    <section className="auth-layout">
      <Reveal className="card auth-hero">
        <SectionHeader
          eyebrow="Customer access"
          title="Sign in or create your store account"
          description="Use the auth service behind the gateway to create or access a customer session."
        />
      </Reveal>

      <Reveal className="card stack" delay={80}>
        <div className="tab-row">
          <button type="button" className={mode === 'login' ? 'tab active' : 'tab'} onClick={() => setMode('login')}>
            Sign in
          </button>
          <button type="button" className={mode === 'register' ? 'tab active' : 'tab'} onClick={() => setMode('register')}>
            Register
          </button>
        </div>

        {auth.error ? <div className="banner error">{auth.error}</div> : null}

        {mode === 'login' ? (
          <form className="stack" onSubmit={handleLogin}>
            <label className="field">
              <span>Email</span>
              <input
                className="input"
                type="email"
                value={loginForm.email}
                onChange={(event) => setLoginForm({ ...loginForm, email: event.target.value })}
              />
            </label>
            <label className="field">
              <span>Password</span>
              <input
                className="input"
                type="password"
                value={loginForm.password}
                onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
              />
            </label>
            <button type="submit" className="button primary" disabled={auth.status === 'loading'}>
              {auth.status === 'loading' ? 'Signing in...' : 'Sign in'}
            </button>
          </form>
        ) : (
          <form className="stack" onSubmit={handleRegister}>
            <label className="field">
              <span>Full name</span>
              <input
                className="input"
                value={registerForm.fullName}
                onChange={(event) => setRegisterForm({ ...registerForm, fullName: event.target.value })}
              />
            </label>
            <label className="field">
              <span>Email</span>
              <input
                className="input"
                type="email"
                value={registerForm.email}
                onChange={(event) => setRegisterForm({ ...registerForm, email: event.target.value })}
              />
            </label>
            <label className="field">
              <span>Password</span>
              <input
                className="input"
                type="password"
                value={registerForm.password}
                onChange={(event) => setRegisterForm({ ...registerForm, password: event.target.value })}
              />
            </label>
            <button type="submit" className="button primary" disabled={auth.status === 'loading'}>
              {auth.status === 'loading' ? 'Creating account...' : 'Register'}
            </button>
          </form>
        )}
      </Reveal>
    </section>
  );
}
