import apiClient from './axiosConfig';

export const getVehicles = async (criteria = {}, page = 0, size = 20) => {
  const response = await apiClient.get('/vehicles', {
    params: {
      ...criteria,
      page,
      size,
      sort: 'created_at,desc'
    },
  });
  return response.data;
};

export const getVehicleById = async (id) => {
  const response = await apiClient.get(`/vehicles/${id}`);
  return response.data;
};

export const createVehicle = async (data) => {
  const response = await apiClient.post('/vehicles', data);
  return response.data;
};

export const updateVehicle = async (id, data) => {
  const response = await apiClient.put(`/vehicles/${id}`, data);
  return response.data;
};

export const updateVehicleStatus = async (id, status) => {
  const response = await apiClient.patch(`/vehicles/${id}/status`, null, {
    params: { status },
  });
  return response.data;
};

export const deleteVehicle = async (id) => {
  await apiClient.delete(`/vehicles/${id}`);
};

export const uploadVehicleImage = async (id, file) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await apiClient.post(`/vehicles/${id}/image`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};
