import { useState, useEffect } from 'react';

/**
 * Hook tùy chỉnh để debounce giá trị
 * @param {any} value - Giá trị cần debounce
 * @param {number} delay - Độ trễ tính bằng mili giây
 * @returns {any} Giá trị đã được debounce
 */
function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    // Đặt bộ hẹn giờ để cập nhật giá trị debounced sau độ trễ đã chỉ định
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Xóa bộ hẹn giờ nếu giá trị thay đổi trước khi hết thời gian trễ
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

export default useDebounce;
