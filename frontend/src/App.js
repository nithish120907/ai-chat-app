import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Chat from './components/Chat';

function App() {
  const isAuthenticated = () => {
    return localStorage.getItem('token') !== null;
  };

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/chat"
          element={isAuthenticated() ? <Chat /> : <Navigate to="/login" />}
        />
        <Route
          path="/"
          element={isAuthenticated() ? <Navigate to="/chat" /> : <Navigate to="/login" />}
        />
      </Routes>
    </Router>
  );
}

export default App;