<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1><i class="fas fa-pen"></i> 成绩录入</h1>
        <p class="page-desc">录入与发布课程成绩</p>
      </div>
    </div>

    <!-- 课程列表 -->
    <div v-if="!selectedCourse" class="course-list">
      <div v-if="loading" class="loading-state"><i class="fas fa-spinner fa-spin fa-2x"></i><p>加载中...</p></div>
      <div v-else-if="error" class="error-state">{{ error }}</div>
      <template v-else>
        <h3 class="section-title">选择课程录入成绩</h3>
        <div class="course-grid">
          <div v-for="c in courses" :key="c.courseId" class="course-card" @click="selectCourse(c)">
            <div class="card-code">{{ c.courseCode }}</div>
            <h3>{{ c.courseName }}</h3>
            <div class="card-meta">
              <span>{{ c.teacherName }}</span>
              <span>{{ c.semester }}</span>
              <span>已选 {{ c.enrolledCount }} 人</span>
            </div>
          </div>
          <div v-if="!courses.length" class="empty-state"><i class="fas fa-book fa-3x"></i><p>暂无课程</p></div>
        </div>
      </template>
    </div>

    <!-- 成绩录入 -->
    <div v-else>
      <div class="back-bar">
        <button class="btn btn-ghost" @click="selectedCourse = null"><i class="fas fa-arrow-left"></i> 返回课程列表</button>
        <h3>{{ selectedCourse.courseName }} ({{ selectedCourse.courseCode }})</h3>
      </div>

      <div v-if="gradesLoading" class="loading-state"><i class="fas fa-spinner fa-spin fa-2x"></i><p>加载学生...</p></div>
      <div v-else-if="!students.length" class="empty-state"><i class="fas fa-users fa-3x"></i><p>该课程暂无选课学生</p></div>

      <template v-else>
        <div class="table-container">
          <table class="grade-table">
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>平时分(30%)</th>
                <th>期末分(70%)</th>
                <th>总评</th>
                <th>绩点</th>
                <th>等级</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in students" :key="s.enrollmentId">
                <td>{{ s.studentNumber }}</td>
                <td class="cell-primary">{{ s.studentName }}</td>
                <td>
                  <input v-model.number="s.editUsual" type="number" min="0" max="100" class="grade-input" @input="calc(s)" :disabled="s.isPublished" />
                </td>
                <td>
                  <input v-model.number="s.editFinal" type="number" min="0" max="100" class="grade-input" @input="calc(s)" :disabled="s.isPublished" />
                </td>
                <td><strong>{{ s.totalScore ?? '-' }}</strong></td>
                <td>{{ s.gradePoint ?? '-' }}</td>
                <td><span :class="['level-badge', s.gradeLevel ? 'lv-' + s.gradeLevel : '']">{{ s.gradeLevel || '-' }}</span></td>
                <td>
                  <span :class="['status-badge', s.isPublished ? 'pub' : 'unpub']">{{ s.isPublished ? '已发布' : '未发布' }}</span>
                </td>
                <td class="action-cell">
                  <button class="btn btn-ghost btn-sm" @click="saveGrade(s)" :disabled="s.isPublished || saving">
                    <i class="fas fa-save"></i>
                  </button>
                  <button v-if="auth.isAdmin || !s.isPublished" class="btn btn-ghost btn-sm" @click="togglePublish(s)">
                    <i :class="s.isPublished ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { showToast } from '../components/AppToast.vue'
import { getCourses } from '../api/course'
import { getEnrollmentsByCourse, updateGrade, publishGrade } from '../api/enrollment'

const auth = useAuthStore()
const courses = ref([])
const loading = ref(true)
const error = ref('')
const selectedCourse = ref(null)
const students = ref([])
const gradesLoading = ref(false)
const saving = ref(false)

onMounted(() => loadCourses())

async function loadCourses() {
  loading.value = true
  try {
    const res = await getCourses()
    if (res.data.success) courses.value = res.data.data || []
  } catch { error.value = '加载失败' }
  finally { loading.value = false }
}

async function selectCourse(c) {
  selectedCourse.value = c
  gradesLoading.value = true
  try {
    const res = await getEnrollmentsByCourse(c.courseId)
    if (res.data.success) {
      students.value = (res.data.data || []).map(s => ({
        ...s,
        editUsual: s.usualScore ?? null,
        editFinal: s.finalScore ?? null
      }))
    }
  } catch { showToast('加载选课数据失败', 'error') }
  finally { gradesLoading.value = false }
}

function calc(s) {
  if (s.editUsual != null && s.editFinal != null && s.editUsual !== '' && s.editFinal !== '') {
    const u = Number(s.editUsual), f = Number(s.editFinal)
    if (isNaN(u) || isNaN(f)) return
    const total = Math.round((u * 0.3 + f * 0.7) * 10) / 10
    s.totalScore = total
    s.gradePoint = total >= 90 ? 4 : total >= 85 ? 3.7 : total >= 82 ? 3.3 : total >= 78 ? 3 : total >= 75 ? 2.7 : total >= 72 ? 2.3 : total >= 68 ? 2 : total >= 64 ? 1.5 : total >= 60 ? 1 : 0
    s.gradeLevel = total >= 90 ? '优秀' : total >= 80 ? '良好' : total >= 70 ? '中等' : total >= 60 ? '及格' : '不及格'
  }
}

async function saveGrade(s) {
  if (s.editUsual == null || s.editFinal == null) {
    showToast('请先输入平时分和期末分', 'error'); return
  }
  saving.value = true
  try {
    const res = await updateGrade(s.enrollmentId, {
      usualScore: Number(s.editUsual), finalScore: Number(s.editFinal), isPublished: false
    })
    if (res.data.success) {
      if (res.data.data) {
        s.totalScore = res.data.data.totalScore
        s.gradePoint = res.data.data.gradePoint
        s.gradeLevel = res.data.data.gradeLevel
      }
      showToast('成绩已保存', 'success')
    } else showToast(res.data.message, 'error')
  } catch { showToast('保存失败', 'error') }
  finally { saving.value = false }
}

async function togglePublish(s) {
  try {
    const publish = !s.isPublished
    if (publish && (s.editUsual == null || s.editFinal == null)) {
      showToast('请先录入成绩再发布', 'error'); return
    }
    const res = await publishGrade(s.enrollmentId, publish)
    if (res.data.success) {
      s.isPublished = publish
      showToast(publish ? '成绩已发布' : '已取消发布', 'success')
    }
  } catch { showToast('操作失败', 'error') }
}
</script>

<style scoped>
.page { padding: var(--space-6) var(--space-8); max-width: 1200px; }
.page-header { margin-bottom: var(--space-6); }
.page-header h1 { font-size: var(--text-2xl); font-weight: 700; color: var(--color-text); }
.page-desc { color: var(--color-text-muted); font-size: var(--text-base); margin-top: var(--space-1); }

.loading-state, .error-state, .empty-state { display: flex; flex-direction: column; align-items: center; padding: 60px 20px; color: var(--color-text-muted); gap: var(--space-3); }

.section-title { font-size: var(--text-md); font-weight: 600; margin-bottom: var(--space-4); color: var(--color-text); }

.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: var(--space-4); }
.course-card { background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); padding: var(--space-5); cursor: pointer; transition: var(--transition-fast); }
.course-card:hover { box-shadow: var(--shadow-sm); border-color: var(--color-primary); }
.course-card h3 { font-size: var(--text-lg); font-weight: 600; color: var(--color-text); margin: var(--space-2) 0 var(--space-3); }
.card-code { font-size: var(--text-sm); font-weight: 600; color: var(--color-primary); }
.card-meta { display: flex; flex-direction: column; gap: 4px; font-size: var(--text-sm); color: var(--color-text-secondary); }

.back-bar { display: flex; align-items: center; gap: var(--space-4); margin-bottom: var(--space-5); }
.back-bar h3 { font-size: var(--text-lg); font-weight: 600; color: var(--color-text); }

.table-container { overflow-x: auto; background: var(--color-surface); border-radius: var(--radius-lg); border: 1px solid var(--color-border); }
.grade-table { width: 100%; border-collapse: collapse; }
.grade-table th { padding: var(--space-3); text-align: left; font-size: var(--text-sm); font-weight: 600; color: var(--color-text-muted); border-bottom: 2px solid var(--color-border); white-space: nowrap; }
.grade-table td { padding: var(--space-2) var(--space-3); border-bottom: 1px solid var(--color-border-light); font-size: var(--text-base); }
.cell-primary { color: var(--color-text); font-weight: 500; }

.grade-input { width: 72px; padding: 4px 8px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); text-align: center; font-size: var(--text-base); }
.grade-input:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 2px rgba(51,65,85,0.1); }
.grade-input:disabled { background: var(--color-surface-hover); cursor: not-allowed; }

.action-cell { white-space: nowrap; }
.btn-sm { height: 28px; padding: 0 8px; font-size: 12px; }

.status-badge { padding: 2px 8px; border-radius: var(--radius-sm); font-size: 12px; font-weight: 500; }
.status-badge.pub { background: rgba(21,128,61,0.1); color: var(--color-success); }
.status-badge.unpub { background: rgba(180,35,24,0.08); color: var(--color-danger); }

.level-badge { padding: 2px 8px; border-radius: var(--radius-sm); font-size: 12px; }
.lv-优秀 { background: rgba(21,128,61,0.1); color: var(--color-success); }
.lv-良好 { background: rgba(51,65,85,0.1); color: var(--color-primary); }
.lv-中等 { background: rgba(245,158,11,0.1); color: #b54708; }
.lv-及格 { background: rgba(59,130,246,0.1); color: #2563eb; }
.lv-不及格 { background: rgba(180,35,24,0.08); color: var(--color-danger); }
</style>
