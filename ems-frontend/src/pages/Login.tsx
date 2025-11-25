import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { Link, useNavigate } from 'react-router-dom';

const Login: React.FC = () => {
    const { login } = useAuth();
    const navigate = useNavigate(); // Hook pentru navigare
    const [form, setForm] = useState({ username: "", password: "" });
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErr(null);
        setLoading(true);
        try {
            await login(form.username, form.password);
            // !!! AICI ERA PROBLEMA: Trebuie să te trimitem explicit pe Home după login
            navigate("/");
        } catch (e: any) {
            console.error("Login error:", e);
            // Extragem mesajul de eroare din backend sau punem unul generic
            const message = e?.response?.data?.error || e?.message || "Login failed. Please check your credentials.";
            setErr(message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2 style={styles.title}>Energy Management System</h2>
                <p style={styles.subtitle}>Please sign in to continue</p>

                <form onSubmit={onSubmit} style={styles.form}>
                    {/* Afișăm eroarea aici, dacă există */}
                    {err && <div style={styles.error}>{err}</div>}

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Username</label>
                        <input
                            style={styles.input}
                            value={form.username}
                            onChange={e => setForm(s => ({ ...s, username: e.target.value }))}
                            placeholder="Enter your username"
                            required
                        />
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Password</label>
                        <input
                            type="password"
                            style={styles.input}
                            value={form.password}
                            onChange={e => setForm(s => ({ ...s, password: e.target.value }))}
                            placeholder="Enter your password"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        style={{ ...styles.button, opacity: loading ? 0.7 : 1 }}
                    >
                        {loading ? "Signing in..." : "Sign In"}
                    </button>
                </form>

                <div style={styles.footer}>
                    <p style={styles.footerText}>
                        Don't have an account?{' '}
                        <Link to="/register" style={styles.link}>Register here</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

// --- Stiluri CSS-in-JS ---
const styles: { [key: string]: React.CSSProperties } = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f0f2f5',
        padding: '20px',
    },
    card: {
        backgroundColor: '#ffffff',
        width: '100%',
        maxWidth: '400px',
        padding: '40px',
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    title: {
        margin: '0 0 10px 0',
        color: '#333',
        fontSize: '24px',
        fontWeight: '600',
        textAlign: 'center',
    },
    subtitle: {
        margin: '0 0 30px 0',
        color: '#666',
        fontSize: '14px',
        textAlign: 'center',
    },
    form: {
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
    },
    inputGroup: {
        display: 'flex',
        flexDirection: 'column',
        textAlign: 'left',
    },
    label: {
        marginBottom: '8px',
        fontSize: '14px',
        color: '#555',
        fontWeight: '500',
    },
    input: {
        padding: '12px',
        borderRadius: '6px',
        border: '1px solid #ddd',
        fontSize: '16px',
        outline: 'none',
        transition: 'border-color 0.2s',
        backgroundColor: '#fafafa',
        color: '#333',
    },
    button: {
        padding: '12px',
        borderRadius: '6px',
        border: 'none',
        backgroundColor: '#007bff',
        color: 'white',
        fontSize: '16px',
        fontWeight: '600',
        cursor: 'pointer',
        transition: 'background-color 0.2s',
        marginTop: '10px',
    },
    error: {
        color: '#d32f2f',
        backgroundColor: '#ffebee',
        padding: '12px',
        borderRadius: '6px',
        fontSize: '14px',
        textAlign: 'center',
        border: '1px solid #ef9a9a'
    },
    footer: {
        marginTop: '24px',
        textAlign: 'center',
        borderTop: '1px solid #eee',
        paddingTop: '20px',
        width: '100%',
    },
    footerText: {
        fontSize: '14px',
        color: '#666',
        margin: 0,
    },
    link: {
        color: '#007bff',
        textDecoration: 'none',
        fontWeight: '500',
    }
};

export default Login;