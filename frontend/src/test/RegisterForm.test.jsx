import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { RegisterForm } from '../components/auth/RegisterForm';
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

describe('RegisterForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = () => {
    return render(
      <MemoryRouter>
        <RegisterForm />
      </MemoryRouter>
    );
  };

  it('renders username, email, password inputs and a register button', () => {
    renderComponent();
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });

  it('calls authApi.register and redirects to login on successful registration', async () => {
    const fakeResponse = {
      success: true,
      message: 'User registered successfully',
    };
    vi.spyOn(authApi, 'register').mockResolvedValue(fakeResponse);

    renderComponent();

    await userEvent.type(screen.getByLabelText(/username/i), 'newuser');
    await userEvent.type(screen.getByLabelText(/email/i), 'newuser@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
      });
      expect(toast.success).toHaveBeenCalledWith('Registration successful! Please log in.');
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('shows error toast when username/email is already taken', async () => {
    const errorResponse = {
      response: {
        status: 409,
        data: { message: 'Username or email already taken' },
      },
    };
    vi.spyOn(authApi, 'register').mockRejectedValue(errorResponse);

    renderComponent();

    await userEvent.type(screen.getByLabelText(/username/i), 'takenuser');
    await userEvent.type(screen.getByLabelText(/email/i), 'taken@example.com');
    await userEvent.type(screen.getByLabelText(/password/i), 'password123');
    await userEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Username or email already taken');
    });
  });
});
