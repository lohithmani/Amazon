import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './app';
import AppProvider from './provider/AppProvider';
import './styles.css';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AppProvider>
      <App />
    </AppProvider>
  </React.StrictMode>
);
