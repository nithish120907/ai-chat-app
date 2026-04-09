import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import ChatWindow from './ChatWindow';
import '../styles/Chat.css';

const CONTACTS = [
  { name: 'bot', label: '🤖 AI Bot', type: 'bot' },
  { name: 'nithish', label: '👤 Nithish', type: 'person' },
  { name: 'alice', label: '👤 Alice', type: 'person' },
  { name: 'bob', label: '👤 Bob', type: 'person' },
];

function Chat() {
  const [selectedContact, setSelectedContact] = useState(CONTACTS[0]);
  const [username, setUsername] = useState('');
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const navigate = useNavigate();
  const clientRef = useRef(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('username');
    if (!token) {
      navigate('/login');
      return;
    }

    // ✅ Normalize username to lowercase
    const normalizedUser = user ? user.toLowerCase() : '';
    setUsername(normalizedUser);

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 3000,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      onConnect: () => {
        console.log('✅ WebSocket connected as:', normalizedUser);

        // Subscribe to online users
        client.subscribe('/topic/online-users', (msg) => {
          try {
            const data = JSON.parse(msg.body);
            // ✅ Normalize all online users to lowercase
            const users = Array.isArray(data)
              ? data.map(u => u.toLowerCase())
              : Array.from(data).map(u => u.toLowerCase());
            console.log('👥 Online users:', users);
            setOnlineUsers(users);
          } catch (e) {
            console.log('❌ Error parsing online users:', e);
          }
        });

        // ✅ Set client after fully connected
        clientRef.current = client;
        setStompClient(client);
      },
      onDisconnect: () => {
        console.log('❌ WebSocket disconnected');
        clientRef.current = null;
        setStompClient(null);
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error:', frame);
      }
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  const isOnline = (contactName) => {
    if (contactName === 'bot') return true;
    // ✅ Compare lowercase
    return onlineUsers.includes(contactName.toLowerCase());
  };

  return (
    <div className="chat-container">
      <div className="sidebar">
        <div className="sidebar-header">
          <div className="user-info">
            <div className="avatar">{username.charAt(0).toUpperCase()}</div>
            <span className="username">{username}</span>
          </div>
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </div>
        <div className="contacts-label">CHATS</div>
        <div className="contacts-list">
          {CONTACTS.map((contact) => (
            <div
              key={contact.name}
              className={`contact-item ${selectedContact.name === contact.name ? 'active' : ''}`}
              onClick={() => setSelectedContact(contact)}
            >
              <div className="contact-avatar">
                {contact.label.charAt(0)}
              </div>
              <div className="contact-info">
                <div className="contact-name">{contact.label}</div>
                <div className="contact-sub">
                  {isOnline(contact.name) ? (
                    <span className="online-dot">🟢 Online</span>
                  ) : (
                    <span className="offline-dot">⚫ Offline</span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <ChatWindow
        currentUser={username}
        contact={selectedContact}
        stompClient={stompClient}
      />
    </div>
  );
}

export default Chat;