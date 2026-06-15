import http from './http'

export function getEnrollmentsByCourse(courseId) { return http.get(`/enrollments/course/${courseId}`) }
export function getUngradedByCourse(courseId) { return http.get(`/enrollments/course/${courseId}/ungraded`) }
export function getStudentCourses(studentId) { return http.get(`/enrollments/student/${studentId}`) }
export function getStudentAllCourses(studentId) { return http.get(`/enrollments/student/${studentId}/all`) }
export function enroll(data) { return http.post('/enrollments', data) }
export function unenroll(id, courseId) { return http.delete(`/enrollments/${id}`, { params: { courseId } }) }
export function updateGrade(id, data) { return http.put(`/enrollments/${id}/grade`, data) }
export function publishGrade(id, publish) { return http.put(`/enrollments/${id}/publish`, { publish }) }
