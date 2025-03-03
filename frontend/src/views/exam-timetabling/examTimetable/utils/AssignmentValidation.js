/**
 * Validates that all assignment changes contain the required fields
 * @param {Object} assignmentChanges - Object containing assignment changes
 * @param {Array} originalData - Array of original assignment data
 * @returns {Object} Validation result with isValid flag and array of invalid assignments
 */
export const validateAssignmentChanges = (assignmentChanges, originalData) => {
  const requiredFields = ['roomId', 'weekNumber', 'date', 'sessionId'];
  const invalidAssignments = [];
  
  // Convert assignmentChanges from object to array of objects with assignmentId
  const assignmentsArray = Object.entries(assignmentChanges).map(([key, value]) => ({
    assignmentId: key,
    ...value
  }));
  
  // Check each assignment for required fields
  for (const assignment of assignmentsArray) {
    const missingFields = [];
    
    // Find the original assignment data to reference examClassId
    const originalAssignment = originalData.find(
      item => item.id.toString() === assignment.assignmentId.toString()
    );
    
    if (!originalAssignment) continue;
    
    // Check each required field
    for (const field of requiredFields) {
      // If the field is missing or empty in the change and in the original
      if (
        (!assignment[field] || assignment[field] === '') && 
        (!originalAssignment[field] || originalAssignment[field] === '')
      ) {
        missingFields.push(field);
      }
    }
    
    // If there are missing fields, add to invalid assignments
    if (missingFields.length > 0) {
      invalidAssignments.push({
        assignmentId: assignment.assignmentId,
        examClassId: originalAssignment.examClassId,
        courseId: originalAssignment.courseId,
        courseName: originalAssignment.courseName,
        missingFields
      });
    }
  }
  
  return {
    isValid: invalidAssignments.length === 0,
    invalidAssignments
  };
};

/**
 * Gets a human-readable field name from technical field name
 * @param {string} fieldName - Technical field name
 * @returns {string} Human-readable field name
 */
export const getFieldDisplayName = (fieldName) => {
  const fieldDisplayNames = {
    roomId: 'Phòng thi',
    weekNumber: 'Tuần',
    date: 'Ngày',
    sessionId: 'Ca thi'
  };
  
  return fieldDisplayNames[fieldName] || fieldName;
};
