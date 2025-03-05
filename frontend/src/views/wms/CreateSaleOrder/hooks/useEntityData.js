import { useState, useCallback, useRef } from 'react';
import { useWms2Data } from "services/useWms2Data";
import { toast } from "react-toastify";

const PAGE_SIZE = 20;

export const useEntityData = (entityType, onDataLoaded) => {
  const { getMoreCustomers, getMoreFacilities, getMoreProducts } = useWms2Data();
  const [state, setState] = useState({
    page: 0,
    hasMore: true,
    loading: false,
    initialized: false
  });
  
  // Use a ref to track if the component is mounted
  const isMountedRef = useRef(true);
  
  // Cleanup on unmount
  useCallback(() => {
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  // Identify which fetch function to use based on entityType
  const getFetchFunction = useCallback(() => {
    switch(entityType) {
      case 'facilities': return getMoreFacilities;
      case 'customers': return getMoreCustomers;
      case 'products': return getMoreProducts;
      default: throw new Error(`Unknown entity type: ${entityType}`);
    }
  }, [entityType, getMoreCustomers, getMoreFacilities, getMoreProducts]);

  // Display name for error messages
  const getEntityDisplayName = useCallback(() => {
    switch(entityType) {
      case 'facilities': return 'kho hàng';
      case 'customers': return 'khách hàng';
      case 'products': return 'sản phẩm';
      default: return entityType;
    }
  }, [entityType]);

  // Fetch data function
  const fetchData = useCallback(async () => {
    // Skip if we're already loading or there's no more data
    if (state.loading || !state.hasMore) return;
    
    // Set loading state
    setState(prev => ({ ...prev, loading: true }));
    
    try {
      const fetchFunction = getFetchFunction();
      const result = await fetchFunction(state.page, PAGE_SIZE);
      
      // Check if component is still mounted
      if (!isMountedRef.current) return;
      
      // Ensure we have an array
      const newData = Array.isArray(result?.data) ? result.data : 
                     Array.isArray(result?.data?.data) ? result.data.data : [];
      
      // Check if we have more data
      const hasMore = newData.length > 0 && newData.length >= PAGE_SIZE;
      
      // Call the callback with new data
      if (onDataLoaded) {
        onDataLoaded(newData);
      }
      
      // Update pagination state
      setState(prev => ({
        ...prev,
        page: prev.page + 1,
        hasMore,
        loading: false,
        initialized: true
      }));
    } catch (error) {
      // Check if component is still mounted
      if (!isMountedRef.current) return;
      
      console.error(`Error fetching ${entityType}:`, error);
      toast.error(`Không thể tải danh sách ${getEntityDisplayName()}`);
      
      // Reset loading state on error
      setState(prev => ({ ...prev, loading: false }));
    }
  }, [state, getFetchFunction, getEntityDisplayName, onDataLoaded, isMountedRef]);

  const handleDropdownOpen = useCallback(() => {
    if (!state.initialized || state.hasMore) {
      fetchData();
    }
  }, [state.initialized, state.hasMore, fetchData]);

  const handleScroll = useCallback((event) => {
    const { currentTarget } = event;
    const isNearBottom = 
      currentTarget.scrollHeight - currentTarget.scrollTop <= currentTarget.clientHeight * 1.5;
    
    if (isNearBottom && state.hasMore && !state.loading) {
      fetchData();
    }
  }, [state, fetchData]);

  return {
    loading: state.loading,
    hasMore: state.hasMore,
    fetchData,
    handleScroll,
    handleDropdownOpen
  };
};