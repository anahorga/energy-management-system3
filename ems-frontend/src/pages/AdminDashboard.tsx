import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { createUserAsAdmin } from "../lib/adminProvision";
import type { UserRole } from "../types/auth";
import MyDevices from "./MyDevices";
import { UserList } from "./UserList";
import { AllDevicesList } from "./AllDevicesList";
import { api } from "../lib/api";

const roles: UserRole[] = ["USER", "ADMIN"];

export default function AdminDashboard() {
    const { username, role, logout } = useAuth();
    const [showForm, setShowForm] = useState(false);
    const [showDevices, setShowDevices] = useState(false);
    const [showUsers, setShowUsers] = useState(false);
    const [showAllDevices, setShowAllDevices] = useState(false);
    const [showDeviceForm, setShowDeviceForm] = useState(false); // <-- 2. State pentru formularul de device

    // State pentru formularul de user
    const [f, setF] = useState({
        username: "",
        password: "",
        userRole: "USER" as UserRole,
        firstName: "",
        lastName: "",
        address: "",
        email: "",
    });
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);
    const [ok, setOk] = useState<string | null>(null);

    // <-- 3. Adaugă state nou pentru formularul de device -->
    const [devForm, setDevForm] = useState({
        name: "",
        consumption: "",
        userId: "",
    });
    const [devLoading, setDevLoading] = useState(false);
    const [devErr, setDevErr] = useState<string | null>(null);
    const [devOk, setDevOk] = useState<string | null>(null);

    // Funcția de creare User (rămâne neschimbată)
    const onCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        setErr(null);
        setOk(null);
        setLoading(true);
        try {
            const { role: createdRole, id } = await createUserAsAdmin(
                { username: f.username, password: f.password, userRole: f.userRole },
                { firstName: f.firstName, lastName: f.lastName, address: f.address, email: f.email }
            );
            setOk(`User creat: ID=${id}, rol=${createdRole}.`);
            setF({
                username: "",
                password: "",
                userRole: "USER",
                firstName: "",
                lastName: "",
                address: "",
                email: "",
            });
            setShowForm(false); // Ascunde formularul după succes
        } catch (e: any) {
            setErr(e?.response?.data?.error || e?.message || "Create user failed");
        } finally {
            setLoading(false);
        }
    };

    // <-- 4. Adaugă funcția de creare Device -->
    const onCreateDevice = async (e: React.FormEvent) => {
        e.preventDefault();
        setDevErr(null);
        setDevOk(null);
        setDevLoading(true);

        try {
            // Construim payload-ul exact cum se așteaptă backend-ul
            //
            const payload = {
                name: devForm.name,
                consumption: parseFloat(devForm.consumption) || 0,
                user: {
                    id: parseInt(devForm.userId)
                }
            };

            if (isNaN(payload.user.id)) {
                throw new Error("User ID must be a number.");
            }

            // Apelăm endpoint-ul POST /api/devices
            //
            await api.post('/api/devices', payload);

            setDevOk(`Device '${devForm.name}' created and assigned to user ID ${devForm.userId}.`);
            setDevForm({ name: "", consumption: "", userId: "" }); // Reset form
            setShowDeviceForm(false); // Ascunde formularul
        } catch (e: any) {
            // Backend-ul returnează erori dacă ID-ul userului nu e găsit
            //
            setDevErr(e?.response?.data?.error || e?.message || "Create device failed");
        } finally {
            setDevLoading(false);
        }
    };


    return (
        <div style={{ padding: 24, fontFamily: "sans-serif" }}>
            {/* Header */}
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h2>Admin dashboard</h2>
                <div>
                    <span style={{ marginRight: 16 }}>Logged in: {username}</span>
                    <button onClick={logout}>Logout</button>
                </div>
            </div>

            <hr style={{ margin: "16px 0" }} />

            {/* Acțiuni rapide */}
            <div style={{ display: "flex", gap: 8, marginBottom: 12, flexWrap: 'wrap' }}>
                <button onClick={() => setShowDevices((s) => !s)}>
                    {showDevices ? "Ascunde device-urile mele" : "Vezi device-urile mele"}
                </button>

                {role === "ADMIN" && (
                    <>
                        <button onClick={() => setShowForm((s) => !s)}>{showForm ? "Close" : "Add user"}</button>
                        {/* 5. Butonul de adăugare device */}
                        <button onClick={() => setShowDeviceForm((s) => !s)}>
                            {showDeviceForm ? "Close" : "Add device"}
                        </button>
                        <button onClick={() => setShowUsers((s) => !s)}>
                            {showUsers ? "Ascunde useri" : "Vezi useri"}
                        </button>
                        <button onClick={() => setShowAllDevices((s) => !s)}>
                            {showAllDevices ? "Ascunde device-uri" : "Vezi toate device-urile"}
                        </button>
                    </>
                )}
            </div>

            {/* Device-urile mele (toggle) */}
            {showDevices && (
                <div style={{ border: "1px solid #ddd", borderRadius: 8, marginBottom: 16, overflowX: 'auto' }}>
                    {/* Aici este modificarea: */}
                    <MyDevices isVisible={showDevices} />
                </div>
            )}

            {/* Conținut principal */}
            {role !== "ADMIN" ? (
                <p style={{ color: "crimson" }}>Nu ai rol de ADMIN.</p>
            ) : (
                <>
                    {/* Form de creare user (admin) */}
                    {showForm && (
                        <form onSubmit={onCreate} style={{ marginTop: 16, maxWidth: 560, display: "grid", gap: 8 }}>
                            {/* ... (formularul de user neschimbat) ... */}
                            <fieldset style={{ border: "1px solid #ccc", padding: 12 }}>
                                <legend>Auth</legend>
                                <label>
                                    Username
                                    <input
                                        value={f.username}
                                        onChange={(e) => setF((s) => ({ ...s, username: e.target.value }))}
                                        required
                                    />
                                </label>
                                <label>
                                    Password
                                    <input
                                        type="password"
                                        value={f.password}
                                        onChange={(e) => setF((s) => ({ ...s, password: e.target.value }))}
                                        required
                                    />
                                </label>
                                <label>
                                    Role
                                    <select
                                        value={f.userRole}
                                        onChange={(e) => setF((s) => ({ ...s, userRole: e.target.value as UserRole }))}
                                    >
                                        {roles.map((r) => (
                                            <option key={r} value={r}>
                                                {r}
                                            </option>
                                        ))}
                                    </select>
                                </label>
                            </fieldset>

                            <fieldset style={{ border: "1px solid #ccc", padding: 12 }}>
                                <legend>Profile</legend>
                                <label>
                                    First name
                                    <input
                                        value={f.firstName}
                                        onChange={(e) => setF((s) => ({ ...s, firstName: e.target.value }))}
                                        required
                                    />
                                </label>
                                <label>
                                    Last name
                                    <input
                                        value={f.lastName}
                                        onChange={(e) => setF((s) => ({ ...s, lastName: e.target.value }))}
                                        required
                                    />
                                </label>
                                <label>
                                    Address
                                    <input
                                        value={f.address}
                                        onChange={(e) => setF((s) => ({ ...s, address: e.target.value }))}
                                        required
                                    />
                                </label>
                                <label>
                                    Email
                                    <input
                                        type="email"
                                        value={f.email}
                                        onChange={(e) => setF((s) => ({ ...s, email: e.target.value }))}
                                        required
                                    />
                                </label>
                            </fieldset>

                            <div style={{ display: "flex", gap: 8 }}>
                                <button type="submit" disabled={loading}>
                                    {loading ? "Creating..." : "Create user"}
                                </button>
                                <button type="button" onClick={() => setShowForm(false)}>
                                    Cancel
                                </button>
                            </div>

                            {err && <p style={{ color: "crimson" }}>{err}</p>}
                            {ok && <p style={{ color: "green" }}>{ok}</p>}
                        </form>
                    )}

                    {/* <-- 6. Adaugă formularul de creare device --> */}
                    {showDeviceForm && (
                        <form onSubmit={onCreateDevice} style={{ marginTop: 16, maxWidth: 560, display: "grid", gap: 8 }}>
                            <fieldset style={{ border: "1px solid #ccc", padding: 12 }}>
                                <legend>New Device</legend>
                                <label>
                                    Device Name
                                    <input
                                        value={devForm.name}
                                        onChange={(e) => setDevForm(s => ({...s, name: e.target.value}))}
                                        required
                                    />
                                </label>
                                <label>
                                    Consumption (kW)
                                    <input
                                        type="number"
                                        step="0.1"
                                        value={devForm.consumption}
                                        onChange={(e) => setDevForm(s => ({...s, consumption: e.target.value}))}
                                        required
                                    />
                                </label>
                                <label>
                                    User ID
                                    <input
                                        type="number"
                                        step="1"
                                        value={devForm.userId}
                                        onChange={(e) => setDevForm(s => ({...s, userId: e.target.value}))}
                                        placeholder="Enter existing user ID"
                                        required
                                    />
                                </label>
                            </fieldset>

                            <div style={{ display: "flex", gap: 8 }}>
                                <button type="submit" disabled={devLoading}>
                                    {devLoading ? "Creating..." : "Create Device"}
                                </button>
                                <button type="button" onClick={() => setShowDeviceForm(false)}>
                                    Cancel
                                </button>
                            </div>

                            {devErr && <p style={{ color: "crimson" }}>{devErr}</p>}
                            {devOk && <p style={{ color: "green" }}>{devOk}</p>}
                        </form>
                    )}

                    {/* Lista de Useri */}
                    {showUsers && (
                        <div style={{ border: "1px solid #ddd", borderRadius: 8, marginTop: 16, overflowX: 'auto' }}>
                            <UserList />
                        </div>
                    )}

                    {/* Lista de Device-uri */}
                    {showAllDevices && (
                        <div style={{ border: "1px solid #ddd", borderRadius: 8, marginTop: 16, overflowX: 'auto' }}>
                            <AllDevicesList />
                        </div>
                    )}
                </>
            )}
        </div>
    );
}