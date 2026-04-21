import axios from 'axios'

const http = axios.create({ baseURL: '/api' })

export function login(username, password) {
  return http.post('/auth/login', { username, password })
}

export function verify(token) {
  return http.get('/auth/verify', {
    headers: { Authorization: `Bearer ${token}` }
  })
}
