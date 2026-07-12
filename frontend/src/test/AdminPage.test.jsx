import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../contexts/AuthContext';
import { AdminPage } from '../pages/AdminPage';
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
    status: 'SOLD',
    description: 'Sleek sedan',
    imageUrl: '',
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
    description: 'Muscle car',
    imageUrl: '',
  }
];

describe('AdminPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = () => {
    localStorage.setItem('traction_token', 'admin-token');
    localStorage.setItem('traction_user', JSON.stringify({ username: 'admin_user', role: 'ADMIN' }));

    return render(
      <AuthProvider>
        <MemoryRouter>
          <AdminPage />
        </MemoryRouter>
      </AuthProvider>
    );
  };

  it('renders vehicles table with status badges and restock actions', async () => {
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

    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
      expect(screen.getByText('Ford Mustang')).toBeInTheDocument();
      expect(screen.getByText('SOLD')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /restock/i })).toBeInTheDocument();
    });
  });

  it('allows admin to restock a SOLD vehicle', async () => {
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

    vi.spyOn(inventoryApi, 'restockVehicle').mockResolvedValue({
      success: true,
      data: {
        ...mockVehicles[0],
        status: 'AVAILABLE',
      },
    });

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
    });

    const restockBtn = screen.getByRole('button', { name: /restock/i });
    await userEvent.click(restockBtn);

    await waitFor(() => {
      expect(inventoryApi.restockVehicle).toHaveBeenCalledWith('vehicle-1');
      expect(toast.success).toHaveBeenCalledWith('Vehicle restocked successfully!');
    });
  });

  it('allows admin to delete a vehicle listing', async () => {
    vi.spyOn(vehicleApi, 'getVehicles').mockResolvedValue({
      success: true,
      data: {
        content: [mockVehicles[1]],
        totalPages: 1,
        totalElements: 1,
        size: 20,
        number: 0,
      },
    });

    vi.spyOn(vehicleApi, 'deleteVehicle').mockResolvedValue();
    window.confirm = vi.fn(() => true);

    renderComponent();

    await waitFor(() => {
      expect(screen.getByText('Ford Mustang')).toBeInTheDocument();
    });

    const deleteBtn = screen.getByRole('button', { name: /delete/i });
    await userEvent.click(deleteBtn);

    await waitFor(() => {
      expect(vehicleApi.deleteVehicle).toHaveBeenCalledWith('vehicle-2');
      expect(toast.success).toHaveBeenCalledWith('Vehicle deleted successfully!');
    });
  });
});
