import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

export const Navbar = () => {
  const { user, logout, isAdmin, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="fixed top-0 left-0 w-full h-16 bg-black/40 backdrop-blur-md border-b border-white/10 z-50 flex items-center justify-between px-6 text-white">
      {/* Left Logo */}
      <div className="flex items-center space-x-2">
        <Link to="/" className="text-xl font-bold tracking-wider flex items-center gap-2 hover:opacity-90 transition">
          <span className="text-2xl">🚗</span>
          <span className="bg-gradient-to-r from-white via-gray-200 to-blue-400 bg-clip-text text-transparent">
            Traction
          </span>
        </Link>
      </div>

      {/* Center Links */}
      <div className="hidden md:flex items-center space-x-6">
        {isAuthenticated && (
          <Link
            to="/dashboard"
            className="text-gray-300 hover:text-white transition duration-200"
          >
            Dashboard
          </Link>
        )}
        {isAuthenticated && isAdmin() && (
          <Link
            to="/admin"
            className="text-gray-300 hover:text-white transition duration-200"
          >
            Admin Panel
          </Link>
        )}
      </div>

      {/* Right Actions */}
      <div className="flex items-center space-x-4">
        {isAuthenticated ? (
          <div className="flex items-center space-x-4">
            <span className="text-gray-300 text-sm hidden sm:inline">
              Welcome, <strong className="text-white">{user?.username}</strong>
            </span>
            <button
              onClick={handleLogout}
              className="bg-red-600/80 hover:bg-red-600 text-white text-sm px-4 py-2 rounded-lg transition duration-200 font-medium"
            >
              Logout
            </button>
          </div>
        ) : (
          <div className="flex items-center space-x-3">
            <Link
              to="/login"
              className="text-gray-300 hover:text-white text-sm font-medium px-3 py-2 transition"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="bg-blue-600 hover:bg-blue-500 text-white text-sm px-4 py-2 rounded-lg transition duration-200 font-medium shadow-lg shadow-blue-600/25"
            >
              Register
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
};
