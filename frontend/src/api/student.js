import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export function getStudents(params) {
  return http.get('/students', { params })
}

export function getStudent(id) {
  return http.get(`/students/${id}`)
}

export function addStudent(data) {
  return http.post('/students', data)
}

export function updateStudent(id, data) {
  return http.put(`/students/${id}`, data)
}

export function deleteStudent(id) {
  return http.delete(`/students/${id}`)
}

export function getMajors() {
  return http.get('/students/majors')
}
