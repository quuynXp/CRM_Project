import React, { createContext, useState, useContext } from 'react';
import { post } from '@crm/network-client';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  const login = async (username, password) => {
    // Logic gọi API qua network-client
    // const res = await post('/auth/login', { username, password });
    setUser({ name: username, role: 'Admin' });
  };

  return (
    <AuthContext.Provider value={{ user, login }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
