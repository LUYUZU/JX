<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">JX</div>
        <div class="brand-text">
          <span class="brand-name">JX Platform</span>
          <span class="brand-desc">学生信息管理系统</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/dashboard" class="nav-item" active-class="active">
          <i class="fas fa-chart-pie"></i>
          <span>仪表盘</span>
        </router-link>
        <router-link v-if="auth.isAdmin" to="/students" class="nav-item" active-class="active">
          <i class="fas fa-users"></i>
          <span>学生管理</span>
        </router-link>
        <router-link v-if="auth.isAdmin || auth.user?.role === 'teacher'" to="/courses" class="nav-item" active-class="active">
          <i class="fas fa-book"></i>
          <span>课程管理</span>
        </router-link>
        <router-link v-if="auth.isAdmin || auth.user?.role === 'teacher'" to="/grades" class="nav-item" active-class="active">
          <i class="fas fa-pen"></i>
          <span>成绩录入</span>
        </router-link>
        <router-link v-if="auth.user?.role === 'student'" to="/transcript" class="nav-item" active-class="active">
          <i class="fas fa-file-alt"></i>
          <span>我的成绩</span>
        </router-link>
      </nav>

      <div class="sidebar-spacer" />

      <div class="sidebar-footer">
        <div class="user-block">
          <div class="user-avatar">
            {{ auth.user?.realName?.charAt(0) || 'U' }}
          </div>
          <div class="user-meta">
            <div class="user-name">{{ auth.user?.realName || '用户' }}</div>
            <div class="user-role">{{ roleLabel }}</div>
          </div>
          <button class="btn-logout" title="退出登录" @click="handleLogout">
            <i class="fas fa-sign-out-alt"></i>
          </button>
        </div>
      </div>
    </aside>

    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const roleLabel = computed(() => {
  if (auth.isAdmin) return '管理员'
  if (auth.user?.role === 'teacher') return '教师'
  return '学生'
})

function handleLogout() {
  auth.clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

/* ---- Sidebar ---- */
.sidebar {
  width: 240px;
  min-width: 240px;
  background: #0d1117;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5) var(--space-5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #fff;
  color: #0d1117;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: var(--text-lg);
  letter-spacing: 0.06em;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: var(--text-sm);
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #fff;
}

.brand-desc {
  font-size: var(--text-xs);
  color: rgba(255, 255, 255, 0.45);
  margin-top: 1px;
}

/* ---- Navigation ---- */
.sidebar-nav {
  padding: var(--space-3) var(--space-3) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  color: rgba(255, 255, 255, 0.65);
  transition: var(--transition-fast);
  cursor: pointer;
}

.nav-item i {
  width: 20px;
  text-align: center;
  font-size: var(--text-md);
}

.nav-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
}

.nav-item.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  font-weight: 500;
}

/* ---- Spacer ---- */
.sidebar-spacer {
  flex: 1;
}

/* ---- Footer ---- */
.sidebar-footer {
  padding: var(--space-3);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.user-block {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2);
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
}

.user-block:hover {
  background: rgba(255, 255, 255, 0.04);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: 600;
  flex-shrink: 0;
}

.user-meta {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: var(--text-xs);
  color: rgba(255, 255, 255, 0.4);
}

.btn-logout {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.35);
  cursor: pointer;
  transition: var(--transition-fast);
  flex-shrink: 0;
}

.btn-logout:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

/* ---- Main ---- */
.main {
  flex: 1;
  min-width: 0;
  background: var(--color-bg);
  overflow-y: auto;
}
</style>
