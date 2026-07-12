import React, { useState, useEffect } from 'react';
import { getVehicles, createVehicle, updateVehicle, deleteVehicle, uploadVehicleImage } from '../api/vehicleApi';
import { restockVehicle } from '../api/inventoryApi';
import toast from 'react-hot-toast';

export const AdminPage = () => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [fileToUpload, setFileToUpload] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Form State
  const [make, setMake] = useState('');
  const [model, setModel] = useState('');
  const [year, setYear] = useState('');
  const [color, setColor] = useState('');
  const [vin, setVin] = useState('');
  const [price, setPrice] = useState('');
  const [mileage, setMileage] = useState('');
  const [description, setDescription] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchAllVehicles = async () => {
    setLoading(true);
    try {
      // Admins get ALL vehicles without filtering out SOLD/RESERVED
      const response = await getVehicles({}, page, 10);
      if (response.success) {
        setVehicles(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (error) {
      toast.error('Failed to load vehicles');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllVehicles();
  }, [page]);

  const handleRestock = async (id) => {
    try {
      const response = await restockVehicle(id);
      if (response.success) {
        toast.success('Vehicle restocked successfully!');
        fetchAllVehicles();
      }
    } catch (error) {
      toast.error('Restock failed');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this vehicle listing?')) return;
    try {
      await deleteVehicle(id);
      toast.success('Vehicle deleted successfully!');
      fetchAllVehicles();
    } catch (error) {
      toast.error('Delete failed');
    }
  };

  const resetForm = () => {
    setMake('');
    setModel('');
    setYear('');
    setColor('');
    setVin('');
    setPrice('');
    setMileage('');
    setDescription('');
    setFileToUpload(null);
    setSelectedVehicle(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!make || !model || !year || !color || !vin || !price) {
      toast.error('Please fill in all required fields');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        make,
        model,
        year: parseInt(year, 10),
        color,
        vin,
        price: parseFloat(price),
        mileage: mileage ? parseInt(mileage, 10) : 0,
        description,
      };

      let vehicleResponse;
      if (selectedVehicle) {
        vehicleResponse = await updateVehicle(selectedVehicle.id, payload);
        toast.success('Vehicle updated successfully!');
      } else {
        vehicleResponse = await createVehicle(payload);
        toast.success('Vehicle created successfully!');
      }

      // If an image is selected, upload it now
      if (fileToUpload && vehicleResponse.success) {
        toast.loading('Uploading image...', { id: 'image-upload' });
        await uploadVehicleImage(vehicleResponse.data.id, fileToUpload);
        toast.success('Image uploaded successfully!', { id: 'image-upload' });
      }

      setShowAddModal(false);
      resetForm();
      fetchAllVehicles();
    } catch (error) {
      const msg = error.response?.data?.message || 'Action failed';
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const openEditModal = (vehicle) => {
    setSelectedVehicle(vehicle);
    setMake(vehicle.make);
    setModel(vehicle.model);
    setYear(vehicle.year.toString());
    setColor(vehicle.color);
    setVin(vehicle.vin);
    setPrice(vehicle.price.toString());
    setMileage(vehicle.mileage.toString());
    setDescription(vehicle.description || '');
    setShowAddModal(true);
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-slate-900">Inventory Management</h1>
            <p className="text-sm text-slate-500 mt-1">Admin control panel for showroom listings</p>
          </div>
          <button
            onClick={() => { resetForm(); setShowAddModal(true); }}
            className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm px-5 py-2.5 rounded-xl transition duration-200 shadow-md shadow-indigo-600/10"
          >
            Add Listing
          </button>
        </div>

        {/* Listings Table Card */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-indigo-600"></div>
          </div>
        ) : vehicles.length === 0 ? (
          <div className="text-center py-16 bg-white border border-slate-200/80 rounded-2xl shadow-sm">
            <span className="text-4xl">🚗</span>
            <h3 className="text-lg font-semibold text-slate-800 mt-4">Showroom Empty</h3>
            <p className="text-slate-500 text-sm mt-1">Create a new vehicle listing to get started.</p>
          </div>
        ) : (
          <div className="bg-white border border-slate-200/80 rounded-2xl shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-400 text-[10px] uppercase font-bold tracking-wider">
                    <th className="px-6 py-4">Vehicle Details</th>
                    <th className="px-6 py-4">VIN</th>
                    <th className="px-6 py-4">Status</th>
                    <th className="px-6 py-4">Price</th>
                    <th className="px-6 py-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-sm">
                  {vehicles.map((vehicle) => (
                    <tr key={vehicle.id} className="hover:bg-slate-50/50 transition">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <span className="text-2xl">{vehicle.imageUrl ? '🖼️' : '🚗'}</span>
                          <div>
                            <div className="font-semibold text-slate-900">
                              {vehicle.make} {vehicle.model}
                            </div>
                            <div className="text-xs text-slate-500">
                              {vehicle.year} • {vehicle.color} • {vehicle.mileage.toLocaleString()} mi
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 font-mono text-xs text-slate-600">{vehicle.vin}</td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${
                          vehicle.status === 'AVAILABLE'
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200/50'
                            : vehicle.status === 'SOLD'
                            ? 'bg-rose-50 text-rose-700 border-rose-200/50'
                            : 'bg-amber-50 text-amber-700 border-amber-200/50'
                        }`}>
                          {vehicle.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 font-bold text-slate-900">
                        ${vehicle.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </td>
                      <td className="px-6 py-4 text-right space-x-2">
                        {vehicle.status !== 'AVAILABLE' && (
                          <button
                            onClick={() => handleRestock(vehicle.id)}
                            className="bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs px-3 py-1.5 rounded-lg font-semibold transition"
                          >
                            Restock
                          </button>
                        )}
                        <button
                          onClick={() => openEditModal(vehicle)}
                          className="bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs px-3 py-1.5 rounded-lg font-semibold transition"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDelete(vehicle.id)}
                          className="bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs px-3 py-1.5 rounded-lg font-semibold transition"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Modal Dialog */}
        {showAddModal && (
          <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4 z-50">
            <div className="bg-white border border-slate-200 rounded-2xl max-w-lg w-full p-6 shadow-2xl relative text-slate-800">
              <button
                onClick={() => setShowAddModal(false)}
                className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 transition"
              >
                ✕
              </button>
              
              <h2 className="text-xl font-bold text-slate-900 mb-6">
                {selectedVehicle ? 'Edit Vehicle Listing' : 'Add Showroom Vehicle'}
              </h2>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">Make *</label>
                    <input
                      type="text"
                      required
                      value={make}
                      onChange={(e) => setMake(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">Model *</label>
                    <input
                      type="text"
                      required
                      value={model}
                      onChange={(e) => setModel(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">Year *</label>
                    <input
                      type="number"
                      required
                      value={year}
                      onChange={(e) => setYear(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">Color *</label>
                    <input
                      type="text"
                      required
                      value={color}
                      onChange={(e) => setColor(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">VIN *</label>
                    <input
                      type="text"
                      required
                      disabled={!!selectedVehicle}
                      value={vin}
                      onChange={(e) => setVin(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm disabled:opacity-50"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">Price *</label>
                    <input
                      type="number"
                      required
                      value={price}
                      onChange={(e) => setPrice(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-600 mb-1">Mileage</label>
                    <input
                      type="number"
                      value={mileage}
                      onChange={(e) => setMileage(e.target.value)}
                      className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-600 mb-1">Description</label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 text-sm h-20 resize-none"
                  />
                </div>

                {/* Cloudinary Image Picker */}
                <div>
                  <label className="block text-xs font-bold text-slate-600 mb-1">Vehicle Image (Cloudinary)</label>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => setFileToUpload(e.target.files[0])}
                    className="w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 transition"
                  />
                </div>

                <div className="pt-4 flex justify-end gap-3">
                  <button
                    type="button"
                    onClick={() => setShowAddModal(false)}
                    className="bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold px-4 py-2 rounded-xl transition text-sm"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold px-6 py-2 rounded-xl transition text-sm disabled:opacity-50"
                  >
                    {submitting ? 'Saving...' : 'Save Listing'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};
