import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useThemeStore = create(
  persist(
    (set, get) => ({
      theme: 'dark',

      toggleTheme: () => {
        const newTheme = get().theme === 'dark' ? 'light' : 'dark';
        // Apply theme to document
        if (newTheme === 'light') {
          document.documentElement.classList.add('light');
        } else {
          document.documentElement.classList.remove('light');
        }
        set({ theme: newTheme });
      },

      setTheme: (theme) => {
        if (theme === 'light') {
          document.documentElement.classList.add('light');
        } else {
          document.documentElement.classList.remove('light');
        }
        set({ theme });
      },

      // Initialize theme on app load
      initTheme: () => {
        const currentTheme = get().theme;
        if (currentTheme === 'light') {
          document.documentElement.classList.add('light');
        } else {
          document.documentElement.classList.remove('light');
        }
      },
    }),
    {
      name: 'ecova-theme',
      onRehydrateStorage: () => (state) => {
        // Apply theme when storage is rehydrated
        if (state?.theme === 'light') {
          document.documentElement.classList.add('light');
        } else {
          document.documentElement.classList.remove('light');
        }
      },
    }
  )
);

export default useThemeStore;
