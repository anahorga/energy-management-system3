import React, { useEffect, useState } from "react";
import { api } from "../lib/api";
import { getUserIdFromToken } from "../lib/jwt";
import type { DeviceDto } from "../types/device";
import { useNavigate } from "react-router-dom";

// 1. Adaugă 'isVisible' la props
const MyDevices: React.FC<{ isVisible: boolean }> = ({ isVisible }) => {
    const [devices, setDevices] = useState<DeviceDto[]>([]);
    const [loading, setLoading] = useState(false); // Setat pe false inițial
    const [err, setErr] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        // 2. Rulează efectul doar dacă 'isVisible' este true
        if (!isVisible) {
            // Dacă devine invizibil, curățăm lista ca să fie proaspătă data viitoare
            setDevices([]);
            setLoading(false);
            return;
        }

        const userId = getUserIdFromToken();
        if (!userId) {
            setErr("Missing user id in token");
            setLoading(false);
            return;
        }

        (async () => {
            setLoading(true); // 3. Setăm loading doar când începem fetch-ul
            setErr(null);
            try {
                const { data } = await api.get<DeviceDto[]>(`/api/devices/${userId}`);
                setDevices(data);
            } catch (e: any) {
                setErr(e?.response?.data?.error || e?.message || "Failed to load devices");
            } finally {
                setLoading(false);
            }
        })();
    }, [isVisible]); // 4. 'isVisible' este acum dependența efectului

    // 5. Nu mai afișăm "Loading..." decât dacă este vizibil și încarcă
    if (isVisible && loading) return <div style={{ padding: 24 }}>Loading devices…</div>;
    if (isVisible && err) return <div style={{ padding: 24, color: "crimson" }}>{err}</div>;

    // 6. Afișăm conținutul doar dacă este vizibil
    if (!isVisible) return null;

    return (
        <div style={{ padding: 24, fontFamily: "sans-serif" }}>
            <h2>My devices</h2>
            {devices.length === 0 ? (
                <p>No devices assigned.</p>
            ) : (
                <ul style={{ marginTop: 12, listStyle: 'none', paddingLeft: 0 }}>
                    {devices.map(d => (
                        <li key={d.id} style={{ marginBottom: 12, border: '1px solid #555', padding: 8, borderRadius: 4 }}>
                            <strong>{d.name}</strong>
                            <div>Consumption: {d.consumption}</div>
                            <small>Device ID: {d.id}</small>

                            {/* Butonul va fi acum mereu vizibil (cât timp device-ul există) */}
                            <button
                                onClick={() => navigate(`/statistics/${d.id}`)}
                                style={{ marginLeft: 16, background: '#004a99' }}
                            >
                                Vezi Statistici
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default MyDevices;