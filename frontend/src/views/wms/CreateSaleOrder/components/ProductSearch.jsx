import React, { useState, useCallback, useEffect } from 'react';
import { Box, Autocomplete, TextField, CircularProgress } from '@mui/material';
import { useOrderForm } from "../context/OrderFormContext";
import { useEntityData } from "../hooks/useEntityData";
import { debounce } from 'lodash';
import { toast } from "react-toastify";
import { useWms2Data } from 'services/useWms2Data';

const PAGE_SIZE = 20;

const ProductSearch = () => {
  const { 
    salesOrder, 
    setSalesOrder, 
    entities, 
    setEntities,
    productSearchState,
    setProductSearchState
  } = useOrderForm();
  
  const [productSearchText, setProductSearchText] = useState("");
  const [searchLoading, setSearchLoading] = useState(false);
  const { searchProducts } = useWms2Data();
  
  // Use the custom hook for product data (for initial loading)
  const { loading, handleScroll: handleScrollForAll, handleDropdownOpen } = useEntityData('products', (newData) => {
    // Process new data
    setEntities(prev => {
      const existingData = prev.products || [];
      const existingIds = new Set(existingData.map(item => item.id));
      const uniqueNewItems = newData.filter(item => !existingIds.has(item.id));
      
      return {
        ...prev,
        products: [...existingData, ...uniqueNewItems]
      };
    });
  });

  // Debounced search function for products
  const debouncedProductSearch = useCallback(
    debounce(async (searchText) => {
      if (searchText && searchText.length >= 2) {
        // Reset search state when new search is performed
        setProductSearchState(prev => ({
          ...prev,
          query: searchText,
          page: 0,
          hasMore: true,
          results: [],
          loading: true
        }));
        
        try {
          // Perform initial search
          const result = await searchProducts(searchText, 0, PAGE_SIZE);
          
          // Process search results
          const products = result?.data || [];
          
          // Update search state
          setProductSearchState(prev => ({
            ...prev,
            results: products,
            page: 1,
            hasMore: products.length >= PAGE_SIZE,
            loading: false
          }));
        } catch (error) {
          console.error("Error searching products", error);
          toast.error("Không thể tìm kiếm sản phẩm");
          
          setProductSearchState(prev => ({
            ...prev,
            loading: false
          }));
        }
      } else {
        // Reset search state when search text is too short
        setProductSearchState(prev => ({
          ...prev,
          query: '',
          results: [],
          page: 0,
          hasMore: true,
          loading: false
        }));
      }
    }, 500),
    [searchProducts, setProductSearchState]
  );

  // Handle loading more search results when scrolling
  const handleLoadMoreSearchResults = useCallback(async () => {
    // Skip if already loading or no more results
    if (productSearchState.loading || !productSearchState.hasMore) return;
    
    // Set loading state
    setProductSearchState(prev => ({
      ...prev,
      loading: true
    }));
    
    try {
      // Load more search results
      const result = await searchProducts(
        productSearchState.query, 
        productSearchState.page, 
        PAGE_SIZE
      );
      
      // Process results
      const newProducts = result?.data || [];
      
      // Update search state
      setProductSearchState(prev => {
        // Avoid duplicates
        const existingIds = new Set(prev.results.map(item => item.id));
        const uniqueNewItems = newProducts.filter(item => !existingIds.has(item.id));
        
        return {
          ...prev,
          results: [...prev.results, ...uniqueNewItems],
          page: prev.page + 1,
          hasMore: newProducts.length >= PAGE_SIZE,
          loading: false
        };
      });
    } catch (error) {
      console.error("Error loading more search results", error);
      
      setProductSearchState(prev => ({
        ...prev,
        loading: false
      }));
    }
  }, [productSearchState, searchProducts, setProductSearchState]);

  // Handle scroll in search results
  const handleSearchScroll = useCallback((event) => {
    const { currentTarget } = event;
    const isNearBottom = 
      currentTarget.scrollHeight - currentTarget.scrollTop <= currentTarget.clientHeight * 1.5;
    
    if (isNearBottom && productSearchState.hasMore && !productSearchState.loading) {
      handleLoadMoreSearchResults();
    }
  }, [productSearchState, handleLoadMoreSearchResults]);

  // Handle input change for search text
  const handleProductSearch = (e, value) => {
    setProductSearchText(value);
    
    // Only call the search API if we have at least 2 characters
    if (value && value.length >= 2) {
      debouncedProductSearch(value);
    } else {
      // Reset search results when input is cleared or too short
      setProductSearchState(prev => ({
        ...prev,
        query: '',
        results: [],
        page: 0,
        hasMore: true
      }));
    }
  };

  // Add product to order
  const addProductToOrder = (product) => {
    if (!product) return;
    
    // Check if product already exists in order
    const existingItem = salesOrder.orderItems.find(item => item.productId === product.id);
    if (existingItem) {
      // Increment quantity if product already in order
      const updatedItems = salesOrder.orderItems.map(item => 
        item.productId === product.id 
          ? { ...item, quantity: item.quantity + 1 } 
          : item
      );
      setSalesOrder(prev => ({ ...prev, orderItems: updatedItems }));
    } else {
      // Add new product to order
      const newItem = {
        productId: product.id,
        quantity: 1,
      };
      setSalesOrder(prev => ({ 
        ...prev, 
        orderItems: [...prev.orderItems, newItem] 
      }));

      // If product is not in our entities yet, add it
      if (!entities.products.find(p => p.id === product.id)) {
        setEntities(prev => ({
          ...prev,
          products: [...prev.products, product]
        }));
      }
    }
    
    // Clear search field
    setProductSearchText("");
    
    // Reset search state
    setProductSearchState(prev => ({
      ...prev,
      query: '',
      results: [],
      page: 0,
      hasMore: true
    }));
  };

  // Determine which options to display (search results or all products)
  const displayOptions = productSearchState.query 
    ? productSearchState.results 
    : entities.products.slice(0, 50); // Limit options if not searching
    
  // Check if loading
  const isLoading = productSearchState.loading || loading;

  // Select correct scroll handler based on context
  const handleScrollForResults = productSearchState.query 
    ? handleSearchScroll 
    : handleScrollForAll;

  return (
    <Box mb={2}>
      <Autocomplete
        options={displayOptions}
        getOptionLabel={(option) => `${option.id} - ${option.name || ''}`}
        inputValue={productSearchText}
        onInputChange={handleProductSearch}
        onChange={(_, value) => addProductToOrder(value)}
        onOpen={handleDropdownOpen}
        ListboxProps={{
          onScroll: handleScrollForResults,
          style: { maxHeight: '200px', overflow: 'auto' }
        }}
        noOptionsText={productSearchText.length < 2 
          ? "Nhập ít nhất 2 ký tự để tìm kiếm" 
          : "Không tìm thấy sản phẩm"
        }
        renderInput={(params) => (
          <TextField 
            {...params} 
            label="Tìm sản phẩm" 
            size="small" 
            fullWidth 
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <>
                  {isLoading ? <CircularProgress color="inherit" size={20} /> : null}
                  {params.InputProps.endAdornment}
                </>
              ),
            }}
          />
        )}
      />
    </Box>
  );
};

export default ProductSearch;