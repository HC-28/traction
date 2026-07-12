import apiClient from './axiosConfig';

export const purchaseVehicle = async (vehicleId) => {
  const response = await apiClient.post(`/vehicles/${vehicleId}/purchase`);
  return response.data;
};

export const restockVehicle = async (vehicleId) => {
  const response = await apiClient.post(`/vehicles/${vehicleId}/restock`);
  return response.data;
};
