import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import { LoginForm } from '../components/auth/LoginForm';
import * as authApi from '../api/authApi';
import toast from 'react-hot-toast';

// Mock authApi
vi.mock('../api/authApi');

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  const renderComponent = () => {
    return render(
      <AuthProvider>
        <MemoryRouter>
          <LoginForm />
        </MemoryRouter>
      </AuthProvider>
    );
  };

  it('renders username and password inputs and a submit button', () => {
    renderComponent();
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('calls authApi.login and redirects to dashboard on successful login', async () => {
    const fakeResponse = {
      success: true,
      data: {
        token: 'fake-jwt-token',
        username: 'testuser',
        role: 'USER',
      },
    };
    vi.spyOn(authApi, 'login').mockResolvedValue(fakeResponse);

    renderComponent();

    await userEvent.type(screen.getByLabelText(/username/i), 'testuser');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      });
      expect(localStorage.getItem('traction_token')).toBe('fake-jwt-token');
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('shows error toast when login API returns unauthorized/invalid credentials', async () => {
    const errorResponse = {
      response: {
        status: 401,
        data: { message: 'Invalid credentials' },
      },
    };
    vi.spyOn(authApi, 'login').mockRejectedValue(errorResponse);

    renderComponent();

    await userEvent.type(screen.getByLabelText(/username/i), 'wronguser');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrongpass');
    await userEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Invalid credentials');
    });
  });
});
