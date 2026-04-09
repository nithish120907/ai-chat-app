import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';

function ChatWindow({ currentUser, contact, stompClient }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const messagesEndRef = useRef(null);
  const subscriptionsRef = useRef([]);
  const currentContactRef = useRef(contact);

  useEffect(() => {
    currentContactRef.current = contact;
  }, [contact]);

  // Load chat history
  useEffect(() => {
    if (!currentUser) return;
    setMessages([]);
    setSuggestions([]);

    const token = localStorage.getItem('token');
    axios.get('http://localhost:8080/messages/history', {
      params: {
        user1: currentUser.toLowerCase(),
        user2: contact.name.toLowerCase()
      },
      headers: { Authorization: `Bearer ${token}` }
    })
    .then((res) => {
      console.log('📜 History:', res.data.length, 'messages');
      setMessages(res.data);
    })
    .catch((err) => console.log('❌ History error:', err.message));
  }, [contact, currentUser]);

  // Subscribe to messages
  useEffect(() => {
    if (!stompClient || !currentUser) return;

    // Clear old subscriptions
    subscriptionsRef.current.forEach(sub => {
      try { sub.unsubscribe(); } catch (e) {}
    });
    subscriptionsRef.current = [];

    const handleMessage = (msg) => {
      console.log('📩 Received:', msg.body);
      const body = JSON.parse(msg.body);

      const activeContact = currentContactRef.current;
      const senderLower = (body.sender || '').toLowerCase();
      const receiverLower = (body.receiver || '').toLowerCase();
      const currentUserLower = currentUser.toLowerCase();
      const contactNameLower = activeContact.name.toLowerCase();

      const isRelevant =
        (senderLower === contactNameLower && receiverLower === currentUserLower) ||
        (senderLower === currentUserLower && receiverLower === contactNameLower) ||
        (senderLower === '🤖 ai bot' && contactNameLower === 'bot') ||
        (receiverLower === 'bot' && contactNameLower === 'bot');

      console.log('🔍 Relevant:', isRelevant, 'sender:', senderLower, 'receiver:', receiverLower);

      if (isRelevant) {
        // ✅ Prevent duplicates
        setMessages((prev) => {
          const isDuplicate = prev.some(
            m => m.sender === body.sender &&
                 m.content === body.content &&
                 m.timestamp === body.timestamp
          );
          if (isDuplicate) return prev;
          return [...prev, body];
        });
      }

      if (body.suggestions && body.suggestions.length > 0) {
        setSuggestions(body.suggestions);
      } else {
        setSuggestions([]);
      }
    };

    // ✅ Subscribe to private queue
    const sub1 = stompClient.subscribe('/user/queue/messages', handleMessage);
    subscriptionsRef.current.push(sub1);
    console.log('📡 Subscribed to /user/queue/messages');

    // ✅ Subscribe to personal topic as fallback
    const personalTopic = `/topic/chat.${currentUser.toLowerCase()}`;
    const sub2 = stompClient.subscribe(personalTopic, handleMessage);
    subscriptionsRef.current.push(sub2);
    console.log('📡 Subscribed to', personalTopic);

    return () => {
      subscriptionsRef.current.forEach(sub => {
        try { sub.unsubscribe(); } catch (e) {}
      });
    };
  }, [stompClient, currentUser]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = (content) => {
    if (!content.trim()) return;
    if (!stompClient) {
      console.log('❌ Not connected!');
      return;
    }

    stompClient.publish({
      destination: '/app/chat',
      body: JSON.stringify({
        sender: currentUser.toLowerCase(),
        receiver: contact.name.toLowerCase(),
        content: content,
      }),
    });

    console.log('📤 Sent:', currentUser, '→', contact.name);
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
              {stompClient ? '🟢 Online' : '🔴 Connecting...'}
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
              (msg.sender || '').toLowerCase() === currentUser.toLowerCase()
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
          disabled={!stompClient}
        >
          ➤
        </button>
      </div>
    </div>
  );
}

export default ChatWindow;