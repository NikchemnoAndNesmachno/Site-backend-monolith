import { useContext, useDebugValue } from "react";
import { AuthContext } from "../context/AuthContext";

const useAuth = () => {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider');
    }
    useDebugValue(
        context,
        context => context?.isAuthenticated ? "Logged In" : "Logged Out"
    );

    return context;
};

export default useAuth;