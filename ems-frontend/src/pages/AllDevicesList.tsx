import React, { useState, useEffect } from 'react';
import { api } from '../lib/api';
import type { DeviceDto } from '../types/device';

// 1. Definim tipul pentru datele de editare
type EditDevicePayload = {
    name: string;
    consumption: number;
};

export const AllDevicesList: React.FC = () => {
    const [devices, setDevices] = useState<DeviceDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);
    const [deleteErr, setDeleteErr] = useState<string | null>(null);

    // 2. State pentru gestionarea editării
    const [editingDeviceId, setEditingDeviceId] = useState<number | null>(null);
    const [editFormData, setEditFormData] = useState<EditDevicePayload | null>(null);


    useEffect(() => {
        // ... (fetchDevices rămâne neschimbat)
        const fetchDevices = async () => {
            try {
                const { data } = await api.get<DeviceDto[]>('/api/devices');
                setDevices(data);
            } catch (e: any) {
                console.error("Eroare la preluarea device-urilor:", e);
                setErr(e?.response?.data?.error || e?.message || "Failed to load devices");
            } finally {
                setLoading(false);
            }
        };
        fetchDevices();
    }, []);

    const handleDeleteDevice = async (deviceId: number, deviceName: string) => {
        // ... (handleDeleteDevice rămâne neschimbat)
        if (!window.confirm(`Are you sure you want to delete device '${deviceName}' (ID: ${deviceId})?`)) {
            return;
        }
        setDeleteErr(null);
        try {
            await api.delete(`/api/devices/${deviceId}`);
            setDevices(currentDevices => currentDevices.filter(d => d.id !== deviceId));
        } catch (e: any) {
            setDeleteErr(e?.response?.data?.error || e?.message || "Delete failed");
        }
    };

    // 3. Funcții pentru a gestiona modul de editare
    const handleEditClick = (device: DeviceDto) => {
        setEditingDeviceId(device.id);
        setEditFormData({
            name: device.name,
            consumption: device.consumption,
        });
        setDeleteErr(null);
    };

    const handleCancelClick = () => {
        setEditingDeviceId(null);
        setEditFormData(null);
    };

    const handleEditFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setEditFormData(prev => prev ? {
            ...prev,
            [name]: name === 'consumption' ? parseFloat(value) || 0 : value
        } : null);
    };

    const handleSaveClick = async (deviceId: number) => {
        if (!editFormData) return;
        setDeleteErr(null);
        try {
            // Apelăm PUT /api/devices/{id}
            // Backend-ul se așteaptă la name și consumption
            const { data: updatedDevice } = await api.put<DeviceDto>(`/api/devices/${deviceId}`, editFormData);

            // Actualizăm state-ul local
            setDevices(prevDevices => prevDevices.map(d =>
                d.id === deviceId ? { ...d, ...updatedDevice } : d
            ));

            handleCancelClick();
        } catch (e: any) {
            setDeleteErr(e?.response?.data?.error || e?.message || "Update failed");
        }
    };


    if (loading) return <div style={{ padding: 16 }}>Loading all devices...</div>;
    if (err) return <div style={{ padding: 16, color: 'crimson' }}>{err}</div>;

    return (
        <div style={{ marginTop: 16, fontFamily: 'sans-serif' }}>
            <h3>All Devices</h3>
            {deleteErr && <p style={{ color: "crimson" }}>Error: {deleteErr}</p>}
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                {/* ... (thead rămâne neschimbat) ... */}
                <thead>
                <tr style={{ borderBottom: '1px solid #555' }}>
                    <th style={{ padding: 8 }}>Device ID</th>
                    <th style={{ padding: 8 }}>Name</th>
                    <th style={{ padding: 8 }}>Consumption</th>
                    <th style={{ padding: 8 }}>Assigned User ID</th>
                    <th style={{ padding: 8 }}>Actions</th>
                </tr>
                </thead>
                <tbody>
                {devices.map(device => (
                    <tr key={device.id} style={{ borderBottom: '1px solid #444' }}>
                        {/* 4. Randare condiționată: formular sau text */}
                        {editingDeviceId === device.id && editFormData ? (
                            <>
                                {/* Câmpurile non-editabile */}
                                <td style={{ padding: 8 }}>{device.id}</td>
                                {/* Câmpurile editabile */}
                                <td style={{ padding: 8 }}>
                                    <input
                                        name="name"
                                        value={editFormData.name}
                                        onChange={handleEditFormChange}
                                    />
                                </td>
                                <td style={{ padding: 8 }}>
                                    <input
                                        type="number"
                                        name="consumption"
                                        value={editFormData.consumption}
                                        onChange={handleEditFormChange}
                                        style={{width: '80px'}}
                                    />
                                </td>
                                {/* User ID nu este editabil */}
                                <td style={{ padding: 8 }}>{device.user?.id ?? 'N/A'}</td>
                                <td style={{ padding: 8 }}>
                                    <button onClick={() => handleSaveClick(device.id)} style={{ marginRight: 8 }}>Save</button>
                                    <button onClick={handleCancelClick} style={{ background: '#555' }}>Cancel</button>
                                </td>
                            </>
                        ) : (
                            <>
                                {/* Randarea normală (text) */}
                                <td style={{ padding: 8 }}>{device.id}</td>
                                <td style={{ padding: 8 }}>{device.name}</td>
                                <td style={{ padding: 8 }}>{device.consumption}</td>
                                <td style={{ padding: 8 }}>{device.user?.id ?? 'N/A'}</td>
                                <td style={{ padding: 8 }}>
                                    <button onClick={() => handleEditClick(device)} style={{ marginRight: 8 }}>Edit</button>
                                    <button
                                        onClick={() => handleDeleteDevice(device.id, device.name)}
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