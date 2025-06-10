import { useHistory } from 'react-router-dom';
import { useCallback } from 'react';

export const useHandleNavigate = () => {
    const history = useHistory();
    const handleNavigate = useCallback((url, supplier) => {
        try {
            if (supplier && typeof supplier === 'function') {
                url = supplier(url);
            }
            history.push(url);
        } catch (error) {
            console.error('Navigation error:', error);
            history.goBack();
        }
    }, [history]);

    return handleNavigate;
};

export const formatCurrency = (
    value,
    {
        locale = "vi-VN",
        currency = "VND",
        minimumFractionDigits = 0,
        maximumFractionDigits = 0,
        fallback = "0 ₫"
    } = {}
) => {
    if (value === null || value === undefined || isNaN(value)) return fallback;

    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency,
        minimumFractionDigits,
        maximumFractionDigits
    }).format(value);
};

