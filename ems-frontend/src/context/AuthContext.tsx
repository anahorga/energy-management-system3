import React, { createContext, useContext, useMemo, useState } from "react";
import { api } from "../lib/api";
import { decodeJwt, type JwtClaims } from "../lib/jwt";

type AuthState = {
    token: string | null;
    role: string | null;
    username: string | null;
    userId: string | null;
};
type AuthCtx = AuthState & {
    login: (u: string, p: string) => Promise<void>;
    logout: () => void;
};
const Ctx = createContext<AuthCtx | null>(null);

export const AuthProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
    const [state, setState] = useState<AuthState>(() => ({
        token: localStorage.getItem("token"),
        role: localStorage.getItem("role"),
        username: localStorage.getItem("username"),
        userId: localStorage.getItem("userId"),
    }));

    const login = async (username: string, password: string) => {
        const { data } = await api.post<{ token: string }>("/api/auth/login", { username, password });
        const token = data.token;
        if (!token) throw new Error("Token missing");

        const claims: JwtClaims | null = decodeJwt(token);
        const role = (claims?.role ?? null) as string | null;
        const userId = claims?.id != null ? String(claims.id) : null;
        const uname = claims?.username ?? username;

        localStorage.setItem("token", token);
        if (role) localStorage.setItem("role", role);
        if (uname) localStorage.setItem("username", uname);
        if (userId) localStorage.setItem("userId", userId);

        setState({ token, role, username: uname, userId });
    };

    const logout = () => {
        localStorage.clear();
        setState({ token: null, role: null, username: null, userId: null });
        window.location.href = "/login";
    };

    const value = useMemo(() => ({ ...state, login, logout }), [state]);
    return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
};

export const useAuth = () => {
    const v = useContext(Ctx);
    if (!v) throw new Error("useAuth must be used within AuthProvider");
    return v;
};
