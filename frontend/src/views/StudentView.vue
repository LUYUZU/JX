<template>
  <div class="page-bg">
    <div class="container">
      <!-- 头部 -->
      <div class="header">
        <h1><i class="fas fa-graduation-cap"></i> 学生信息管理系统</h1>
        <p>搜索、筛选、数据校验完整功能</p>
        <div class="user-info">
          <span>
            <i class="fas fa-user-circle"></i>
            {{ auth.user?.realName }} ({{ auth.isAdmin ? '管理员' : '学生' }})
          </span>
          <button class="btn-logout" @click="logout">
            <i class="fas fa-sign-out-alt"></i> 退出登录
          </button>
        </div>
      </div>

      <div class="content">
        <!-- 搜索筛选 -->
        <div class="filter-section">
          <div class="search-box">
            <label><i class="fas fa-search"></i> 搜索（姓名/学号）</label>
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="输入姓名或学号..."
              :disabled="!auth.isAdmin && !!auth.user?.studentNumber"
              @keyup.enter="search"
            />
          </div>

          <template v-if="auth.isAdmin">
            <div class="filter-box">
              <label><i class="fas fa-book"></i> 专业筛选</label>
              <select v-model="filters.major">
                <option value="">全部专业</option>
                <option v-for="m in majors" :key="m" :value="m">{{ m }}</option>
              </select>
            </div>
            <div class="filter-box">
              <label><i class="fas fa-layer-group"></i> 年级筛选</label>
              <select v-model="filters.grade">
                <option value="">全部年级</option>
                <option value="大一">大一</option>
                <option value="大二">大二</option>
                <option value="大三">大三</option>
                <option value="大四">大四</option>
              </select>
            </div>
          </template>

          <div class="filter-actions">
            <button class="btn-search" @click="search">
              <i class="fas fa-search"></i> 搜索
            </button>
            <button class="btn-reset" @click="resetFilters">
              <i class="fas fa-undo"></i> 重置
            </button>
          </div>
        </div>

        <!-- 搜索统计 -->
        <div v-if="hasFilter" class="search-stats">
          <span><i class="fas fa-info-circle"></i> 找到 {{ students.length }} 条匹配记录</span>
          <span class="clear-search" @click="resetFilters">清除筛选 <i class="fas fa-times"></i></span>
        </div>

        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <button v-if="auth.isAdmin" class="btn-add" @click="openAddModal">
              <i class="fas fa-plus"></i> 新增学生
            </button>
            <button class="refresh-btn" @click="loadStudents">
              <i class="fas fa-sync-alt"></i> 刷新数据
            </button>
          </div>
          <div class="stats">共 <strong>{{ students.length }}</strong> 条记录</div>
        </div>

        <!-- 表格 -->
        <div class="table-container">
          <div v-if="loading" class="loading">
            <i class="fas fa-spinner fa-spin fa-2x" style="color:#667eea;"></i>
            <p>加载中...</p>
          </div>

          <div v-else-if="errorMsg" class="error-message">{{ errorMsg }}</div>

          <template v-else>
            <table v-if="paginatedStudents.length">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>姓名</th>
                  <th>学号</th>
                  <th>性别</th>
                  <th>年龄</th>
                  <th>专业</th>
                  <th>年级</th>
                  <th>邮箱</th>
                  <th>入学日期</th>
                  <th>状态</th>
                  <th v-if="auth.isAdmin">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in paginatedStudents" :key="s.studentId">
                  <td>{{ s.studentId }}</td>
                  <td><strong>{{ s.studentName }}</strong></td>
                  <td>{{ s.studentNumber }}</td>
                  <td>{{ s.gender || '-' }}</td>
                  <td>{{ s.age || '-' }}</td>
                  <td>{{ s.major || '-' }}</td>
                  <td>{{ s.grade || '-' }}</td>
                  <td>{{ s.email || '-' }}</td>
                  <td>{{ formatDate(s.enrollmentDate) }}</td>
                  <td>
                    <span :class="['status-badge', s.isActive ? 'status-active' : 'status-inactive']">
                      {{ s.isActive ? '在读' : '离校' }}
                    </span>
                  </td>
                  <td v-if="auth.isAdmin">
                    <div class="action-buttons">
                      <button class="btn-edit" @click="openEditModal(s.studentId)">
                        <i class="fas fa-edit"></i> 编辑
                      </button>
                      <button class="btn-delete-row" @click="showDeleteConfirm(s.studentId, s.studentName)">
                        <i class="fas fa-trash"></i> 删除
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-else class="no-data">
              <i class="fas fa-database fa-3x"></i>
              <p>暂无学生数据</p>
            </div>
          </template>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination">
          <button :disabled="currentPage === 1" @click="currentPage--">
            <i class="fas fa-chevron-left"></i>
          </button>
          <button
            v-for="p in totalPages"
            :key="p"
            :class="{ active: p === currentPage }"
            @click="currentPage = p"
          >{{ p }}</button>
          <button :disabled="currentPage === totalPages" @click="currentPage++">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 学生表单弹窗 -->
    <AppModal v-model="showModal" :title="editingId ? '编辑学生' : '新增学生'">
      <StudentForm ref="formRef" :student="editingStudent" />
      <template #footer>
        <button class="btn-cancel" @click="showModal = false">取消</button>
        <button class="btn-save" @click="saveStudent">保存</button>
      </template>
    </AppModal>

    <!-- 删除确认弹窗 -->
    <ConfirmDialog
      v-model="showConfirm"
      :message="confirmMessage"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getStudents, getStudent, addStudent, updateStudent, deleteStudent, getMajors } from '../api/student'
import { showToast } from '../components/AppToast.vue'
import AppModal from '../components/AppModal.vue'
import StudentForm from '../components/StudentForm.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'

const router = useRouter()
const auth = useAuthStore()

const students = ref([])
const majors = ref([])
const loading = ref(false)
const errorMsg = ref('')

const filters = ref({ keyword: '', major: '', grade: '' })
const hasFilter = computed(() =>
  filters.value.keyword || filters.value.major || filters.value.grade
)

const currentPage = ref(1)
const pageSize = 10
const totalPages = computed(() => Math.ceil(students.value.length / pageSize))
const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return students.value.slice(start, start + pageSize)
})

const showModal = ref(false)
const editingId = ref(null)
const editingStudent = ref(null)
const formRef = ref(null)

const showConfirm = ref(false)
const confirmMessage = ref('')
const deletingId = ref(null)

onMounted(() => {
  loadStudents()
  if (auth.isAdmin) loadMajors()
})

async function loadStudents() {
  loading.value = true
  errorMsg.value = ''
  try {
    const params = {}
    if (!auth.isAdmin && auth.user?.studentNumber) {
      params.keyword = auth.user.studentNumber
    } else {
      if (filters.value.keyword) params.keyword = filters.value.keyword
      if (filters.value.major) params.major = filters.value.major
      if (filters.value.grade) params.grade = filters.value.grade
    }
    const res = await getStudents(params)
    if (res.data.success) {
      students.value = res.data.data || []
      currentPage.value = 1
    } else {
      errorMsg.value = res.data.message || '加载数据失败'
    }
  } catch {
    errorMsg.value = '网络错误，请检查后端服务是否启动'
  } finally {
    loading.value = false
  }
}

async function loadMajors() {
  try {
    const res = await getMajors()
    if (res.data.success) majors.value = res.data.data || []
  } catch {}
}

function search() {
  currentPage.value = 1
  loadStudents()
}

function resetFilters() {
  filters.value = { keyword: '', major: '', grade: '' }
  loadStudents()
}

function openAddModal() {
  editingId.value = null
  editingStudent.value = null
  showModal.value = true
}

async function openEditModal(id) {
  try {
    const res = await getStudent(id)
    if (res.data.success) {
      editingId.value = id
      editingStudent.value = res.data.data
      showModal.value = true
    } else {
      showToast('获取学生信息失败', 'error')
    }
  } catch {
    showToast('获取学生信息失败', 'error')
  }
}

async function saveStudent() {
  if (!formRef.value?.validateAll()) {
    showToast('请正确填写表单', 'error')
    return
  }
  const data = formRef.value.getFormData()
  try {
    let res
    if (editingId.value) {
      res = await updateStudent(editingId.value, data)
    } else {
      res = await addStudent(data)
    }
    if (res.data.success) {
      showToast(editingId.value ? '更新成功' : '添加成功', 'success')
      showModal.value = false
      loadStudents()
    } else {
      showToast(res.data.message || '操作失败', 'error')
    }
  } catch (err) {
    showToast(err.response?.data?.message || '操作失败，请检查网络', 'error')
  }
}

function showDeleteConfirm(id, name) {
  deletingId.value = id
  confirmMessage.value = `确定要删除学生 "${name}" 的信息吗？此操作不可恢复。`
  showConfirm.value = true
}

async function confirmDelete() {
  try {
    const res = await deleteStudent(deletingId.value)
    if (res.data.success) {
      showToast('删除成功', 'success')
      loadStudents()
    } else {
      showToast(res.data.message || '删除失败', 'error')
    }
  } catch {
    showToast('删除失败，请检查网络', 'error')
  }
}

function logout() {
  auth.clearAuth()
  router.push('/login')
}

function formatDate(d) {
  return d ? new Date(d).toLocaleDateString('zh-CN') : '-'
}
</script>

<style scoped>
.page-bg {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  text-align: center;
}
.header h1 { font-size: 2em; margin-bottom: 8px; }
.header p { font-size: 1em; opacity: 0.9; }

.user-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  margin-top: 20px;
  border-radius: 8px;
  font-size: 14px;
}
.user-info span { display: flex; align-items: center; gap: 8px; }

.btn-logout {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 6px 14px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background 0.3s;
}
.btn-logout:hover { background: rgba(255, 255, 255, 0.3); }

.content { padding: 24px; }

.filter-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: flex-end;
}
.search-box { flex: 2; min-width: 250px; }
.filter-box { flex: 1; min-width: 150px; }
.filter-box label, .search-box label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-size: 13px;
  font-weight: 500;
}
.filter-box input, .filter-box select,
.search-box input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s;
}
.filter-box input:focus, .filter-box select:focus,
.search-box input:focus {
  border-color: #667eea;
  outline: none;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}
.filter-box input:disabled, .search-box input:disabled {
  background: #e9ecef;
  cursor: not-allowed;
}

.filter-actions { display: flex; gap: 10px; align-items: center; }

.btn-search {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background 0.3s;
}
.btn-search:hover { background: #5a67d8; }

.btn-reset {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background 0.3s;
}
.btn-reset:hover { background: #5a6268; }

.search-stats {
  background: #e7f3ff;
  padding: 10px 15px;
  border-radius: 5px;
  margin-bottom: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #004085;
  border-left: 4px solid #667eea;
}
.clear-search { color: #667eea; cursor: pointer; text-decoration: underline; font-size: 13px; }

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left { display: flex; gap: 10px; }

.btn-add {
  background: #28a745;
  color: white;
  border: none;
  padding: 9px 18px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background 0.3s;
}
.btn-add:hover { background: #218838; }

.refresh-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 9px 18px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background 0.3s;
}
.refresh-btn:hover { background: #5a67d8; }

.stats { color: #666; font-size: 14px; }

.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

table { width: 100%; border-collapse: collapse; background: white; }

th {
  background: #f8f9fa;
  color: #333;
  font-weight: 600;
  padding: 14px 12px;
  text-align: left;
  border-bottom: 2px solid #dee2e6;
  white-space: nowrap;
  font-size: 14px;
}

td {
  padding: 11px 12px;
  border-bottom: 1px solid #dee2e6;
  color: #555;
  font-size: 14px;
}

tr:hover td { background: #f8f9fa; }

.status-badge {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.status-active { background: #d4edda; color: #155724; }
.status-inactive { background: #f8d7da; color: #721c24; }

.action-buttons { display: flex; gap: 6px; }

.btn-edit {
  background: #28a745;
  color: white;
  border: none;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: background 0.3s;
}
.btn-edit:hover { background: #218838; }

.btn-delete-row {
  background: #dc3545;
  color: white;
  border: none;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: background 0.3s;
}
.btn-delete-row:hover { background: #c82333; }

.loading { text-align: center; padding: 50px; color: #666; }
.loading p { margin-top: 10px; }

.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 15px;
  border-radius: 5px;
  font-size: 14px;
}

.no-data { text-align: center; padding: 50px; color: #999; }
.no-data p { margin-top: 10px; }

.pagination {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 16px;
}
.pagination button {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.pagination button:hover:not(:disabled) { background: #f8f9fa; border-color: #667eea; }
.pagination button.active { background: #667eea; color: white; border-color: #667eea; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-cancel {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}
.btn-cancel:hover { background: #5a6268; }

.btn-save {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}
.btn-save:hover { background: #218838; }
</style>
