import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import { DashboardPage } from '../pages/DashboardPage';
import * as vehicleApi from '../api/vehicleApi';
import * as inventoryApi from '../api/inventoryApi';
import toast from 'react-hot-toast';

// Mock APIs
vi.mock('../api/vehicleApi');
vi.mock('../api/inventoryApi');

// Mock toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

const mockVehicles = [
  {
    id: 'vehicle-1',
    make: 'Toyota',
    model: 'Camry',
    year: 2022,
    color: 'White',
    vin: 'VIN00000000000001',
    price: 25000.00,
    mileage: 15000,
    status: 'AVAILABLE',
    description: 'Sleek sedan',
    imageUrl: 'https://res.cloudinary.com/test/camry.jpg',
  },
  {
    id: 'vehicle-2',
    make: 'Ford',
    model: 'Mustang',
    year: 2021,
    color: 'Red',
    vin: 'VIN00000000000002',
    price: 45000.00,
    mileage: 5000,
    status: 'AVAILABLE',
    description: 'V8 Muscle car',
    imageUrl: '',
  }
];

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (userRole = 'USER') => {
    // Mock local storage for auth context
    localStorage.setItem('traction_token', 'fake-token');
    localStorage.setItem('traction_user', JSON.stringify({ username: 'john_doe', role: userRole }));

    return render(
      <AuthProvider>
        <MemoryRouter>
          <DashboardPage />
        </MemoryRouter>
      </AuthProvider>
    );
  };

  it('renders filters, search bar, and displays list of available vehicles', async () => {
    vi.spyOn(vehicleApi, 'getVehicles').mockResolvedValue({
      success: true,
      data: {
        content: mockVehicles,
        totalPages: 1,
        totalElements: 2,
        size: 20,
        number: 0,
      },
    });

    renderComponent();

    // Verify filter elements exist
    expect(screen.getByPlaceholderText(/search make/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/search model/i)).toBeInTheDocument();

    // Verify vehicles are displayed
    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
      expect(screen.getByText('Ford Mustang')).toBeInTheDocument();
      expect(screen.getByText('$25,000.00')).toBeInTheDocument();
    });
  });

  it('allows user to click Purchase and triggers API successfully', async () => {
    vi.spyOn(vehicleApi, 'getVehicles').mockResolvedValue({
      success: true,
      data: {
        content: [mockVehicles[0]],
        totalPages: 1,
        totalElements: 1,
        size: 20,
        number: 0,
      },
    });

    vi.spyOn(inventoryApi, 'purchaseVehicle').mockResolvedValue({
      success: true,
      data: {
        purchaseId: 'p-1',
        vehicleId: 'vehicle-1',
        vehicleName: 'Toyota Camry',
        totalPrice: 25000.00,
      },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
    });

    const purchaseBtn = screen.getByRole('button', { name: /purchase/i });
    await userEvent.click(purchaseBtn);

    await waitFor(() => {
      expect(inventoryApi.purchaseVehicle).toHaveBeenCalledWith('vehicle-1');
      expect(toast.success).toHaveBeenCalledWith('Purchase successful!');
    });
  });
});
