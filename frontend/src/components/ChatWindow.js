import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import axios from 'axios';

function ChatWindow({ currentUser, contact }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const connectedRef = useRef(false);

  // Load chat history when contact changes
  useEffect(() => {
    setMessages([]);
    setSuggestions([]);

    const token = localStorage.getItem('token');

    axios.get('http://localhost:8080/messages/history', {
      params: {
        user1: currentUser,
        user2: contact.name
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    .then((res) => {
      console.log('📜 History loaded:', res.data.length, 'messages');
      setMessages(res.data);
    })
    .catch((err) => {
      console.log('❌ History error:', err.message);
    });

  }, [contact, currentUser]);

  useEffect(() => {
    const token = localStorage.getItem('token');

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 3000,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      onConnect: () => {
        connectedRef.current = true;
        setConnected(true);
        console.log('✅ WebSocket connected');

        // ✅ Subscribe to private messages only for this user
        client.subscribe('/user/queue/messages', (msg) => {
          console.log('📩 Received:', msg.body);
          const body = JSON.parse(msg.body);
          setMessages((prev) => [...prev, body]);
          if (body.suggestions && body.suggestions.length > 0) {
            setSuggestions(body.suggestions);
          } else {
            setSuggestions([]);
          }
        });
      },
      onDisconnect: () => {
        connectedRef.current = false;
        setConnected(false);
        console.log('❌ WebSocket disconnected');
      },
      onStompError: (frame) => {
        console.error('❌ STOMP error:', frame);
      }
    });

    client.activate();
    clientRef.current = client;

    return () => client.deactivate();
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = (content) => {
    if (!content.trim()) return;
    if (!clientRef.current || !connectedRef.current) {
      console.log('❌ Not connected!');
      return;
    }

    clientRef.current.publish({
      destination: '/app/chat',
      body: JSON.stringify({
        sender: currentUser,
        receiver: contact.name,
        content: content,
      }),
    });

    console.log('📤 Sent to /app/chat');
    setInput('');
    setSuggestions([]);
  };

  return (
    <div className="chat-window">
      <div className="chat-header">
        <div className="chat-header-info">
          <div className="chat-avatar">{contact.label.charAt(0)}</div>
          <div>
            <div className="chat-name">{contact.label}</div>
            <div className="chat-status">
              {connected ? '🟢 Online' : '🔴 Connecting...'}
            </div>
          </div>
        </div>
      </div>

      <div className="messages-area">
        {messages.length === 0 && (
          <div className="empty-chat">
            <p>Start a conversation with {contact.label}</p>
          </div>
        )}
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`message ${
              msg.sender === currentUser
                ? 'sent'
                : msg.sender === '🤖 AI Bot'
                ? 'bot'
                : 'received'
            }`}
          >
            <div className="message-sender">{msg.sender}</div>
            <div className="message-content">{msg.content}</div>
            <div className="message-time">
              {msg.timestamp
                ? new Date(msg.timestamp).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                  })
                : ''}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {suggestions.length > 0 && (
        <div className="suggestions-bar">
          <span className="suggestions-label">💡 Suggestions:</span>
          {suggestions.map((s, i) => (
            <button
              key={i}
              className="suggestion-btn"
              onClick={() => sendMessage(s)}
            >
              {s}
            </button>
          ))}
        </div>
      )}

      <div className="input-area">
        <input
          type="text"
          placeholder="Type a message..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && sendMessage(input)}
        />
        <button
          className="send-btn"
          onClick={() => sendMessage(input)}
          disabled={!connected}
        >
          ➤
        </button>
      </div>
    </div>
  );
}

export default ChatWindow;