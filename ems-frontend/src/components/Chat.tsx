import React, { useState, useEffect, useRef, useMemo } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';

// Structura unui mesaj
type Message = {
    senderId: string;
    content: string;
    isAdmin: boolean;
};

export const Chat: React.FC = () => {
    const { username, role } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const [activeTab, setActiveTab] = useState<'bot' | 'admin'>('bot');
    const [input, setInput] = useState("");

    // Mesaje pe fiecare tab
    const [botMessages, setBotMessages] = useState<Message[]>([]);
    const [adminMessages, setAdminMessages] = useState<Message[]>([]);

    // Pentru admin → userul cu care vorbește ACUM
    const [currentChatUser, setCurrentChatUser] = useState<string | null>(null);

    // referință către WebSocket client
    const clientRef = useRef<any>(null);

    // mesajele vizibile în UI
    const displayMessages = activeTab === 'bot' ? botMessages : adminMessages;

    // numele afișat în tab-ul de admin
    const currentChatName = useMemo(() => {
        if (role !== "ADMIN") return "Admin";
        return currentChatUser ?? "";
    }, [role, currentChatUser]);

    useEffect(() => {
        if (isOpen && !clientRef.current) {
            const socket = new SockJS('http://localhost:81/chat-socket');
            const client = Stomp.over(socket);

            client.debug = () => {}; // fără spam în consolă

            client.connect({}, () => {
                console.log("WebSocket connected!");

                const userId = username || "Guest";

                // 🔵 BOT PRIVAT — abonare doar la canalul userului
                client.subscribe(`/topic/bot.${userId}`, (msg: any) => {
                    if (!msg.body) return;
                    const parsed: Message = JSON.parse(msg.body);
                    setBotMessages(prev => [...prev, parsed]);
                });

                // 🔴 ADMIN (broadcast) — logică specială când adminul primește mesaje
                client.subscribe('/topic/admin', (msg: any) => {
                    if (!msg.body) return;
                    const parsed: Message = JSON.parse(msg.body);

                    if (role === "ADMIN") {
                        if (parsed.senderId !== username) {
                            // mesaj venit de la un user

                            setCurrentChatUser(prevUser => {
                                if (prevUser === null || prevUser === parsed.senderId) {
                                    // rămânem în aceeași conversație
                                    setAdminMessages(prev => [...prev, parsed]);
                                    return parsed.senderId;
                                } else {
                                    // a scris alt user → RESET chat
                                    setAdminMessages([parsed]);
                                    return parsed.senderId;
                                }
                            });

                        } else {
                            // mesaj trimis de admin → doar append
                            setAdminMessages(prev => [...prev, parsed]);
                        }
                    } else {
                        // pentru user normal
                        setAdminMessages(prev => [...prev, parsed]);
                    }
                });

            }, (err: any) => console.error("WS Error: ", err));

            clientRef.current = client;
        }
    }, [isOpen, role, username]);

    const handleSend = () => {
        if (!input.trim() || !clientRef.current) return;

        const payload: Message = {
            senderId: username || "Guest",
            content: input,
            isAdmin: role === "ADMIN",
        };

        if (activeTab === "bot") {
            clientRef.current.send("/app/chat/bot", {}, JSON.stringify(payload));
        } else {
            clientRef.current.send("/app/chat/admin", {}, JSON.stringify(payload));
        }

        setInput("");
    };

    // buton plutitor când chatul e închis
    if (!isOpen) {
        return (
            <button
                onClick={() => setIsOpen(true)}
                style={{
                    position: 'fixed', bottom: 20, right: 20,
                    width: 60, height: 60, borderRadius: '50%',
                    backgroundColor: '#007bff', color: 'white',
                    border: 'none', fontSize: '24px', cursor: 'pointer',
                    boxShadow: '0 4px 10px rgba(0,0,0,0.3)', zIndex: 9999
                }}
            >
                💬
            </button>
        );
    }

    return (
        <div style={{
            position: 'fixed', bottom: 20, right: 20,
            width: 320, height: 450, background: 'white',
            borderRadius: 12, boxShadow: '0 5px 20px rgba(0,0,0,0.2)',
            display: 'flex', flexDirection: 'column', overflow: 'hidden',
            border: '1px solid #ddd', zIndex: 9999
        }}>
            {/* Header */}
            <div style={{
                padding: '12px', background: '#007bff', color: 'white',
                display: 'flex', justifyContent: 'space-between'
            }}>
                <span style={{ fontWeight: 'bold' }}>Support Chat</span>
                <button
                    onClick={() => setIsOpen(false)}
                    style={{
                        background: 'transparent', border: 'none',
                        color: 'white', fontSize: '18px', cursor: 'pointer'
                    }}
                >
                    ✕
                </button>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', borderBottom: '1px solid #ddd' }}>
                <div
                    onClick={() => setActiveTab('bot')}
                    style={{
                        flex: 1, padding: '10px', textAlign: 'center',
                        cursor: 'pointer',
                        background: activeTab === 'bot' ? 'white' : '#f8f9fa',
                        borderBottom: activeTab === 'bot' ? '3px solid #007bff' : 'none',
                        fontWeight: activeTab === 'bot' ? 'bold' : 'normal'
                    }}
                >
                    🤖 Robot
                </div>

                <div
                    onClick={() => setActiveTab('admin')}
                    style={{
                        flex: 1, padding: '10px', textAlign: 'center',
                        cursor: 'pointer',
                        background: activeTab === 'admin' ? 'white' : '#f8f9fa',
                        borderBottom: activeTab === 'admin' ? '3px solid #007bff' : 'none',
                        fontWeight: activeTab === 'admin' ? 'bold' : 'normal'
                    }}
                >
                    👨‍💼 {currentChatName}
                </div>
            </div>

            {/* Mesaje */}
            <div
                style={{
                    flex: 1,
                    padding: "15px",
                    overflowY: "auto",
                    backgroundColor: "#fdfdfd",
                    display: "flex",
                    flexDirection: "column",
                    gap: "10px",
                }}
            >
                {displayMessages.length === 0 && (
                    <div
                        style={{
                            textAlign: "center",
                            color: "#aaa",
                            marginTop: 20,
                            fontSize: "0.9em",
                        }}
                    >
                        {activeTab === "bot"
                            ? "Say 'hello' to verify functionality."
                            : role === "ADMIN"
                                ? "Așteaptă un mesaj de la un utilizator."
                                : "Wait for an admin to reply."}
                    </div>
                )}

                {displayMessages.map((msg, idx) => {
                    const isMe = msg.senderId === username;
                    const isBotResponse = msg.senderId === "RobBot";

                    return (
                        <div
                            key={idx}
                            style={{
                                alignSelf: isMe ? "flex-end" : "flex-start",
                                maxWidth: "80%",
                                display: "flex",
                                flexDirection: "column",
                                alignItems: isMe ? "flex-end" : "flex-start",
                            }}
                        >
                <span
                    style={{
                        fontSize: "0.7em",
                        color: "#888",
                        marginBottom: 2,
                    }}
                >
                    {msg.senderId}
                </span>

                            <div
                                style={{
                                    padding: "8px 12px",
                                    borderRadius: "12px",
                                    borderBottomRightRadius: isMe ? 0 : 12,
                                    borderBottomLeftRadius: isMe ? 12 : 0,
                                    backgroundColor: isMe
                                        ? "#007bff"
                                        : isBotResponse
                                            ? "#28a745"
                                            : "#e9ecef",
                                    color: isMe || isBotResponse ? "white" : "black",
                                    boxShadow: "0 1px 2px rgba(0,0,0,0.1)",
                                }}
                            >
                                {msg.content}
                            </div>
                        </div>
                    );
                })}
            </div>


            {/* Input */}
            <div style={{
                padding: '10px', borderTop: '1px solid #ddd',
                display: 'flex', gap: '8px'
            }}>
                <input
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleSend()}
                    placeholder={
                        activeTab === 'bot'
                            ? "Ask RobBot..."
                            : role === "ADMIN"
                                ? currentChatUser
                                    ? `Message ${currentChatUser}...`
                                    : "Waiting for user..."
                                : "Message Admin..."
                    }
                    style={{
                        flex: 1, padding: '8px', borderRadius: 20,
                        border: '1px solid #ccc'
                    }}
                />
                <button
                    onClick={handleSend}
                    style={{
                        background: '#007bff', color: 'white',
                        border: 'none', borderRadius: '50%',
                        width: 35, height: 35, cursor: 'pointer'
                    }}
                >
                    ➤
                </button>
            </div>
        </div>
    );
};
