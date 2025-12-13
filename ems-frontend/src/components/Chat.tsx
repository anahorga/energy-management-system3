import React, { useState, useEffect, useRef, useMemo } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';
import { jwtDecode } from "jwt-decode";

// --- TIPURI ---
type Message = {
    senderId: string;
    content: string;
    isAdmin: boolean;
};

type Notification = {
    message: string;
    userId: number;
    deviceId: number;
};

interface DecodedToken {
    id: number;
    sub: string;
    role: string;
    exp: number;
}

export const Chat: React.FC = () => {
    const { username, role } = useAuth();

    // UI State
    const [isOpen, setIsOpen] = useState(false);
    const [hasUnread, setHasUnread] = useState(false);
    const [activeTab, setActiveTab] = useState<'bot' | 'admin'>('bot');
    const [input, setInput] = useState("");

    // Message Data
    const [botMessages, setBotMessages] = useState<Message[]>([]);
    const [adminMessages, setAdminMessages] = useState<Message[]>([]);
    const [currentChatUser, setCurrentChatUser] = useState<string | null>(null);

    // WebSocket Reference
    const clientRef = useRef<any>(null);

    // Helpers
    const displayMessages = activeTab === 'bot' ? botMessages : adminMessages;

    const currentChatName = useMemo(() => {
        if (role !== "ADMIN") return "Admin";
        return currentChatUser ?? "User";
    }, [role, currentChatUser]);

    const getCurrentUserId = (): number | null => {
        const token = localStorage.getItem("token");
        if (!token) return null;
        try {
            const decoded = jwtDecode<DecodedToken>(token);
            return decoded.id;
        } catch (error) {
            console.error("Eroare decodare token:", error);
            return null;
        }
    };

    // --- WEBSOCKET LOGIC ---
    useEffect(() => {
        // Dacă există o conexiune veche, o închidem (ex: la logout sau user switch)
        if (clientRef.current) {
            try {
                clientRef.current.disconnect();
            } catch (e) {
                console.warn("Eroare la deconectare veche:", e);
            }
            clientRef.current = null;
        }

        const myNumericId = getCurrentUserId();
        const myUsername = username || "Guest";

        console.log(`LOG: Inițializare WebSocket pentru ${myUsername} (ID: ${myNumericId})`);

        const socket = new SockJS('http://localhost:81/chat-socket');
        const client = Stomp.over(socket);

        // Dezactivează logurile detaliate în consolă (opțional)
        client.debug = () => {};

        client.connect({}, () => {
            console.log("LOG: WebSocket conectat!");

            // 1. CHAT BOT - Canal personal
            client.subscribe(`/topic/bot.${myUsername}`, (msg: any) => {
                if (msg.body) {
                    const parsed: Message = JSON.parse(msg.body);
                    handleIncomingBotMessage(parsed);
                }
            });

            // 2. NOTIFICĂRI -> Transformate în MESAJE BOT
            if (myNumericId) {
                console.log("LOG: Abonat la notificări ID:", myNumericId);
                client.subscribe(`/topic/user/${myNumericId}/notifications`, (msg: any) => {
                    if (msg.body) {
                        const notif: Notification = JSON.parse(msg.body);

                        // Creăm un mesaj artificial de la robot
                        const alertMsg: Message = {
                            senderId: "RobBot",
                            content: `⚠️ ALERTĂ: ${notif.message}`,
                            isAdmin: false
                        };
                        handleIncomingBotMessage(alertMsg);
                    }
                });
            }

            // 3. ADMIN CHAT
            client.subscribe('/topic/admin', (msg: any) => {
                if (msg.body) {
                    const parsed: Message = JSON.parse(msg.body);
                    handleIncomingAdminMessage(parsed, myUsername, role);
                }
            });

        }, (err: any) => {
            console.error("Eroare conexiune WebSocket:", err);
        });

        clientRef.current = client;

        // Cleanup la unmount (refresh pagină sau logout complet din app)
        return () => {
            if (clientRef.current) {
                clientRef.current.disconnect();
                clientRef.current = null;
            }
        };
        // Dependențele sunt critice: reconectăm dacă se schimbă userul
    }, [username, role]);


    // --- HANDLERS (Definite pentru a folosi State-ul corect) ---

    // Adaugă mesaj la Bot și gestionează notificarea (punct roșu)
    const handleIncomingBotMessage = (msg: Message) => {
        setBotMessages(prev => [...prev, msg]);
        setIsOpen(currentIsOpen => {
            if (!currentIsOpen) {
                setHasUnread(true); // Aprinde beculețul dacă chat-ul e închis
            }
            return currentIsOpen;
        });
    };

    const handleIncomingAdminMessage = (msg: Message, myUser: string, myRole: string | null) => {
        if (myRole === "ADMIN") {
            if (msg.senderId !== myUser) {
                // Admin primește mesaj de la User
                setCurrentChatUser(() => {
                    // Dacă e un user nou, resetăm sau adăugăm (aici adăugăm)
                    return msg.senderId;
                });
                setAdminMessages(prev => [...prev, msg]);

                setIsOpen(currentIsOpen => {
                    if (!currentIsOpen) setHasUnread(true);
                    return currentIsOpen;
                });
            } else {
                // Admin vede propriul mesaj
                setAdminMessages(prev => [...prev, msg]);
            }
        } else {
            // User primește mesaj de la Admin
            setAdminMessages(prev => [...prev, msg]);
            setIsOpen(currentIsOpen => {
                if (!currentIsOpen) setHasUnread(true);
                return currentIsOpen;
            });
        }
    };

    const handleSend = () => {
        if (!input.trim() || !clientRef.current) return;

        const payload: Message = {
            senderId: username || "Guest",
            content: input,
            isAdmin: role === "ADMIN",
        };

        const dest = activeTab === "bot" ? "/app/chat/bot" : "/app/chat/admin";

        if (clientRef.current.connected) {
            clientRef.current.send(dest, {}, JSON.stringify(payload));
            setInput("");
        }
    };

    const toggleChat = () => {
        setIsOpen(prev => {
            if (!prev) setHasUnread(false); // Dacă deschidem, stingem notificarea
            return !prev;
        });
    };

    // --- RENDER UI ---

    if (!isOpen) {
        return (
            <div style={{ position: 'fixed', bottom: 20, right: 20, zIndex: 9999 }}>
                {hasUnread && (
                    <div style={{
                        position: 'absolute', top: 0, right: 0,
                        width: 16, height: 16, borderRadius: '50%',
                        backgroundColor: '#dc3545', border: '2px solid white'
                    }} />
                )}
                <button
                    onClick={toggleChat}
                    style={{
                        width: 60, height: 60, borderRadius: '50%',
                        backgroundColor: '#007bff', color: 'white',
                        border: 'none', fontSize: '24px', cursor: 'pointer',
                        boxShadow: '0 4px 10px rgba(0,0,0,0.3)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center'
                    }}
                >
                    💬
                </button>
            </div>
        );
    }

    return (
        <div style={{
            position: 'fixed', bottom: 20, right: 20,
            width: 320, height: 450, background: 'white',
            borderRadius: 12, boxShadow: '0 5px 20px rgba(0,0,0,0.2)',
            display: 'flex', flexDirection: 'column', overflow: 'hidden',
            border: '1px solid #ddd', zIndex: 9999,
            fontFamily: 'Arial, sans-serif'
        }}>
            {/* Header */}
            <div style={{
                padding: '12px', background: '#007bff', color: 'white',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center'
            }}>
                <span style={{ fontWeight: 'bold' }}>Support Chat</span>
                <button
                    onClick={toggleChat}
                    style={{
                        background: 'transparent', border: 'none',
                        color: 'white', fontSize: '18px', cursor: 'pointer'
                    }}
                >
                    ✕
                </button>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', borderBottom: '1px solid #ddd', background: '#f8f9fa' }}>
                <div
                    onClick={() => setActiveTab('bot')}
                    style={{
                        flex: 1, padding: '10px', textAlign: 'center', cursor: 'pointer',
                        background: activeTab === 'bot' ? 'white' : 'transparent',
                        borderBottom: activeTab === 'bot' ? '3px solid #007bff' : 'none',
                        fontWeight: activeTab === 'bot' ? 'bold' : 'normal',
                        color: activeTab === 'bot' ? '#007bff' : '#666'
                    }}
                >
                    🤖 Robot
                </div>
                <div
                    onClick={() => setActiveTab('admin')}
                    style={{
                        flex: 1, padding: '10px', textAlign: 'center', cursor: 'pointer',
                        background: activeTab === 'admin' ? 'white' : 'transparent',
                        borderBottom: activeTab === 'admin' ? '3px solid #007bff' : 'none',
                        fontWeight: activeTab === 'admin' ? 'bold' : 'normal',
                        color: activeTab === 'admin' ? '#007bff' : '#666'
                    }}
                >
                    👨‍💼 {currentChatName}
                </div>
            </div>

            {/* Lista Mesaje */}
            <div style={{
                flex: 1, padding: "15px", overflowY: "auto",
                backgroundColor: "#fff", display: "flex", flexDirection: "column", gap: "10px",
            }}>
                {displayMessages.length === 0 && (
                    <div style={{ textAlign: "center", color: "#ccc", marginTop: 40, fontSize: "0.9em" }}>
                        {activeTab === 'bot'
                            ? "Nu ai notificări sau mesaje de la robot."
                            : "Începe o conversație cu administratorul."}
                    </div>
                )}

                {displayMessages.map((msg, idx) => {
                    const isMe = msg.senderId === username;
                    const isBot = msg.senderId === "RobBot";
                    const isAlert = msg.content.includes("ALERTĂ");

                    return (
                        <div key={idx} style={{
                            alignSelf: isMe ? "flex-end" : "flex-start",
                            maxWidth: "85%", display: "flex", flexDirection: "column",
                            alignItems: isMe ? "flex-end" : "flex-start",
                        }}>
                            <span style={{ fontSize: "0.7em", color: "#999", marginBottom: 2, marginLeft: 4 }}>
                                {isBot ? "Sistem" : msg.senderId}
                            </span>
                            <div style={{
                                padding: "10px 14px", borderRadius: "14px",
                                borderTopLeftRadius: isMe ? "14px" : "2px",
                                borderTopRightRadius: isMe ? "2px" : "14px",
                                backgroundColor: isMe
                                    ? "#007bff"
                                    : (isAlert ? "#ffebee" : (isBot ? "#e8f5e9" : "#f1f3f5")),
                                color: isMe ? "white" : (isAlert ? "#c62828" : "black"),
                                border: isAlert ? "1px solid #ffcdd2" : "none",
                                boxShadow: "0 1px 2px rgba(0,0,0,0.1)",
                                fontSize: "14px", lineHeight: "1.4"
                            }}>
                                {msg.content}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Input */}
            <div style={{ padding: '12px', borderTop: '1px solid #ddd', display: 'flex', gap: '8px', background: 'white' }}>
                <input
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleSend()}
                    placeholder="Scrie mesaj..."
                    style={{
                        flex: 1, padding: '10px', borderRadius: 20,
                        border: '1px solid #ccc', outline: 'none'
                    }}
                />
                <button onClick={handleSend} style={{
                    background: '#007bff', color: 'white', border: 'none',
                    borderRadius: '50%', width: 40, height: 40, cursor: 'pointer',
                    fontWeight: 'bold'
                }}>➤</button>
            </div>
        </div>
    );
};