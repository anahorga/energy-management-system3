import React, { useMemo } from "react";
import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { ProtectedRoute } from "./routes/ProtectedRoute";
import Login from "./pages/Login";
import Admin from "./pages/AdminDashboard";
import Client from "./pages/ClientDashboard";
import { DeviceStatistics } from "./pages/DeviceStatistics";
import Register from "./pages/Register";
const HomeRedirect: React.FC = () => {
    const { role } = useAuth();
    const target = useMemo(() => (role === "ADMIN" ? "/admin" : "/client"), [role]);
    const loc = useLocation();
    if (loc.pathname === "/") return <Navigate to={target} replace />;
    return null;
};

function AppInner(){
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/admin" element={<ProtectedRoute allow={["ADMIN"]}><Admin /></ProtectedRoute>} />
            <Route path="/client" element={<ProtectedRoute allow={["USER"]}><Client /></ProtectedRoute>} />
            <Route
                path="/statistics/:deviceId"
                element={
                    <ProtectedRoute allow={["ADMIN", "USER"]}>
                        <DeviceStatistics />
                    </ProtectedRoute>
                }
            />

            <Route path="/*" element={<HomeRedirect />} />

        </Routes>
    );
}

export default function App(){
    return (<AuthProvider><BrowserRouter><AppInner/></BrowserRouter></AuthProvider>);
}
