import React, { useState, useEffect } from 'react';
import { getVehicles } from '../api/vehicleApi';
import { purchaseVehicle } from '../api/inventoryApi';
import { useAuth } from '../contexts/AuthContext';
import toast from 'react-hot-toast';

export const DashboardPage = () => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [buyingId, setBuyingId] = useState(null);

  // Filters State
  const [make, setMake] = useState('');
  const [model, setModel] = useState('');
  const [year, setYear] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const { user } = useAuth();

  const fetchVehicles = async () => {
    setLoading(true);
    try {
      const criteria = {};
      if (make) criteria.make = make;
      if (model) criteria.model = model;
      if (year) criteria.year = parseInt(year, 10);
      if (minPrice) criteria.minPrice = parseFloat(minPrice);
      if (maxPrice) criteria.maxPrice = parseFloat(maxPrice);
      // Showroom dashboard defaults to showing AVAILABLE stock
      criteria.status = 'AVAILABLE';

      const response = await getVehicles(criteria, page, 8);
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
    fetchVehicles();
  }, [page, make, model, year, minPrice, maxPrice]);

  const handlePurchase = async (vehicleId) => {
    setBuyingId(vehicleId);
    try {
      const response = await purchaseVehicle(vehicleId);
      if (response.success) {
        toast.success('Purchase successful!');
        // Refresh listings after purchase
        fetchVehicles();
      }
    } catch (error) {
      const msg = error.response?.data?.message || 'Purchase failed';
      toast.error(msg);
    } finally {
      setBuyingId(null);
    }
  };

  const handleClearFilters = () => {
    setMake('');
    setModel('');
    setYear('');
    setMinPrice('');
    setMaxPrice('');
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-slate-900">Showroom Listings</h1>
            <p className="text-sm text-slate-500 mt-1">Browse and purchase premium available vehicles</p>
          </div>
          <button
            onClick={handleClearFilters}
            className="text-sm font-semibold text-indigo-600 hover:text-indigo-500 transition px-3 py-1.5 rounded-lg hover:bg-indigo-50"
          >
            Clear All Filters
          </button>
        </div>

        {/* Filter Card */}
        <div className="bg-white border border-slate-200/80 rounded-2xl p-6 shadow-sm shadow-slate-100/50 space-y-4">
          <h2 className="text-sm font-semibold text-slate-700 uppercase tracking-wider">Search & Filters</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-4">
            <div>
              <input
                type="text"
                placeholder="Search Make"
                value={make}
                onChange={(e) => { setMake(e.target.value); setPage(0); }}
                className="w-full px-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-sm"
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="Search Model"
                value={model}
                onChange={(e) => { setModel(e.target.value); setPage(0); }}
                className="w-full px-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-sm"
              />
            </div>
            <div>
              <input
                type="number"
                placeholder="Year"
                value={year}
                onChange={(e) => { setYear(e.target.value); setPage(0); }}
                className="w-full px-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-sm"
              />
            </div>
            <div>
              <input
                type="number"
                placeholder="Min Price"
                value={minPrice}
                onChange={(e) => { setMinPrice(e.target.value); setPage(0); }}
                className="w-full px-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-sm"
              />
            </div>
            <div>
              <input
                type="number"
                placeholder="Max Price"
                value={maxPrice}
                onChange={(e) => { setMaxPrice(e.target.value); setPage(0); }}
                className="w-full px-3 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-sm"
              />
            </div>
          </div>
        </div>

        {/* Listings Grid */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-indigo-600"></div>
          </div>
        ) : vehicles.length === 0 ? (
          <div className="text-center py-16 bg-white border border-slate-200/80 rounded-2xl shadow-sm">
            <span className="text-4xl">🔍</span>
            <h3 className="text-lg font-semibold text-slate-800 mt-4">No Vehicles Found</h3>
            <p className="text-slate-500 text-sm mt-1">Try adjusting your filters or search terms.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {vehicles.map((vehicle) => (
              <div
                key={vehicle.id}
                className="bg-white border border-slate-200/80 rounded-2xl overflow-hidden shadow-sm hover:shadow-lg transition-all duration-300 flex flex-col group"
              >
                {/* Image Section */}
                <div className="aspect-[16/10] bg-slate-100 relative overflow-hidden flex items-center justify-center">
                  {vehicle.imageUrl ? (
                    <img
                      src={vehicle.imageUrl}
                      alt={`${vehicle.make} ${vehicle.model}`}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                  ) : (
                    <div className="flex flex-col items-center text-slate-400 select-none">
                      <span className="text-3xl">🚗</span>
                      <span className="text-xs font-semibold mt-2">No Photo Available</span>
                    </div>
                  )}
                  {/* Status Badge */}
                  <span className="absolute top-3 right-3 bg-emerald-50 text-emerald-700 text-xs font-bold px-2.5 py-1 rounded-full border border-emerald-200/50">
                    AVAILABLE
                  </span>
                </div>

                {/* Content Section */}
                <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                  <div>
                    <h3 className="font-bold text-lg text-slate-900 group-hover:text-indigo-600 transition">
                      {vehicle.make} {vehicle.model}
                    </h3>
                    <p className="text-xs text-slate-500 font-semibold tracking-wider uppercase mt-0.5">
                      VIN: {vehicle.vin}
                    </p>
                    <div className="grid grid-cols-2 gap-4 mt-3 text-xs font-medium text-slate-600 border-t border-slate-100 pt-3">
                      <div>
                        <span className="text-slate-400 block text-[10px] uppercase font-bold tracking-wider">Year</span>
                        <span className="text-slate-800 text-sm font-semibold">{vehicle.year}</span>
                      </div>
                      <div>
                        <span className="text-slate-400 block text-[10px] uppercase font-bold tracking-wider">Mileage</span>
                        <span className="text-slate-800 text-sm font-semibold">{vehicle.mileage.toLocaleString()} mi</span>
                      </div>
                    </div>
                  </div>

                  {/* Pricing and Action */}
                  <div className="border-t border-slate-100 pt-4 flex items-center justify-between gap-4">
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold tracking-wider">Price</span>
                      <span className="text-xl font-extrabold text-slate-900">
                        ${vehicle.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </span>
                    </div>
                    {user?.role === 'USER' && (
                      <button
                        onClick={() => handlePurchase(vehicle.id)}
                        disabled={buyingId === vehicle.id}
                        className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs px-4 py-2.5 rounded-xl transition duration-200 shadow-md shadow-indigo-600/10 disabled:opacity-50 flex items-center gap-1.5"
                      >
                        {buyingId === vehicle.id ? (
                          <div className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        ) : null}
                        <span>Purchase</span>
                      </button>
                    )}
                  </div>
                </div>

              </div>
            ))}
          </div>
        )}

        {/* Pagination Card */}
        {totalPages > 1 && (
          <div className="flex justify-between items-center bg-white border border-slate-200/80 rounded-2xl px-6 py-4 shadow-sm">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="text-sm font-semibold text-slate-600 hover:text-indigo-600 disabled:opacity-40 transition"
            >
              ← Previous
            </button>
            <span className="text-xs font-bold text-slate-500 tracking-wider">
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page === totalPages - 1}
              className="text-sm font-semibold text-slate-600 hover:text-indigo-600 disabled:opacity-40 transition"
            >
              Next →
            </button>
          </div>
        )}

      </div>
    </div>
  );
};
