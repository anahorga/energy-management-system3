import axios from "axios";

export const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE, // ex: http://localhost:81
});

// atașează tokenul în headerul PE CARE ÎL AȘTEAPTĂ BACKEND-UL: "app-auth"
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers = config.headers ?? {};
        (config.headers as any)["app-auth"] = token;
    }
    return config;
});

api.interceptors.response.use(
    (res) => res,
    (err) => {
        if (err?.response?.status === 401) {
            localStorage.clear();
            if (window.location.pathname !== "/login") window.location.href = "/login";
        }
        return Promise.reject(err);
    }
);
