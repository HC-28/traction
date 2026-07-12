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
    <nav className="fixed top-0 left-0 w-full h-16 bg-white/90 backdrop-blur-md border-b border-slate-200 z-50 flex items-center justify-between px-6 shadow-sm">
      {/* Left Logo */}
      <div className="flex items-center space-x-2">
        <Link to="/" className="flex items-center hover:opacity-90 transition">
          <img src="/logo.png" alt="Traction Logo" className="h-10 object-contain" />
        </Link>
      </div>

      {/* Center Links */}
      <div className="hidden md:flex items-center space-x-6">
        {isAuthenticated && (
          <Link
            to="/dashboard"
            className="text-slate-600 hover:text-indigo-600 font-medium transition duration-200"
          >
            Dashboard
          </Link>
        )}
        {isAuthenticated && isAdmin() && (
          <Link
            to="/admin"
            className="text-slate-600 hover:text-indigo-600 font-medium transition duration-200"
          >
            Admin Panel
          </Link>
        )}
      </div>

      {/* Right Actions */}
      <div className="flex items-center space-x-4">
        {isAuthenticated ? (
          <div className="flex items-center space-x-4">
            <span className="text-slate-500 text-sm hidden sm:inline">
              Welcome, <strong className="text-slate-800">{user?.username}</strong>
            </span>
            <button
              onClick={handleLogout}
              className="bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 text-sm px-4 py-2 rounded-lg transition duration-200 font-medium"
            >
              Logout
            </button>
          </div>
        ) : (
          <div className="flex items-center space-x-3">
            <Link
              to="/login"
              className="text-slate-600 hover:text-indigo-600 text-sm font-medium px-3 py-2 transition"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="bg-indigo-600 hover:bg-indigo-500 text-white text-sm px-4 py-2 rounded-lg transition duration-200 font-medium shadow-sm shadow-indigo-200"
            >
              Register
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
};
