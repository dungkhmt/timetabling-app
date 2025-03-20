/**
 * Form validation utility functions for the class planning forms
 */

/**
 * Validates the class creation form input
 * @param {Object} formData - The form data to validate
 * @param {Object} semester - The selected semester object
 * @returns {Object} An object containing validation errors, empty if no errors
 */
export const validateClassForm = (formData, semester) => {
  const errors = {};

  if (!formData.moduleCode) {
    errors.moduleCode = "Mã học phần không được để trống";
  }

  if (!formData.moduleName) {
    errors.moduleName = "Tên học phần không được để trống";
  }

  if (!formData.learningWeeks) {
    errors.learningWeeks = "Tuần học không được để trống";
  }

  if (!semester) {
    errors.semester = "Vui lòng chọn kỳ học";
  }

  if (!formData.groupId) {
    errors.groupId = "Vui lòng chọn nhóm";
  }

  // Validate mass format if provided
  if (formData.mass && !validateMassFormat(formData.mass)) {
    errors.mass = "Khối lượng phải có định dạng X(a-b-c-d) với X,a,b,c,d là số tự nhiên";
  }

  return errors;
};

/**
 * Helper function to validate the mass format X(a-b-c-d)
 * @param {string} mass - The mass string to validate
 * @returns {boolean} True if the format is valid, false otherwise
 */
export const validateMassFormat = (mass) => {
  const regex = /^[0-9]+\([0-9]+-[0-9]+-[0-9]+-[0-9]+\)$/;
  return regex.test(mass);
};

/**
 * Prepare a payload from form data for API submission
 * @param {Object} formData - The form data
 * @param {Object} semester - The selected semester
 * @returns {Object} The formatted payload for API submission
 */
export const prepareClassPayload = (formData, semester) => {
  // Define which numeric fields should be null when empty vs. 0 when empty
  const numericFields = ['duration', 'numberOfClasses'];
  const nullableNumericFields = ['lectureMaxQuantity', 'exerciseMaxQuantity', 'lectureExerciseMaxQuantity'];
  
  const payload = {
    groupId: formData.groupId, // Use groupId for API
    programName: formData.programName, // Add programName to payload 
    moduleCode: formData.moduleCode,
    moduleName: formData.moduleName,
    semester: semester.semester,
    learningWeeks: formData.learningWeeks,
    weekType: formData.weekType,
    crew: formData.crew,
    promotion: formData.promotion,
    mass: formData.mass || ""
  };
  
  // Handle standard numeric fields (convert to 0 if empty)
  numericFields.forEach(field => {
    payload[field] = formData[field] ? Number(formData[field]) : 0;
  });
  
  // Handle nullable numeric fields (convert to null if empty)
  nullableNumericFields.forEach(field => {
    payload[field] = formData[field] ? Number(formData[field]) : null;
  });
  
  return payload;
};

/**
 * Extract meaningful error message from API error response
 * @param {Object} error - The error object from the API
 * @returns {string} A user-friendly error message
 */
export const extractErrorMessage = (error) => {
  if (!error) {
    return "Đã xảy ra lỗi không xác định";
  }
  
  if (error.response) {
    // Server returned an error response
    const { status, data } = error.response;
    
    // Check if we have a string message in the response
    if (typeof data === 'string' && data.trim()) {
      return data;
    }
    
    // Check for common fields that might contain error messages
    if (data) {
      if (data.message) return data.message;
      if (data.error) return data.error;
      if (data.errorMessage) return data.errorMessage;
    }
    
    // Return a generic message based on HTTP status
    switch (status) {
      case 400:
        return "Dữ liệu yêu cầu không hợp lệ";
      case 401:
        return "Bạn cần đăng nhập để thực hiện chức năng này";
      case 403:
        return "Bạn không có quyền thực hiện chức năng này";
      case 404:
        return "Không tìm thấy tài nguyên yêu cầu";
      case 409:
        return "Dữ liệu đã tồn tại hoặc xung đột";
      case 410:
        return "Dữ liệu yêu cầu không còn tồn tại";
      case 500:
        return "Lỗi máy chủ, vui lòng thử lại sau";
      default:
        return `Lỗi ${status}: ${error.response.statusText || 'Có lỗi khi gửi yêu cầu'}`;
    }
  } else if (error.request) {
    // Request was made but no response
    return "Không nhận được phản hồi từ máy chủ";
  } else if (error.message) {
    // Error has a message property
    if (error.message.includes('timeout')) {
      return "Yêu cầu đã hết thời gian chờ";
    }
    if (error.message.includes('Network Error')) {
      return "Lỗi kết nối mạng";
    }
    return error.message;
  }
  
  // Fallback
  return "Đã xảy ra lỗi không xác định";
};
