import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import type {JSX} from "react";
import useAuth from "./hooks/useAuth.ts";
import {HomePage} from "./pages/HomePage.tsx";
import {VideoPage} from "./pages/VideoPage.tsx";
import {ProfilePage} from "./pages/ProfilePage.tsx";

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

function PublicOnlyRoute({ children }: { children: JSX.Element }) {
    const { isAuthenticated, isInitializing } = useAuth();

    if (isInitializing) {
        return <div>Loading auth...</div>;
    }

    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return children;
}

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<ProtectedRoute><HomePage /></ProtectedRoute>} />
            <Route path="/videos/:videoId" element={<VideoPage />} />
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
            <Route path="/login" element={<PublicOnlyRoute><LoginPage /></PublicOnlyRoute>} />
            <Route path="/register" element={<PublicOnlyRoute><RegisterPage /></PublicOnlyRoute>} />
        </Routes>
    );
}
