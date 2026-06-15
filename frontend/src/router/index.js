import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import AppLayout from '../components/AppLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import StudentView from '../views/StudentView.vue'
import CourseView from '../views/CourseView.vue'
import GradeView from '../views/GradeView.vue'
import TranscriptView from '../views/TranscriptView.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView },
  {
    path: '/dashboard',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: DashboardView }]
  },
  {
    path: '/students',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: StudentView }]
  },
  {
    path: '/courses',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: CourseView }]
  },
  {
    path: '/grades',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: GradeView }]
  },
  {
    path: '/transcript',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [{ path: '', component: TranscriptView }]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.matched.some(r => r.meta.requiresAuth) && !auth.token) {
    next('/login')
  } else if (to.path === '/login' && auth.token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
