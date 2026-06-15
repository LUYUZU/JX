import http from './http'

export function getCourses() { return http.get('/courses') }
export function getCourse(id) { return http.get(`/courses/${id}`) }
export function getAvailableCourses(studentId) { return http.get('/courses/available', { params: { studentId } }) }
export function getTeachers() { return http.get('/courses/teachers') }
export function addCourse(data) { return http.post('/courses', data) }
export function updateCourse(id, data) { return http.put(`/courses/${id}`, data) }
export function deleteCourse(id) { return http.delete(`/courses/${id}`) }
