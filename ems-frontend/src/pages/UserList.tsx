import React, { useState, useEffect } from 'react';
import { api } from '../lib/api';
import type { AuthUserDto } from '../types/auth';
import type { UserServiceDto } from '../types/user';

type CombinedUser = AuthUserDto & Partial<Omit<UserServiceDto, 'id'>>;
// 1. Definim tipul pentru datele de editare (doar câmpurile din UserService)
type EditUserPayload = Omit<UserServiceDto, 'id'>;

export const UserList: React.FC = () => {
    const [users, setUsers] = useState<CombinedUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);
    const [deleteErr, setDeleteErr] = useState<string | null>(null);

    // 2. State pentru gestionarea editării
    const [editingUserId, setEditingUserId] = useState<number | null>(null);
    const [editFormData, setEditFormData] = useState<EditUserPayload | null>(null);

    useEffect(() => {
        // ... (fetchUsers rămâne neschimbat)
        const fetchUsers = async () => {
            try {
                const [authRes, profileRes] = await Promise.all([
                    api.get<AuthUserDto[]>('/api/auth'),
                    api.get<UserServiceDto[]>('/api/users')
                ]);
                const authUsers = authRes.data;
                const userProfiles = profileRes.data;
                const profilesMap = new Map<number, Omit<UserServiceDto, 'id'>>();
                userProfiles.forEach(p => {
                    const { id, ...rest } = p;
                    profilesMap.set(id, rest);
                });
                const combined = authUsers.map(authUser => ({
                    ...authUser,
                    ...(profilesMap.get(authUser.id) || {}),
                }));
                setUsers(combined);
            } catch (e: any) {
                setErr(e?.response?.data?.error || e?.message || "Failed to load users");
            } finally {
                setLoading(false);
            }
        };
        fetchUsers();
    }, []);

    const handleDeleteUser = async (userId: number, username: string) => {
        if (!window.confirm(`Are you sure you want to delete user '${username}' (ID: ${userId})? This will delete all associated data.`)) {
            return;
        }
        setDeleteErr(null);
        try {

            await api.delete(`/api/auth/${userId}`);

            setUsers(currentUsers => currentUsers.filter(u => u.id !== userId));
        } catch (e: any) {
            console.error("Delete failed:", e);
            setDeleteErr(e?.response?.data?.error || e?.message || "Delete failed");
        }
    };

    const handleEditClick = (user: CombinedUser) => {
        setEditingUserId(user.id);
        setEditFormData({
            firstName: user.firstName || '',
            lastName: user.lastName || '',
            address: user.address || '',
            email: user.email || '',
        });
        setDeleteErr(null); // Resetează erorile
    };

    const handleCancelClick = () => {
        setEditingUserId(null);
        setEditFormData(null);
    };

    const handleEditFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEditFormData(prev => prev ? { ...prev, [e.target.name]: e.target.value } : null);
    };

    const handleSaveClick = async (userId: number) => {
        if (!editFormData) return;
        setDeleteErr(null);
        try {
            // Apelăm PUT /api/users/{id}
            const { data: updatedUser } = await api.put<UserServiceDto>(`/api/users/${userId}`, editFormData);

            // Actualizăm state-ul local
            setUsers(prevUsers => prevUsers.map(u =>
                u.id === userId ? { ...u, ...updatedUser } : u
            ));

            // Ieșim din modul de editare
            handleCancelClick();
        } catch (e: any) {
            setDeleteErr(e?.response?.data?.error || e?.message || "Update failed");
        }
    };

    if (loading) return <div style={{ padding: 16 }}>Loading users...</div>;
    if (err) return <div style={{ padding: 16, color: 'crimson' }}>{err}</div>;

    return (
        <div style={{ marginTop: 16, fontFamily: 'sans-serif' }}>
            <h3>User Management</h3>
            {deleteErr && <p style={{ color: "crimson" }}>Error: {deleteErr}</p>}
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                {/* ... (thead rămâne neschimbat) ... */}
                <thead>
                <tr style={{ borderBottom: '1px solid #555' }}>
                    <th style={{ padding: 8 }}>ID</th>
                    <th style={{ padding: 8 }}>Username</th>
                    <th style={{ padding: 8 }}>Role</th>
                    <th style={{ padding: 8 }}>Full Name</th>
                    <th style={{ padding: 8 }}>Email</th>
                    <th style={{ padding: 8 }}>Address</th>
                    <th style={{ padding: 8 }}>Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map(user => (
                    <tr key={user.id} style={{ borderBottom: '1px solid #444' }}>
                        {/* 4. Randare condiționată: formular sau text */}
                        {editingUserId === user.id && editFormData ? (
                            <>
                                {/* Câmpurile non-editabile */}
                                <td style={{ padding: 8 }}>{user.id}</td>
                                <td style={{ padding: 8 }}>{user.username}</td>
                                <td style={{ padding: 8 }}>{user.userRole}</td>
                                {/* Câmpurile editabile */}
                                <td style={{ padding: 8 }}>
                                    <input
                                        name="firstName"
                                        value={editFormData.firstName}
                                        onChange={handleEditFormChange}
                                        style={{width: '100px'}}
                                    />
                                    <input
                                        name="lastName"
                                        value={editFormData.lastName}
                                        onChange={handleEditFormChange}
                                        style={{width: '100px', marginTop: '4px'}}
                                    />
                                </td>
                                <td style={{ padding: 8 }}>
                                    <input
                                        type="email"
                                        name="email"
                                        value={editFormData.email}
                                        onChange={handleEditFormChange}
                                        style={{width: '150px'}}
                                    />
                                </td>
                                <td style={{ padding: 8 }}>
                                    <input
                                        name="address"
                                        value={editFormData.address}
                                        onChange={handleEditFormChange}
                                        style={{width: '150px'}}
                                    />
                                </td>
                                <td style={{ padding: 8 }}>
                                    <button onClick={() => handleSaveClick(user.id)} style={{ marginRight: 8 }}>Save</button>
                                    <button onClick={handleCancelClick} style={{ background: '#555' }}>Cancel</button>
                                </td>
                            </>
                        ) : (
                            <>
                                {/* Randarea normală (text) */}
                                <td style={{ padding: 8 }}>{user.id}</td>
                                <td style={{ padding: 8 }}>{user.username}</td>
                                <td style={{ padding: 8 }}>{user.userRole}</td>
                                <td style={{ padding: 8 }}>{user.firstName || 'N/A'} {user.lastName || ''}</td>
                                <td style={{ padding: 8 }}>{user.email || 'N/A'}</td>
                                <td style={{ padding: 8 }}>{user.address || 'N/A'}</td>
                                <td style={{ padding: 8 }}>
                                    <button onClick={() => handleEditClick(user)} style={{ marginRight: 8 }}>Edit</button>
                                    <button
                                        onClick={() => handleDeleteUser(user.id, user.username)}
                                        style={{ background: '#500' }}
                                    >
                                        Delete
                                    </button>
                                </td>
                            </>
                        )}
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};