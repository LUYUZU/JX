<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <i class="fas fa-graduation-cap"></i>
        <h1>学生信息管理系统</h1>
        <p>请登录您的账号</p>
      </div>

      <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label><i class="fas fa-user"></i> 用户名</label>
          <div class="input-group">
            <i class="fas fa-user"></i>
            <input v-model="username" type="text" placeholder="请输入用户名" required />
          </div>
        </div>

        <div class="form-group">
          <label><i class="fas fa-lock"></i> 密码</label>
          <div class="input-group">
            <i class="fas fa-lock"></i>
            <input v-model="password" type="password" placeholder="请输入密码" required />
          </div>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          <span>登录</span>
          <span v-if="loading" class="spinner"></span>
        </button>
      </form>

      <div class="test-accounts">
        <h4>测试账号</h4>
        <div class="account-item">
          <span class="role">管理员</span>
          <span class="details">admin / 123456</span>
          <i class="fas fa-copy" title="复制账号" @click="fillAccount('admin', '123456')"></i>
        </div>
        <div class="account-item">
          <span class="role">学生</span>
          <span class="details">zhangsan / 123456</span>
          <i class="fas fa-copy" title="复制账号" @click="fillAccount('zhangsan', '123456')"></i>
        </div>
      </div>
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
      if (res.data.success) router.push('/students')
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
      router.push('/students')
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-container {
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 400px;
  max-width: 90%;
  padding: 40px;
  animation: slideIn 0.5s;
}
@keyframes slideIn {
  from { transform: translateY(-50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.login-header { text-align: center; margin-bottom: 30px; }
.login-header i { font-size: 60px; color: #667eea; margin-bottom: 20px; display: block; }
.login-header h1 { color: #667eea; font-size: 1.8em; margin-bottom: 10px; }
.login-header p { color: #666; }

.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 12px;
  border-radius: 5px;
  margin-bottom: 20px;
  text-align: center;
  font-size: 14px;
}

.form-group { margin-bottom: 20px; }

label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 5px;
  transition: all 0.3s;
}
.input-group:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}
.input-group i { padding: 0 15px; color: #999; }
.input-group input {
  flex: 1;
  padding: 12px 15px 12px 0;
  border: none;
  outline: none;
  font-size: 14px;
}

.btn-login {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: transform 0.3s;
}
.btn-login:hover:not(:disabled) { transform: translateY(-2px); }
.btn-login:disabled { opacity: 0.7; cursor: not-allowed; }

.spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.test-accounts {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}
.test-accounts h4 { color: #666; margin-bottom: 15px; text-align: center; }

.account-item {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.account-item .role {
  background: #667eea;
  color: white;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
}
.account-item .details { flex: 1; font-size: 13px; color: #666; }
.account-item i { color: #28a745; cursor: pointer; }
</style>
