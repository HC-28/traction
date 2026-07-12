import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { Navbar } from '../components/common/Navbar';
import { AuthProvider } from '../contexts/AuthContext';
import * as AuthContext from '../contexts/AuthContext';

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Navbar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (userValue) => {
    vi.spyOn(AuthContext, 'useAuth').mockReturnValue(userValue);
    return render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );
  };

  it('renders sign in/up options when user is not logged in', () => {
    renderComponent({
      user: null,
      isAuthenticated: false,
      isAdmin: () => false,
      logout: vi.fn(),
    });

    expect(screen.getByText('Login')).toBeInTheDocument();
    expect(screen.getByText('Register')).toBeInTheDocument();
    expect(screen.queryByText('Dashboard')).not.toBeInTheDocument();
  });

  it('renders Dashboard link and Logout button when user is logged in', () => {
    renderComponent({
      user: { username: 'john_doe', role: 'USER' },
      isAuthenticated: true,
      isAdmin: () => false,
      logout: vi.fn(),
    });

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Logout')).toBeInTheDocument();
    expect(screen.queryByText('Admin Panel')).not.toBeInTheDocument();
  });

  it('renders Admin Panel link when logged in user is an ADMIN', () => {
    renderComponent({
      user: { username: 'admin_user', role: 'ADMIN' },
      isAuthenticated: true,
      isAdmin: () => true,
      logout: vi.fn(),
    });

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Admin Panel')).toBeInTheDocument();
  });
});
