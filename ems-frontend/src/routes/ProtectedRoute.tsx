import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export const ProtectedRoute: React.FC<React.PropsWithChildren<{ allow?: string[] }>> = ({ children, allow }) => {
    const { token, role } = useAuth();
    if (!token) return <Navigate to="/login" replace />;
    if (allow && role && !allow.includes(role)) return <Navigate to="/" replace />;
    return <>{children}</>;
};
