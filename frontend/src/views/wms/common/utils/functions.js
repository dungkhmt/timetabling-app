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
