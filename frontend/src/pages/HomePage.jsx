import React from 'react';
import { Link } from 'react-router-dom';

export const HomePage = () => {
  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col items-center justify-center px-4 relative overflow-hidden bg-[#0a0a0f]">
      {/* Background gradients */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-10 left-10 w-[300px] h-[300px] bg-purple-500/5 rounded-full blur-3xl pointer-events-none"></div>

      <div className="max-w-3xl text-center space-y-6 relative z-10">
        <div className="inline-flex items-center space-x-2 bg-white/5 border border-white/10 rounded-full px-4 py-1.5 text-xs text-blue-400 font-medium tracking-wide">
          <span>✨</span>
          <span>Next-Gen Dealership Experience</span>
        </div>

        <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-white leading-tight">
          Premium Cars. <br />
          <span className="bg-gradient-to-r from-blue-400 via-indigo-200 to-purple-400 bg-clip-text text-transparent">
            Instant Access.
          </span>
        </h1>

        <p className="text-lg text-gray-400 max-w-xl mx-auto leading-relaxed">
          Browse our curated digital showroom of luxury and performance vehicles. 
          Purchase instantly and manage showroom inventory seamlessly.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
          <Link
            to="/dashboard"
            className="w-full sm:w-auto bg-blue-600 hover:bg-blue-500 text-white font-semibold px-8 py-3.5 rounded-xl transition duration-200 shadow-xl shadow-blue-600/20 text-center"
          >
            Browse Showroom
          </Link>
          <Link
            to="/register"
            className="w-full sm:w-auto bg-white/5 hover:bg-white/10 border border-white/10 text-white font-semibold px-8 py-3.5 rounded-xl transition duration-200 text-center"
          >
            Register Account
          </Link>
        </div>
      </div>
    </div>
  );
};
