import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../context/AuthContext';

// Definim structura unui mesaj
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

    // Stocăm mesajele separat pentru fiecare tab (in-memory, se pierd la refresh)
    const [botMessages, setBotMessages] = useState<Message[]>([]);
    const [adminMessages, setAdminMessages] = useState<Message[]>([]);

    // Ref pentru clientul WebSocket
    const clientRef = useRef<any>(null);

    // Lista curentă de mesaje în funcție de tab
    const displayMessages = activeTab === 'bot' ? botMessages : adminMessages;

    useEffect(() => {
        if (isOpen && !clientRef.current) {
            // Ne conectăm prin Traefik (port 81) la endpoint-ul definit în backend
            const socket = new SockJS('http://localhost:81/chat-socket');
            const client = Stomp.over(socket);

            // Dezactivăm logurile din consolă pentru curățenie
            client.debug = () => {};

            client.connect({}, () => {
                console.log("Chat WebSocket Connected!");

                // Abonare la canalul BOT
                client.subscribe('/topic/bot', (msg: any) => {
                    if (msg.body) {
                        const parsed = JSON.parse(msg.body);
                        setBotMessages(prev => [...prev, parsed]);
                    }
                });

                // Abonare la canalul ADMIN
                client.subscribe('/topic/admin', (msg: any) => {
                    if (msg.body) {
                        const parsed = JSON.parse(msg.body);
                        setAdminMessages(prev => [...prev, parsed]);
                    }
                });

            }, (err: any) => console.error("WS Error:", err));

            clientRef.current = client;
        }
    }, [isOpen]);

    const handleSend = () => {
        if (!input.trim() || !clientRef.current) return;

        const payload = {
            senderId: username || "Guest",
            content: input,
            isAdmin: role === "ADMIN"
        };

        // Trimitem pe ruta specifică tab-ului activ
        if (activeTab === 'bot') {
            clientRef.current.send("/app/chat/bot", {}, JSON.stringify(payload));
        } else {
            clientRef.current.send("/app/chat/admin", {}, JSON.stringify(payload));
        }

        setInput("");
    };

    // Butonul plutitor (când chat-ul e închis)
    if (!isOpen) {
        return (
            <button
                onClick={() => setIsOpen(true)}
                style={{
                    position: 'fixed', bottom: 20, right: 20,
                    width: 60, height: 60, borderRadius: '50%',
                    backgroundColor: '#007bff', color: 'white', border: 'none',
                    fontSize: '24px', cursor: 'pointer', zIndex: 9999,
                    boxShadow: '0 4px 10px rgba(0,0,0,0.3)'
                }}
            >
                💬
            </button>
        );
    }

    // Fereastra de Chat
    return (
        <div style={{
            position: 'fixed', bottom: 20, right: 20,
            width: 320, height: 450, backgroundColor: 'white',
            borderRadius: 12, boxShadow: '0 5px 20px rgba(0,0,0,0.2)',
            display: 'flex', flexDirection: 'column', overflow: 'hidden', zIndex: 9999,
            fontFamily: 'sans-serif', border: '1px solid #ddd', color: 'black'
        }}>
            {/* Header */}
            <div style={{ padding: '12px', background: '#007bff', color: 'white', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{fontWeight: 'bold'}}>Support Chat</span>
                <button onClick={() => setIsOpen(false)} style={{background:'transparent', border:'none', color:'white', fontSize:'18px', cursor:'pointer'}}>✕</button>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', background: '#f8f9fa', borderBottom: '1px solid #ddd' }}>
                <div
                    onClick={() => setActiveTab('bot')}
                    style={{
                        flex: 1, padding: '10px', textAlign: 'center', cursor: 'pointer',
                        backgroundColor: activeTab === 'bot' ? 'white' : 'transparent',
                        fontWeight: activeTab === 'bot' ? 'bold' : 'normal',
                        borderBottom: activeTab === 'bot' ? '3px solid #007bff' : 'none',
                        color: 'black'
                    }}
                >
                    🤖 Robot
                </div>
                <div
                    onClick={() => setActiveTab('admin')}
                    style={{
                        flex: 1, padding: '10px', textAlign: 'center', cursor: 'pointer',
                        backgroundColor: activeTab === 'admin' ? 'white' : 'transparent',
                        fontWeight: activeTab === 'admin' ? 'bold' : 'normal',
                        borderBottom: activeTab === 'admin' ? '3px solid #007bff' : 'none',
                        color: 'black'
                    }}
                >
                    👨‍💼 Admin
                </div>
            </div>

            {/* Zona de Mesaje */}
            <div style={{ flex: 1, padding: '15px', overflowY: 'auto', backgroundColor: '#fdfdfd', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {displayMessages.length === 0 && (
                    <div style={{textAlign: 'center', color: '#aaa', marginTop: 20, fontSize: '0.9em'}}>
                        {activeTab === 'bot' ? "Say 'hello' to verify functionality." : "Wait for an admin to reply."}
                    </div>
                )}

                {displayMessages.map((msg, idx) => {
                    const isMe = msg.senderId === username;
                    // Identificăm dacă e un mesaj "sistem" (bot sau admin pe celălalt fir)
                    const isBotResponse = msg.senderId === 'RobBot';

                    return (
                        <div key={idx} style={{
                            alignSelf: isMe ? 'flex-end' : 'flex-start',
                            maxWidth: '80%',
                            display: 'flex', flexDirection: 'column', alignItems: isMe ? 'flex-end' : 'flex-start'
                        }}>
                            <span style={{ fontSize: '0.7em', color: '#888', marginBottom: 2 }}>
                                {msg.senderId}
                            </span>
                            <div style={{
                                padding: '8px 12px',
                                borderRadius: '12px',
                                borderBottomRightRadius: isMe ? 0 : 12,
                                borderBottomLeftRadius: isMe ? 12 : 0,
                                backgroundColor: isMe ? '#007bff' : (isBotResponse ? '#28a745' : '#e9ecef'),
                                color: isMe || isBotResponse ? 'white' : 'black',
                                boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
                            }}>
                                {msg.content}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Zona de Input */}
            <div style={{ padding: '10px', borderTop: '1px solid #ddd', display: 'flex', gap: '8px', background: 'white' }}>
                <input
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                    placeholder={activeTab === 'bot' ? "Ask RobBot..." : "Message Admin..."}
                    style={{ flex: 1, padding: '8px', borderRadius: '20px', border: '1px solid #ccc', outline: 'none', paddingLeft: '12px', backgroundColor: 'white', color: 'black' }}
                />
                <button
                    onClick={handleSend}
                    style={{ backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '50%', width: '35px', height: '35px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                    ➤
                </button>
            </div>
        </div>
    );
};