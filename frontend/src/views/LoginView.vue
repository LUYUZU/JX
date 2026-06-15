<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-mark">JX</div>
        <div class="brand-copy">
          <span class="brand-kicker">JX Platform</span>
          <h1>学生信息管理系统</h1>
          <p>面向教学管理与日常维护的统一数据入口，提供稳定、清晰、克制的使用体验。</p>
        </div>
      </section>

      <section class="login-container">
        <div class="login-header fade-step step-1">
          <div class="product-lockup">
            <div class="product-badge">J</div>
            <div>
              <span class="product-name">JX Console</span>
              <h2>欢迎回来</h2>
            </div>
          </div>
          <p>请登录您的账号以继续访问学生数据。</p>
        </div>

        <div v-if="errorMsg" class="error-message fade-step step-2">{{ errorMsg }}</div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group fade-step step-2">
            <label for="username">用户名</label>
            <div class="input-group">
              <i class="fas fa-user"></i>
              <input id="username" v-model="username" type="text" placeholder="请输入用户名" required />
            </div>
          </div>

          <div class="form-group fade-step step-3">
            <label for="password">密码</label>
            <div class="input-group">
              <i class="fas fa-lock"></i>
              <input id="password" v-model="password" type="password" placeholder="请输入密码" required />
            </div>
          </div>

          <button type="submit" class="btn btn-primary login-submit fade-step step-4" :disabled="loading">
            <span>登录</span>
            <span v-if="loading" class="spinner"></span>
          </button>
        </form>

        <div class="test-accounts fade-step step-5">
          <div class="test-accounts-header">
            <h3>测试账号</h3>
            <span>仅用于本地联调</span>
          </div>
          <button class="account-item" type="button" @click="fillAccount('admin', '123456')">
            <span class="role">管理员</span>
            <span class="details">admin / 123456</span>
            <i class="fas fa-arrow-up-right-from-square"></i>
          </button>
          <button class="account-item" type="button" @click="fillAccount('zhangsan', '123456')">
            <span class="role">学生</span>
            <span class="details">zhangsan / 123456</span>
            <i class="fas fa-arrow-up-right-from-square"></i>
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { login, verify } from '../api/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

onMounted(async () => {
  if (auth.token) {
    try {
      const res = await verify(auth.token)
      if (res.data.success) router.push('/dashboard')
    } catch {
      auth.clearAuth()
    }
  }
})

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await login(username.value.trim(), password.value.trim())
    if (res.data.success) {
      auth.setAuth(res.data.token, res.data.user)
      router.push('/dashboard')
    } else {
      errorMsg.value = res.data.message || '登录失败'
    }
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '网络错误，请检查后端服务是否启动'
  } finally {
    loading.value = false
  }
}

function fillAccount(u, p) {
  username.value = u
  password.value = p
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: var(--space-8);
  background: #fafaf8;
}

.login-shell {
  min-height: calc(100vh - var(--space-8) * 2);
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(380px, 460px);
  align-items: center;
  gap: var(--space-10);
}

.brand-panel {
  padding: var(--space-12) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.brand-mark {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  background: #0d1117;
  color: white;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xl);
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow: var(--shadow-md);
}

.brand-copy {
  max-width: 520px;
}

.brand-kicker {
  display: inline-block;
  margin-bottom: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-copy h1 {
  font-size: var(--text-3xl);
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: var(--color-text);
  margin-bottom: var(--space-4);
}

.brand-copy p {
  max-width: 460px;
  color: var(--color-text-secondary);
  font-size: var(--text-md);
  line-height: 1.75;
}

.login-container {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-modal);
  padding: var(--space-12);
  animation: cardIn var(--transition-slow) ease-out both;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-step {
  opacity: 0;
  transform: translateY(16px);
  animation: stepIn 280ms ease-out forwards;
}

.step-1 { animation-delay: 0ms; }
.step-2 { animation-delay: 40ms; }
.step-3 { animation-delay: 80ms; }
.step-4 { animation-delay: 120ms; }
.step-5 { animation-delay: 160ms; }

@keyframes stepIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  margin-bottom: var(--space-8);
}

.product-lockup {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.product-badge {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: rgba(51, 65, 85, 0.08);
  color: var(--color-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-lg);
  font-weight: 700;
}

.product-name {
  display: block;
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: var(--space-1);
}

.login-header h2 {
  font-size: var(--text-2xl);
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.login-header p {
  color: var(--color-text-muted);
  font-weight: 300;
  line-height: 1.7;
}

.error-message {
  background: rgba(180, 35, 24, 0.08);
  border: 1px solid rgba(180, 35, 24, 0.12);
  color: var(--color-danger);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-5);
  font-size: var(--text-base);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

label {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
  height: 44px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  transition: var(--transition-fast);
}

.input-group:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(51, 65, 85, 0.12);
}

.input-group i {
  padding: 0 var(--space-4);
  color: var(--color-text-muted);
}

.input-group input {
  flex: 1;
  height: 100%;
  padding: 0 var(--space-4) 0 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--text-base);
  color: var(--color-text);
}

.input-group input::placeholder {
  color: var(--color-text-muted);
}

.login-submit {
  width: 100%;
  height: 44px;
  margin-top: var(--space-1);
}

.spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.test-accounts {
  margin-top: var(--space-8);
  padding-top: var(--space-5);
  border-top: 1px solid var(--color-border-light);
}

.test-accounts-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.test-accounts-header h3 {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 600;
}

.test-accounts-header span {
  color: var(--color-text-muted);
  font-size: var(--text-xs);
}

.account-item {
  width: 100%;
  margin-bottom: var(--space-2);
  padding: 0 var(--space-3);
  height: 40px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  background: var(--color-surface);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--color-text-muted);
  transition: var(--transition-fast);
}

.account-item:last-child {
  margin-bottom: 0;
}

.account-item:hover {
  border-color: var(--color-border);
  background: var(--color-surface-hover);
}

.account-item .role {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
}

.account-item .details {
  flex: 1;
  font-size: var(--text-sm);
}

.account-item i {
  color: var(--color-text-muted);
  font-size: 12px;
}

@media (max-width: 980px) {
  .login-page {
    padding: var(--space-5);
  }

  .login-shell {
    min-height: auto;
    grid-template-columns: 1fr;
    gap: var(--space-6);
  }

  .brand-panel {
    padding: var(--space-4) var(--space-2) 0;
  }

  .brand-copy p {
    max-width: none;
  }
}

@media (max-width: 640px) {
  .login-container {
    padding: var(--space-8);
  }

  .brand-copy h1 {
    font-size: var(--text-2xl);
  }

  .login-header h2 {
    font-size: var(--text-xl);
  }
}
</style>
