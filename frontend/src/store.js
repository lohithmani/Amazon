import { configureStore, createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { api, setApiToken } from './services/api';
import { mapApiProduct } from './utils/catalog';

const storageKey = 'amazon-custom-frontend';

function normalizeUser(user = {}) {
  return {
    fullName: user.fullName || user.name || 'Account',
    email: user.email || '',
    roles: Array.isArray(user.roles) && user.roles.length > 0 ? user.roles : [user.role || 'CUSTOMER']
  };
}

function createInitialAuthState(auth) {
  if (!auth) {
    return {
      user: null,
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      tokenType: 'Bearer',
      status: 'idle',
      error: null
    };
  }

  return {
    user: auth.user ? normalizeUser(auth.user) : null,
    isAuthenticated: Boolean(auth.isAuthenticated || auth.accessToken),
    accessToken: auth.accessToken || null,
    refreshToken: auth.refreshToken || null,
    tokenType: auth.tokenType || 'Bearer',
    status: 'idle',
    error: null
  };
}

function loadState() {
  if (typeof window === 'undefined') return undefined;

  try {
    const raw = window.localStorage.getItem(storageKey);
    return raw ? JSON.parse(raw) : undefined;
  } catch {
    return undefined;
  }
}

function saveState(state) {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(storageKey, JSON.stringify(state));
}

function extractErrorMessage(error, fallback) {
  const responseData = error && error.response ? error.response.data : null;
  return (
    (responseData && (responseData.message || responseData.detail || responseData.error)) ||
    error.message ||
    fallback
  );
}

const persisted = loadState();
const preloadedState = persisted
  ? {
      auth: createInitialAuthState(persisted.auth),
      cart: persisted.cart || {
        items: []
      },
      orders: persisted.orders || {
        items: [],
        status: 'idle',
        error: null
      }
    }
  : undefined;
setApiToken(preloadedState ? preloadedState.auth.accessToken : null);

export const fetchCatalog = createAsyncThunk('catalog/fetchCatalog', async () => {
  const { data } = await api.get('/products');
  return (data || []).map(mapApiProduct);
});

export const registerUser = createAsyncThunk('auth/registerUser', async (payload, { rejectWithValue }) => {
  try {
    const { data } = await api.post('/auth/register', {
      email: payload.email,
      fullName: payload.fullName,
      password: payload.password
    });
    setApiToken(data.accessToken);
    return data;
  } catch (error) {
    return rejectWithValue(extractErrorMessage(error, 'Unable to register account'));
  }
});

export const loginUser = createAsyncThunk('auth/loginUser', async (payload, { rejectWithValue }) => {
  try {
    const { data } = await api.post('/auth/login', {
      email: payload.email,
      password: payload.password
    });
    setApiToken(data.accessToken);
    return data;
  } catch (error) {
    return rejectWithValue(extractErrorMessage(error, 'Unable to sign in'));
  }
});

export const fetchOrders = createAsyncThunk('orders/fetchOrders', async (_, { rejectWithValue }) => {
  try {
    const { data } = await api.get('/orders');
    return data || [];
  } catch (error) {
    return rejectWithValue(extractErrorMessage(error, 'Unable to load orders'));
  }
});

export const createOrder = createAsyncThunk('orders/createOrder', async (items, { rejectWithValue }) => {
  try {
    const { data } = await api.post('/orders', {
      items: items.map((item) => ({
        productId: item.id,
        quantity: item.quantity
      }))
    });
    return data;
  } catch (error) {
    return rejectWithValue(extractErrorMessage(error, 'Unable to place order'));
  }
});

const catalogSlice = createSlice({
  name: 'catalog',
  initialState: {
    items: [],
    status: 'idle',
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchCatalog.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchCatalog.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload;
      })
      .addCase(fetchCatalog.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message || 'Unable to load catalog';
      });
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: preloadedState ? preloadedState.auth : createInitialAuthState(),
  reducers: {
    signOut() {
      return createInitialAuthState();
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(registerUser.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.user = normalizeUser(action.payload);
        state.isAuthenticated = true;
        state.accessToken = action.payload.accessToken || null;
        state.refreshToken = action.payload.refreshToken || null;
        state.tokenType = action.payload.tokenType || 'Bearer';
        state.status = 'succeeded';
        state.error = null;
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload || action.error.message || 'Unable to register account';
      })
      .addCase(loginUser.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.user = normalizeUser(action.payload);
        state.isAuthenticated = true;
        state.accessToken = action.payload.accessToken || null;
        state.refreshToken = action.payload.refreshToken || null;
        state.tokenType = action.payload.tokenType || 'Bearer';
        state.status = 'succeeded';
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload || action.error.message || 'Unable to sign in';
      });
  }
});

const cartSlice = createSlice({
  name: 'cart',
  initialState: preloadedState ? preloadedState.cart : {
    items: []
  },
  reducers: {
    addItem(state, action) {
      const existing = state.items.find((item) => item.id === action.payload.id);
      if (existing) {
        existing.quantity += 1;
        return;
      }
      state.items.push({ ...action.payload, quantity: 1 });
    },
    removeItem(state, action) {
      state.items = state.items.filter((item) => item.id !== action.payload);
    },
    decrementItem(state, action) {
      const existing = state.items.find((item) => item.id === action.payload);
      if (!existing) return;
      existing.quantity -= 1;
      state.items = state.items.filter((item) => item.quantity > 0);
    },
    clearCart(state) {
      state.items = [];
    }
  }
});

const ordersSlice = createSlice({
  name: 'orders',
  initialState: preloadedState ? preloadedState.orders : {
    items: [],
    status: 'idle',
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrders.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items = action.payload;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload || action.error.message || 'Unable to load orders';
      })
      .addCase(createOrder.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(createOrder.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.items.unshift(action.payload);
      })
      .addCase(createOrder.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload || action.error.message || 'Unable to place order';
      });
  }
});

const uiSlice = createSlice({
  name: 'ui',
  initialState: {
    query: '',
    category: 'All',
    brand: 'All',
    sortBy: 'featured',
    minRating: 0,
    priceCap: 5000
  },
  reducers: {
    setQuery(state, action) {
      state.query = action.payload;
    },
    setCategory(state, action) {
      state.category = action.payload;
      state.brand = 'All';
    },
    setBrand(state, action) {
      state.brand = action.payload;
    },
    setSortBy(state, action) {
      state.sortBy = action.payload;
    },
    setMinRating(state, action) {
      state.minRating = action.payload;
    },
    setPriceCap(state, action) {
      state.priceCap = action.payload;
    }
  }
});

export const { signOut } = authSlice.actions;
export const { addItem, removeItem, decrementItem, clearCart } = cartSlice.actions;
export const { setQuery, setCategory, setBrand, setSortBy, setMinRating, setPriceCap } = uiSlice.actions;

export const store = configureStore({
  reducer: {
    auth: authSlice.reducer,
    cart: cartSlice.reducer,
    catalog: catalogSlice.reducer,
    orders: ordersSlice.reducer,
    ui: uiSlice.reducer
  },
  preloadedState
});

store.subscribe(() => {
  const state = store.getState();
  saveState({
    auth: state.auth,
    cart: state.cart,
    orders: state.orders
  });
});

export const selectCartCount = (state) =>
  state.cart.items.reduce((total, item) => total + item.quantity, 0);

export const selectCartTotal = (state) =>
  state.cart.items.reduce((total, item) => total + item.price * item.quantity, 0);
