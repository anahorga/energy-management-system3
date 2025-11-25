import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../lib/api';

type RegisterForm = {
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    address: string;
    email: string;
};

export default function Register() {
    const navigate = useNavigate();
    const [form, setForm] = useState<RegisterForm>({
        username: '',
        password: '',
        firstName: '',
        lastName: '',
        address: '',
        email: ''
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            await api.post('/api/auth/register', {
                ...form,
                userRole: 'USER' // Chiar dacă backend-ul îl forțează, DTO-ul Auth cere acest câmp. Îl trimitem ca USER.
            });

            alert('Account created successfully! Please log in.');
            navigate('/login'); // Redirecționare către login după succes
        } catch (err: any) {
            setError(err?.response?.data?.error || err?.message || 'Registration failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: 400, margin: '40px auto', padding: 24, fontFamily: 'sans-serif', border: '1px solid #ccc', borderRadius: 8 }}>
            <h2 style={{ textAlign: 'center' }}>Create an Account</h2>

            <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 12 }}>

                {/* Credențiale */}
                <div style={{ display: 'grid', gap: 4 }}>
                    <label>Username</label>
                    <input name="username" value={form.username} onChange={handleChange} required />
                </div>
                <div style={{ display: 'grid', gap: 4 }}>
                    <label>Password</label>
                    <input type="password" name="password" value={form.password} onChange={handleChange} required />
                </div>

                <hr style={{ margin: '8px 0', border: '0', borderTop: '1px solid #eee' }} />

                {/* Profil */}
                <div style={{ display: 'grid', gap: 4 }}>
                    <label>First Name</label>
                    <input name="firstName" value={form.firstName} onChange={handleChange} required />
                </div>
                <div style={{ display: 'grid', gap: 4 }}>
                    <label>Last Name</label>
                    <input name="lastName" value={form.lastName} onChange={handleChange} required />
                </div>
                <div style={{ display: 'grid', gap: 4 }}>
                    <label>Email</label>
                    <input type="email" name="email" value={form.email} onChange={handleChange} required />
                </div>
                <div style={{ display: 'grid', gap: 4 }}>
                    <label>Address</label>
                    <input name="address" value={form.address} onChange={handleChange} required />
                </div>

                {/* Acțiuni */}
                <button type="submit" disabled={loading} style={{ marginTop: 12, padding: 10, cursor: 'pointer' }}>
                    {loading ? 'Registering...' : 'Register'}
                </button>

                {error && <p style={{ color: 'crimson', textAlign: 'center' }}>{error}</p>}
            </form>

            <p style={{ textAlign: 'center', marginTop: 16 }}>
                Already have an account? <Link to="/login">Log in here</Link>
            </p>
        </div>
    );
}