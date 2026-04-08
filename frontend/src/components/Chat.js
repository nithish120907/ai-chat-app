import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('username');
    if (!token) {
      navigate('/login');
    } else {
      setUsername(user);
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  return (
    <div className="chat-container">
      {/* Sidebar */}
      <div className="sidebar">
        <div className="sidebar-header">
          <div className="user-info">
            <div className="avatar">{username.charAt(0).toUpperCase()}</div>
            <span className="username">{username}</span>
          </div>
          <button className="logout-btn" onClick={handleLogout}>Logout</button>
        </div>
        <div className="contacts-label">Chats</div>
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
                  {contact.type === 'bot' ? 'AI powered' : 'Online'}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Chat Window */}
      <ChatWindow
        currentUser={username}
        contact={selectedContact}
      />
    </div>
  );
}

export default Chat;