<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1><i class="fas fa-book"></i> 课程管理</h1>
        <p class="page-desc">开设课程、管理选课</p>
      </div>
      <div class="header-actions">
        <button v-if="auth.isAdmin" class="btn btn-primary" @click="openAdd">
          <i class="fas fa-plus"></i> 新增课程
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state"><i class="fas fa-spinner fa-spin fa-2x"></i><p>加载中...</p></div>
    <div v-else-if="error" class="error-state">{{ error }}</div>

    <template v-else>
      <div class="toolbar">
        <span class="stats">共 <strong>{{ courses.length }}</strong> 门课程</span>
      </div>

      <div class="course-grid">
        <div v-for="c in courses" :key="c.courseId" class="course-card">
          <div class="card-header">
            <div class="card-code">{{ c.courseCode }}</div>
            <div class="card-credits">{{ c.credits }}学分</div>
          </div>
          <h3 class="card-title">{{ c.courseName }}</h3>
          <div class="card-meta">
            <span><i class="fas fa-user-tie"></i> {{ c.teacherName || '未分配' }}</span>
            <span><i class="fas fa-calendar"></i> {{ c.semester }}</span>
            <span><i class="fas fa-clock"></i> {{ c.schedule || '-' }}</span>
          </div>
          <div class="card-footer">
            <div class="enroll-stats">
              <div class="enroll-bar">
                <div class="enroll-fill" :style="{ width: (c.enrolledCount / c.maxStudents * 100) + '%' }" />
              </div>
              <span class="enroll-text">{{ c.enrolledCount }}/{{ c.maxStudents }}</span>
            </div>
            <div class="card-actions">
              <button v-if="auth.isAdmin" class="btn btn-ghost" @click="openEdit(c)"><i class="fas fa-edit"></i></button>
              <button v-if="auth.isAdmin" class="btn btn-ghost" @click="handleDelete(c)"><i class="fas fa-trash"></i></button>
            </div>
          </div>
        </div>
        <div v-if="!courses.length" class="empty-state">
          <i class="fas fa-book fa-3x"></i><p>暂无课程数据</p>
        </div>
      </div>
    </template>

    <!-- 课程表单弹窗 -->
    <AppModal v-model="showModal" :title="editing ? '编辑课程' : '新增课程'">
      <form @submit.prevent class="course-form">
        <div class="form-row">
          <div class="form-group">
            <label>课程名称 <span class="req">*</span></label>
            <input v-model="form.courseName" placeholder="例: 数据结构与算法" />
          </div>
          <div class="form-group">
            <label>课程代码 <span class="req">*</span></label>
            <input v-model="form.courseCode" placeholder="例: CS201" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>学分 <span class="req">*</span></label>
            <input v-model.number="form.credits" type="number" step="0.5" min="0.5" max="10" />
          </div>
          <div class="form-group">
            <label>授课教师</label>
            <select v-model.number="form.teacherId">
              <option :value="0">请选择</option>
              <option v-for="t in teachers" :key="t.userId" :value="t.userId">{{ t.realName }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>学期</label>
            <input v-model="form.semester" placeholder="例: 2024-2025-1" />
          </div>
          <div class="form-group">
            <label>上课时间</label>
            <input v-model="form.schedule" placeholder="例: 周一 1-2节" />
          </div>
        </div>
        <div class="form-group">
          <label>最大人数</label>
          <input v-model.number="form.maxStudents" type="number" min="1" max="200" />
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" @click="showModal = false">取消</button>
        <button class="btn btn-primary" @click="saveCourse">保存</button>
      </template>
    </AppModal>

    <ConfirmDialog v-model="showConfirm" message="确定要删除该课程吗？" @confirm="confirmDelete" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { showToast } from '../components/AppToast.vue'
import { getCourses, getTeachers, addCourse, updateCourse, deleteCourse } from '../api/course'
import AppModal from '../components/AppModal.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'

const auth = useAuthStore()
const courses = ref([])
const teachers = ref([])
const loading = ref(true)
const error = ref('')
const showModal = ref(false)
const showConfirm = ref(false)
const editing = ref(null)
const deleting = ref(null)

const form = ref({ courseName: '', courseCode: '', credits: 3, teacherId: 0, semester: '2024-2025-1', schedule: '', maxStudents: 50 })

onMounted(async () => {
  await loadTeachers()
  loadCourses()
})

async function loadTeachers() {
  try {
    const res = await getTeachers()
    if (res.data.success) teachers.value = res.data.data || []
  } catch {}
}

async function loadCourses() {
  loading.value = true; error.value = ''
  try {
    const res = await getCourses()
    if (res.data.success) courses.value = res.data.data || []
    else error.value = res.data.message
  } catch { error.value = '加载失败' }
  finally { loading.value = false }
}

function openAdd() {
  editing.value = null
  form.value = { courseName: '', courseCode: '', credits: 3, teacherId: 0, semester: '2024-2025-1', schedule: '', maxStudents: 50 }
  showModal.value = true
}

function openEdit(c) {
  editing.value = c
  form.value = {
    courseName: c.courseName, courseCode: c.courseCode, credits: c.credits,
    teacherId: c.teacherId || 0, semester: c.semester, schedule: c.schedule || '', maxStudents: c.maxStudents
  }
  showModal.value = true
}

async function saveCourse() {
  if (!form.value.courseName || !form.value.courseCode || !form.value.credits) {
    showToast('请填写必填字段', 'error'); return
  }
  const data = { ...form.value, teacherId: form.value.teacherId || null }
  try {
    const res = editing.value ? await updateCourse(editing.value.courseId, data) : await addCourse(data)
    if (res.data.success) {
      showToast(editing.value ? '更新成功' : '添加成功', 'success')
      showModal.value = false; loadCourses()
    } else showToast(res.data.message, 'error')
  } catch (err) { showToast('操作失败', 'error') }
}

function handleDelete(c) { deleting.value = c; showConfirm.value = true }
async function confirmDelete() {
  try {
    const res = await deleteCourse(deleting.value.courseId)
    if (res.data.success) { showToast('删除成功', 'success'); loadCourses() }
    else showToast(res.data.message, 'error')
  } catch { showToast('删除失败', 'error') }
}
</script>

<style scoped>
.page { padding: var(--space-6) var(--space-8); max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-6); }
.page-header h1 { font-size: var(--text-2xl); font-weight: 700; color: var(--color-text); }
.page-desc { color: var(--color-text-muted); font-size: var(--text-base); margin-top: var(--space-1); }
.loading-state, .error-state, .empty-state { display: flex; flex-direction: column; align-items: center; padding: 60px 20px; color: var(--color-text-muted); gap: var(--space-3); }
.toolbar { margin-bottom: var(--space-4); }
.stats { color: var(--color-text-secondary); font-size: var(--text-base); }

.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: var(--space-4); }
.course-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: var(--space-5); transition: var(--transition-fast); }
.course-card:hover { box-shadow: var(--shadow-sm); border-color: rgba(0,0,0,0.12); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); }
.card-code { font-size: var(--text-sm); font-weight: 600; color: var(--color-primary); letter-spacing: 0.04em; }
.card-credits { font-size: var(--text-xs); color: var(--color-text-muted); background: var(--color-surface-hover); padding: 2px 8px; border-radius: var(--radius-sm); }

.card-title { font-size: var(--text-lg); font-weight: 600; color: var(--color-text); margin-bottom: var(--space-3); }
.card-meta { display: flex; flex-direction: column; gap: var(--space-1); margin-bottom: var(--space-4); }
.card-meta span { font-size: var(--text-sm); color: var(--color-text-secondary); display: flex; align-items: center; gap: var(--space-2); }
.card-meta i { width: 16px; color: var(--color-text-muted); }

.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: var(--space-3); border-top: 1px solid var(--color-border-light); }
.enroll-stats { flex: 1; display: flex; align-items: center; gap: var(--space-3); }
.enroll-bar { flex: 1; height: 6px; background: var(--color-surface-hover); border-radius: 3px; overflow: hidden; }
.enroll-fill { height: 100%; background: var(--color-primary); border-radius: 3px; transition: width 0.3s; min-width: 0; }
.enroll-text { font-size: var(--text-xs); color: var(--color-text-muted); white-space: nowrap; }
.card-actions { display: flex; gap: var(--space-1); }

.course-form { max-height: 60vh; overflow-y: auto; }
.form-row { display: flex; gap: var(--space-4); margin-bottom: var(--space-4); }
.form-row .form-group { flex: 1; margin-bottom: 0; }
.form-group { margin-bottom: var(--space-4); }
.form-group label { display: block; margin-bottom: var(--space-1); font-size: var(--text-sm); font-weight: 500; color: var(--color-text); }
.req { color: var(--color-danger); }
.form-group input, .form-group select { width: 100%; padding: var(--space-2) var(--space-3); border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: var(--text-base); transition: var(--transition-fast); }
.form-group input:focus, .form-group select:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 2px rgba(51,65,85,0.1); }
</style>
