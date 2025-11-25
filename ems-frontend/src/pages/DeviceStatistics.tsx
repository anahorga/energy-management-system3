import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import { SimpleBarChart } from './SimpleBarChart'; // <-- Importul este corect dacă ambele sunt în 'pages'

// Tipul MonitoringDto
type MonitoringDto = {
    id: number;
    timestamp: string;
    consumption: number;
    device: { id: number };
};

// Funcția toIsoDate
function toIsoDate(date: Date): string {
    return date.toISOString().split('T')[0];
}

export const DeviceStatistics: React.FC = () => {
    const { deviceId } = useParams<{ deviceId: string }>();
    const navigate = useNavigate();

    const [selectedDate, setSelectedDate] = useState<string>(toIsoDate(new Date()));
    const [data, setData] = useState<MonitoringDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    // useEffect pentru fetch
    useEffect(() => {
        if (!deviceId || !selectedDate) {
            setData([]);
            return;
        }
        const fetchStatistics = async () => {
            setLoading(true);
            setErr(null);
            setData([]);
            try {
                const response = await api.get<MonitoringDto[]>('/api/monitoring', {
                    params: {
                        deviceId: parseInt(deviceId),
                        day: selectedDate
                    }
                });
                setData(response.data);
            } catch (e: any) {
                console.error("Eroare la preluarea statisticilor:", e);
                setErr(e?.response?.data?.error || e?.message || "Failed to load statistics");
            } finally {
                setLoading(false);
            }
        };
        fetchStatistics();
    }, [deviceId, selectedDate]);

    // Transformarea datelor pentru grafic
    const chartData = useMemo(() => {
        if (data.length === 0) return [];

        return data.map(item => {
            const hour = new Date(item.timestamp).getHours();
            return {
                label: `${hour}:00`,
                value: item.consumption
            };
        });
    }, [data]);

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedDate(e.target.value);
    };

    if (!deviceId) {
        return <div style={{ padding: 24, color: 'crimson' }}>Error: Missing Device ID.</div>;
    }

    return (
        <div style={{ padding: 24, fontFamily: "sans-serif" }}>
            <button onClick={() => navigate(-1)} style={{ marginBottom: 16 }}>&larr; Înapoi</button>

            <h2>Statistici Consum (Device ID: {deviceId})</h2>

            <label>
                Selectează data:
                <input
                    type="date"
                    value={selectedDate}
                    onChange={handleDateChange}
                    style={{ marginLeft: 8 }}
                />
            </label>

            <hr style={{ margin: "16px 0" }} />

            {/* Afișarea componentelor */}
            {loading && <p>Se încarcă datele...</p>}
            {err && <p style={{ color: "crimson" }}>Eroare: {err}</p>}

            {!loading && !err && data.length === 0 && (
                <p>Nu există înregistrări pentru data selectată.</p>
            )}

            {data.length > 0 && (
                <div>
                    <h3 style={{textAlign: 'left'}}>Consum Orar (kWh)</h3>
                    <SimpleBarChart data={chartData} />

                    <h3 style={{textAlign: 'left', marginTop: '30px'}}>Date Brute</h3>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                        <tr style={{ borderBottom: '1px solid #555' }}>
                            <th style={{ padding: 8 }}>Timestamp (Oră agregată)</th>
                            <th style={{ padding: 8 }}>Consum (kWh)</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.map(item => (
                            <tr key={item.id} style={{ borderBottom: '1px solid #444' }}>
                                <td style={{ padding: 8 }}>{item.timestamp}</td>
                                <td style={{ padding: 8 }}>{item.consumption}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};