import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { useAuthStore } from './store/authStore';
import { LoginForm, RegisterForm } from './components/auth/AuthForms';
import EventsPage from './pages/EventsPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: 1,
    },
  },
});

// ── Route Guards ──────────────────────────────────────────────

function PrivateRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function GuestRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  return !isAuthenticated ? children : <Navigate to="/events" replace />;
}

// ── App ───────────────────────────────────────────────────────

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/" element={<Navigate to="/events" replace />} />
          <Route path="/login"    element={<GuestRoute><LoginForm /></GuestRoute>} />
          <Route path="/register" element={<GuestRoute><RegisterForm /></GuestRoute>} />

          {/* Events — public browse, private registration */}
          <Route path="/events"     element={<EventsPage />} />
          <Route path="/events/:id" element={<EventsPage />} />

          {/* Protected */}
          <Route path="/events/create" element={<PrivateRoute><div>Create Event (build out)</div></PrivateRoute>} />
          <Route path="/dashboard"     element={<PrivateRoute><div>Dashboard (build out)</div></PrivateRoute>} />
          <Route path="/profile"       element={<PrivateRoute><div>Profile (build out)</div></PrivateRoute>} />

          {/* 404 */}
          <Route path="*" element={<div className="min-h-screen flex items-center justify-center text-gray-500">Page not found</div>} />
        </Routes>
      </BrowserRouter>

      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: { borderRadius: '10px', fontSize: '14px' },
          success: { iconTheme: { primary: '#4F46E5', secondary: '#fff' } },
        }}
      />
    </QueryClientProvider>
  );
}
