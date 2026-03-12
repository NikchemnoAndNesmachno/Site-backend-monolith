import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import { useAuth } from './context/AuthContext';
import type {JSX} from "react";

function HomePage() {
    const { user, logout, isInitializing } = useAuth();

    if (isInitializing) {
        return <div>Loading auth...</div>;
    }

    return (
        <div style={{ padding: 24 }}>
            <h1>Home</h1>
            {user ? (
                <>
                    <p>User ID: {user.id}</p>
                    {/*<p>Email: {user.email}</p>*/}
                    <p>Role: {user.role}</p>
                    <button onClick={() => void logout()}>Logout</button>
                </>
            ) : (
                <p>Guest</p>
            )}
        </div>
    );
}

function ProtectedRoute({ children }: { children: JSX.Element }) {
    const { isAuthenticated, isInitializing } = useAuth();

    if (isInitializing) {
        return <div>Loading auth...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
        </Routes>
    );
}