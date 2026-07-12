import React from 'react';
import { Navbar } from './Navbar';

export const Layout = ({ children }) => {
  return (
    <div className="min-h-screen bg-[#0a0a0f] text-white">
      <Navbar />
      <main className="pt-16 min-h-screen bg-[#0a0a0f]">{children}</main>
    </div>
  );
};
