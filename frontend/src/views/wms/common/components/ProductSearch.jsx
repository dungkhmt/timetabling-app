import React, { useState, useCallback, useEffect } from 'react';
import { Box, Autocomplete, TextField, CircularProgress } from '@mui/material';
import { useOrderForm } from "../context/OrderFormContext";
import { useEntityData } from "../hooks/useEntityData";
import { debounce } from 'lodash';
import { toast } from "react-toastify";
import { useWms2Data } from 'services/useWms2Data';
import { ORDER_TYPE_ID } from '../constants/constants';

const PAGE_SIZE = 20;

const ProductSearch = ({ orderTypeId = ORDER_TYPE_ID.SALES_ORDER }) => {
  const { 
    order, 
    setOrder, 
    entities, 
    setEntities,
    productSearchState,
    setProductSearchState
  } = useOrderForm();
  
  const [productSearchText, setProductSearchText] = useState("");
  const { searchProducts } = useWms2Data();
  
  const isPurchaseOrder = orderTypeId === ORDER_TYPE_ID.PURCHASE_ORDER;
  const isSaleOrder = orderTypeId === ORDER_TYPE_ID.SALES_ORDER;
  
  // Đảm bảo rằng products và results luôn là mảng
  useEffect(() => {
    if (!Array.isArray(entities.products)) {
      setEntities(prev => ({ ...prev, products: [] }));
    }
    
    if (!productSearchState || !Array.isArray(productSearchState.results)) {
      setProductSearchState(prev => ({ 
        ...prev, 
        results: [],
        query: '',
        page: 0,
        hasMore: true,
        loading: false
      }));
    }
  }, []);
  
  // Use the custom hook for product data (for initial loading)
  const { handleScroll: handleScrollForAll, handleDropdownOpen } = useEntityData('products', (newData) => {
    // Process new data
    setEntities(prev => {
      const existingData = Array.isArray(prev.products) ? prev.products : [];
      const existingIds = new Set(existingData.map(item => item.id));
      const uniqueNewItems = Array.isArray(newData) ? newData.filter(item => !existingIds.has(item.id)) : [];
      
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
          const products = Array.isArray(result?.data) ? result.data : [];
          
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
            loading: false,
            results: []
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
    // Kiểm tra xem productSearchState có hợp lệ không
    if (!productSearchState || productSearchState.loading || !productSearchState.hasMore) return;
    
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
      const newProducts = Array.isArray(result?.data) ? result.data : [];
      
      // Update search state
      setProductSearchState(prev => {
        const prevResults = Array.isArray(prev.results) ? prev.results : [];
        
        // Avoid duplicates
        const existingIds = new Set(prevResults.map(item => item.id));
        const uniqueNewItems = newProducts.filter(item => !existingIds.has(item.id));
        
        return {
          ...prev,
          results: [...prevResults, ...uniqueNewItems],
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
    if (!event || !event.currentTarget) return;
    
    const { currentTarget } = event;
    const isNearBottom = 
      currentTarget.scrollHeight - currentTarget.scrollTop <= currentTarget.clientHeight * 1.5;
    
    if (isNearBottom && productSearchState?.hasMore && !productSearchState?.loading) {
      handleLoadMoreSearchResults();
    }
  }, [productSearchState, handleLoadMoreSearchResults]);

  // Handle input change for search text
  const handleProductSearch = (e, value) => {
    setProductSearchText(value || "");
    
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

  const addProductToOrder = (product) => {
    console.log("Adding product to order:", product);
    if (!product) return;
    
    // Check if product already exists in order
    const existingItem = order.orderItems.find(item => item.productId === product.id);
    if (existingItem) {
      // Increment quantity if product already in order
      const updatedItems = order.orderItems.map(item => 
        item.productId === product.id 
          ? { ...item, quantity: item.quantity + 1 } 
          : item
      );
      setOrder(prev => ({ ...prev, orderItems: updatedItems }));
    } else {
      // Determine price based on order type
      let productPrice;
      if (isPurchaseOrder) {
        // Purchase Order: Use cost price (giá nhập)
        productPrice = product.costPrice || 0;
      } else {
        // Sale Order: Use wholesale price (giá bán)
        productPrice = product.wholeSalePrice || 0;
      }

      // Add new product to order with correct price and fields
      const newItem = {
        productId: product.id,
        productName: product.name, // Store product name for reference
        quantity: 1,
        price: productPrice,
        unit: product.unit || "Cái",
        discount: 0,
        // Add tax field for purchase orders (as percentage)
        ...(isPurchaseOrder && {
          tax: product.vatRate // Default VAT rate as percentage, user can modify
        }),
        note: ""
      };
      
      setOrder(prev => ({ 
        ...prev, 
        orderItems: [...prev.orderItems, newItem] 
      }));

      // If product is not in our entities yet, add it
      const productsArray = Array.isArray(entities.products) ? entities.products : [];
      if (!productsArray.find(p => p.id === product.id)) {
        setEntities(prev => ({
          ...prev,
          products: [...productsArray, product]
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

  // Helper function to get display price based on order type
  const getDisplayPrice = (product) => {
    if (isPurchaseOrder) {
      return product.costPrice || 0;
    } else {
      return product.wholeSalePrice || 0;
    }
  };

  // Helper function to get price label based on order type
  const getPriceLabel = () => {
    return isPurchaseOrder ? "Giá nhập" : "Giá bán";
  };

  // Đảm bảo options luôn là mảng
  const safeProductsArray = Array.isArray(entities.products) ? entities.products : [];
  const safeResultsArray = Array.isArray(productSearchState?.results) ? productSearchState.results : [];
  
  // Determine which options to display (search results or all products)
  const displayOptions = productSearchState?.query 
    ? safeResultsArray 
    : safeProductsArray.slice(0, 50); // Limit options if not searching
    

  // Select correct scroll handler based on context
  const handleScrollForResults = productSearchState?.query 
    ? handleSearchScroll 
    : handleScrollForAll;

  return (
    <Box mb={2}>
      <Autocomplete
        options={displayOptions}
        getOptionLabel={(option) => {
          if (typeof option !== 'object' || option === null) return '';
          const price = getDisplayPrice(option);
          const priceLabel = getPriceLabel();
          return `${option.id || ''} - ${option.name || ''} (${priceLabel}: ${price.toLocaleString()} VND)`;
        }}
        inputValue={productSearchText}
        onInputChange={handleProductSearch}
        onChange={(_, value) => addProductToOrder(value)}
        onOpen={handleDropdownOpen}
        isOptionEqualToValue={(option, value) => option.id === value.id}
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
            label={`Tìm sản phẩm (${getPriceLabel()})`}
            size="small" 
            fullWidth 
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <>
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