import { useAuth } from "../context/AuthContext";
import MyDevices from "./MyDevices";

export default function ClientDashboard() {
    const { username, logout } = useAuth();

    return (
        <div style={{ padding: 24, fontFamily: "sans-serif" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h2>Client</h2>
                <div>
                    <span style={{ marginRight: 16 }}>Welcome, {username}</span>
                    <button onClick={logout}>Logout</button>
                </div>
            </div>

            <hr style={{ margin: "16px 0" }} />

            {/* Lista de device-uri ale userului curent */}
            <MyDevices isVisible={true} />
        </div>
    );
}
