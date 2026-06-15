<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1><i class="fas fa-file-alt"></i> 我的成绩单</h1>
        <p class="page-desc">查看已发布课程成绩与学分绩点</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="exportTranscript">
          <i class="fas fa-file-export"></i> 导出成绩单
        </button>
      </div>
    </div>

    <!-- 学生选择（管理员视角） -->
    <div v-if="auth.isAdmin" class="student-selector">
      <label>选择学生：</label>
      <select v-model="selectedStudentId" @change="loadTranscript">
        <option v-for="s in studentList" :key="s.studentId" :value="s.studentId">
          {{ s.studentName }} ({{ s.studentNumber }})
        </option>
      </select>
    </div>

    <div v-if="loading" class="loading-state"><i class="fas fa-spinner fa-spin fa-2x"></i><p>加载中...</p></div>
    <div v-else-if="error" class="error-state">{{ error }}</div>

    <template v-else-if="transcript">
      <!-- GPA 卡片 -->
      <div class="gpa-card">
        <div class="gpa-main">
          <span class="gpa-value">{{ transcript.gpa }}</span>
          <span class="gpa-label">平均绩点 (GPA)</span>
        </div>
        <div class="gpa-stats">
          <div class="gpa-stat">
            <span class="stat-value">{{ transcript.totalCourses }}</span>
            <span class="stat-label">课程数</span>
          </div>
          <div class="gpa-stat">
            <span class="stat-value">{{ transcript.totalCredits }}</span>
            <span class="stat-label">总学分</span>
          </div>
        </div>
      </div>

      <!-- 成绩明细 -->
      <div class="section-title">成绩明细</div>
      <div class="table-container">
        <table v-if="transcript.enrollments.length" class="transcript-table">
          <thead>
            <tr>
              <th>课程代码</th>
              <th>课程名称</th>
              <th>学分</th>
              <th>平时分</th>
              <th>期末分</th>
              <th>总评</th>
              <th>绩点</th>
              <th>等级</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in transcript.enrollments" :key="e.enrollmentId">
              <td class="code">{{ e.courseCode }}</td>
              <td class="cell-primary">{{ e.courseName }}</td>
              <td>{{ e.credits }}</td>
              <td>{{ e.usualScore ?? '-' }}</td>
              <td>{{ e.finalScore ?? '-' }}</td>
              <td><strong>{{ e.totalScore ?? '-' }}</strong></td>
              <td>{{ e.gradePoint ?? '-' }}</td>
              <td>
                <span :class="['level-badge', e.gradeLevel ? 'lv-' + e.gradeLevel : '']">{{ e.gradeLevel || '-' }}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-inline">暂无已发布的成绩</div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { showToast } from '../components/AppToast.vue'
import * as XLSX from 'xlsx'
import { getStudentCourses } from '../api/enrollment'
import { getStudents } from '../api/student'

const auth = useAuthStore()
const loading = ref(true)
const error = ref('')
const transcript = ref(null)
const studentList = ref([])
const selectedStudentId = ref(null)

onMounted(async () => {
  if (auth.isAdmin) {
    await loadStudentList()
  }
  const id = auth.isAdmin && studentList.value.length ? studentList.value[0].studentId : auth.user?.userId
  if (id) {
    selectedStudentId.value = id
    loadTranscript()
  } else {
    loading.value = false
  }
})

async function loadStudentList() {
  try {
    const res = await getStudents({})
    if (res.data.success) studentList.value = res.data.data || []
  } catch {}
}

async function loadTranscript() {
  if (!selectedStudentId.value) return
  loading.value = true; error.value = ''
  try {
    const res = await getStudentCourses(selectedStudentId.value)
    if (res.data.success) transcript.value = res.data.data
    else error.value = res.data.message
  } catch { error.value = '加载失败' }
  finally { loading.value = false }
}

function exportTranscript() {
  if (!transcript.value?.enrollments?.length) {
    showToast('没有可导出的成绩', 'error'); return
  }
  const data = transcript.value.enrollments.map(e => ({
    '课程代码': e.courseCode, '课程名称': e.courseName, '学分': e.credits,
    '平时分': e.usualScore ?? '', '期末分': e.finalScore ?? '', '总评': e.totalScore ?? '',
    '绩点': e.gradePoint ?? '', '等级': e.gradeLevel ?? ''
  }))
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '成绩单')
  XLSX.writeFile(wb, `成绩单_${new Date().toLocaleDateString('zh-CN')}.xlsx`)
}
</script>

<style scoped>
.page { padding: var(--space-6) var(--space-8); max-width: 1000px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-6); }
.page-header h1 { font-size: var(--text-2xl); font-weight: 700; color: var(--color-text); }
.page-desc { color: var(--color-text-muted); font-size: var(--text-base); margin-top: var(--space-1); }
.loading-state, .error-state { display: flex; flex-direction: column; align-items: center; padding: 60px 20px; color: var(--color-text-muted); gap: var(--space-3); }

.student-selector { display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-5); background: var(--color-surface); padding: var(--space-3) var(--space-4); border-radius: var(--radius-md); border: 1px solid var(--color-border); }
.student-selector select { flex: 1; padding: var(--space-2); border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: var(--text-base); }

.gpa-card { display: flex; align-items: center; gap: var(--space-8); background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: var(--space-6) var(--space-8); margin-bottom: var(--space-6); }
.gpa-main { display: flex; flex-direction: column; align-items: center; }
.gpa-value { font-size: 48px; font-weight: 700; color: var(--color-primary); line-height: 1; }
.gpa-label { font-size: var(--text-sm); color: var(--color-text-muted); margin-top: var(--space-1); }
.gpa-stats { display: flex; gap: var(--space-8); }
.gpa-stat { display: flex; flex-direction: column; align-items: center; }
.stat-value { font-size: var(--text-2xl); font-weight: 600; color: var(--color-text); }
.stat-label { font-size: var(--text-sm); color: var(--color-text-muted); }

.section-title { font-size: var(--text-md); font-weight: 600; color: var(--color-text); margin-bottom: var(--space-4); }

.table-container { overflow-x: auto; background: var(--color-surface); border-radius: var(--radius-lg); border: 1px solid var(--color-border); }
.transcript-table { width: 100%; border-collapse: collapse; }
.transcript-table th { padding: var(--space-3); text-align: left; font-size: var(--text-sm); font-weight: 600; color: var(--color-text-muted); border-bottom: 2px solid var(--color-border); white-space: nowrap; }
.transcript-table td { padding: var(--space-3); border-bottom: 1px solid var(--color-border-light); font-size: var(--text-base); color: var(--color-text-secondary); }
.code { font-family: monospace; color: var(--color-primary); }
.cell-primary { color: var(--color-text); font-weight: 500; }
.level-badge { padding: 2px 8px; border-radius: var(--radius-sm); font-size: 12px; }
.lv-优秀 { background: rgba(21,128,61,0.1); color: var(--color-success); }
.lv-良好 { background: rgba(51,65,85,0.1); color: var(--color-primary); }
.lv-中等 { background: rgba(245,158,11,0.1); color: #b54708; }
.lv-及格 { background: rgba(59,130,246,0.1); color: #2563eb; }
.lv-不及格 { background: rgba(180,35,24,0.08); color: var(--color-danger); }
.empty-inline { padding: 40px; text-align: center; color: var(--color-text-muted); }
</style>
