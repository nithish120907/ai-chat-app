import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import '../styles/Login.css';

function Login() {
  const [isRegister, setIsRegister] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      if (isRegister) {
        await axios.post('http://localhost:8080/auth/register', {
          username,
          password
        });
        setSuccess('Account created! Please sign in.');
        setIsRegister(false);
        setUsername('');
        setPassword('');
      } else {
        const res = await axios.post('http://localhost:8080/auth/login', {
          username,
          password
        });
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('username', username); // ✅ use input field value
        navigate('/chat');
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (isRegister ? 'Registration failed' : 'Invalid username or password')
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>💬 AI Chat App</h1>
          <p>{isRegister ? 'Create an account' : 'Sign in to continue'}</p>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Username</label>
            <input
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="input-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {error && <div className="error-msg">{error}</div>}
          {success && <div className="success-msg">{success}</div>}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? 'Please wait...' : isRegister ? 'Register' : 'Sign In'}
          </button>
        </form>

        <div className="toggle-auth">
          {isRegister ? (
            <p>Already have an account?{' '}
              <span onClick={() => { setIsRegister(false); setError(''); setSuccess(''); }}>
                Sign In
              </span>
            </p>
          ) : (
            <p>Don't have an account?{' '}
              <span onClick={() => { setIsRegister(true); setError(''); setSuccess(''); }}>
                Register
              </span>
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

export default Login;