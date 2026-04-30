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
          <button class="btn btn-secondary" @click="logout">
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
            <button class="btn btn-primary" @click="search">
              <i class="fas fa-search"></i> 搜索
            </button>
            <button class="btn btn-secondary" @click="resetFilters">
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
            <button v-if="auth.isAdmin" class="btn btn-primary" @click="openAddModal">
              <i class="fas fa-plus"></i> 新增学生
            </button>
            <button class="btn btn-secondary" @click="loadStudents">
              <i class="fas fa-sync-alt"></i> 刷新数据
            </button>
          </div>
          <div class="stats">共 <strong>{{ students.length }}</strong> 条记录</div>
        </div>

        <!-- 表格 -->
        <div class="table-container">
          <div v-if="loading" class="loading">
            <i class="fas fa-spinner fa-spin fa-2x loading-icon"></i>
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
                  <th v-if="auth.isAdmin" class="action-cell">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in paginatedStudents" :key="s.studentId">
                  <td>{{ s.studentId }}</td>
                  <td class="cell-primary">{{ s.studentName }}</td>
                  <td class="cell-primary">{{ s.studentNumber }}</td>
                  <td>{{ s.gender || '-' }}</td>
                  <td>{{ s.age || '-' }}</td>
                  <td class="cell-secondary">{{ s.major || '-' }}</td>
                  <td class="cell-secondary">{{ s.grade || '-' }}</td>
                  <td>{{ s.email || '-' }}</td>
                  <td>{{ formatDate(s.enrollmentDate) }}</td>
                  <td>
                    <span :class="['status-badge', s.isActive ? 'status-active' : 'status-inactive']">
                      {{ s.isActive ? '在读' : '离校' }}
                    </span>
                  </td>
                  <td v-if="auth.isAdmin" class="action-cell">
                    <div class="action-buttons">
                      <button class="btn btn-ghost" @click="openEditModal(s.studentId)">
                        <i class="fas fa-edit"></i> 编辑
                      </button>
                      <button class="btn btn-danger" @click="showDeleteConfirm(s.studentId, s.studentName)">
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
          <button class="btn btn-secondary" :disabled="currentPage === 1" @click="currentPage--">
            <i class="fas fa-chevron-left"></i>
          </button>
          <button
            v-for="p in totalPages"
            :key="p"
            class="btn btn-secondary"
            :class="{ active: p === currentPage }"
            @click="currentPage = p"
          >{{ p }}</button>
          <button class="btn btn-secondary" :disabled="currentPage === totalPages" @click="currentPage++">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 学生表单弹窗 -->
    <AppModal v-model="showModal" :title="editingId ? '编辑学生' : '新增学生'">
      <StudentForm ref="formRef" :student="editingStudent" />
      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">取消</button>
        <button class="btn btn-primary" @click="saveStudent">保存</button>
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
  background: var(--color-bg);
  min-height: 100vh;
  padding: var(--space-5);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.header {
  background: var(--color-primary);
  color: white;
  padding: var(--space-8);
  text-align: center;
}
.header h1 { font-size: 2em; margin-bottom: var(--space-2); }
.header p { font-size: 1em; opacity: 0.9; }

.user-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-5);
  background: rgba(255, 255, 255, 0.1);
  margin-top: var(--space-5);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
}
.user-info span { display: flex; align-items: center; gap: var(--space-2); }
.user-info :deep(.btn-secondary) {
  color: white;
  border-color: rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
}
.user-info :deep(.btn-secondary:hover:not(:disabled)) {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.36);
}

.content { padding: var(--space-6); }

.filter-section {
  background: var(--color-surface-hover);
  padding: var(--space-5);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  align-items: flex-end;
}
.search-box { flex: 2; min-width: 250px; }
.filter-box { flex: 1; min-width: 150px; }
.filter-box label, .search-box label {
  display: block;
  margin-bottom: var(--space-1);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
}
.filter-box input, .filter-box select,
.search-box input {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  transition: var(--transition-fast);
}
.filter-box input:focus, .filter-box select:focus,
.search-box input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 2px rgba(51, 65, 85, 0.1);
}
.filter-box input:disabled, .search-box input:disabled {
  background: color-mix(in srgb, var(--color-surface-hover) 82%, black);
  cursor: not-allowed;
}

.filter-actions { display: flex; gap: var(--space-3); align-items: center; }

.search-stats {
  background: rgba(51, 65, 85, 0.06);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  border-left: 4px solid var(--color-primary);
}
.clear-search {
  color: var(--color-primary);
  cursor: pointer;
  text-decoration: underline;
  font-size: var(--text-sm);
}

.toolbar {
  margin-bottom: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left { display: flex; gap: var(--space-3); }

.stats { color: var(--color-text-secondary); font-size: var(--text-base); }

.table-container {
  overflow-x: auto;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.loading-icon {
  color: var(--color-primary);
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: transparent;
}

th {
  height: 44px;
  padding: 0 var(--space-3);
  text-align: left;
  white-space: nowrap;
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 600;
  letter-spacing: 0.04em;
  border-bottom: 2px solid var(--color-border);
}

tbody tr {
  height: 52px;
  transition: var(--transition-fast);
}

tbody tr:hover {
  background: var(--color-surface-hover);
}

td {
  padding: 0 var(--space-3);
  border-bottom: 1px solid var(--color-border-light);
  color: var(--color-text-muted);
  font-size: var(--text-base);
  vertical-align: middle;
}

tbody tr:last-child td {
  border-bottom: none;
}

.cell-primary {
  color: var(--color-text);
  font-weight: 500;
}

.cell-secondary {
  color: var(--color-text-secondary);
}

.action-cell {
  text-align: right;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  border-left: 2px solid currentColor;
}
.status-active {
  color: var(--color-success);
  background: rgba(21, 128, 61, 0.1);
}
.status-inactive {
  color: var(--color-danger);
  background: rgba(180, 35, 24, 0.08);
}

.action-buttons {
  display: inline-flex;
  justify-content: flex-end;
  gap: var(--space-1);
  opacity: 0.54;
  transition: var(--transition-fast);
}
tr:hover .action-buttons {
  opacity: 1;
}
.action-buttons :deep(.btn) {
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
}

.loading { text-align: center; padding: 50px; color: var(--color-text-secondary); }
.loading p { margin-top: var(--space-2); }

.error-message {
  background: rgba(180, 35, 24, 0.08);
  color: var(--color-danger);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
}

.no-data { text-align: center; padding: 50px; color: var(--color-text-muted); }
.no-data p { margin-top: var(--space-2); }

.pagination {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-1);
  margin-top: var(--space-4);
}
.pagination :deep(.btn) {
  min-width: 32px;
  height: 32px;
  padding: 0 var(--space-2);
}
.pagination :deep(.btn.active) {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}
.pagination :deep(.btn:disabled) { opacity: 0.4; }
</style>



