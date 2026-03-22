import { StrictMode, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import router from './router/AppRouter';
import useThemeStore from './context/themeStore';
import './index.css';

// Theme initializer component
const ThemeInitializer = ({ children }) => {
  const { initTheme } = useThemeStore();

  useEffect(() => {
    initTheme();
  }, [initTheme]);

  return children;
};

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeInitializer>
      <RouterProvider router={router} />
      <Toaster
      position="bottom-right"
      toastOptions={{
        duration: 4000,
        style: {
          background: 'var(--bg-elevated)',
          color: 'var(--text-primary)',
          border: '1px solid var(--bg-border)',
        },
        success: {
          iconTheme: {
            primary: 'var(--green)',
            secondary: 'white',
          },
        },
        error: {
          iconTheme: {
            primary: 'var(--red)',
            secondary: 'white',
          },
        },
      }}
      />
    </ThemeInitializer>
  </StrictMode>
);
