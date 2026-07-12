import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('traction_token');
    const storedUserJson = localStorage.getItem('traction_user');

    if (storedToken && storedUserJson) {
      try {
        const storedUser = JSON.parse(storedUserJson);
        setUser({
          ...storedUser,
          token: storedToken,
        });
      } catch (e) {
        localStorage.removeItem('traction_token');
        localStorage.removeItem('traction_user');
      }
    }
    setLoading(false);
  }, []);

  const login = (newUser) => {
    setUser(newUser);
    localStorage.setItem('traction_token', newUser.token);
    localStorage.setItem('traction_user', JSON.stringify({
      username: newUser.username,
      role: newUser.role,
    }));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('traction_token');
    localStorage.removeItem('traction_user');
  };

  const isAdmin = () => {
    return user?.role === 'ADMIN';
  };

  const isAuthenticated = !!user;

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin, isAuthenticated, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
