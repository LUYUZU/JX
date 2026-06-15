<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1>仪表盘</h1>
        <p class="page-desc">学生数据概览与统计分析</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="refresh">
          <i class="fas fa-sync-alt"></i> 刷新
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p>加载统计数据...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <i class="fas fa-exclamation-triangle"></i>
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="refresh">重新加载</button>
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-icon stat-icon-blue">
            <i class="fas fa-user-graduate"></i>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.totalStudents }}</span>
            <span class="stat-label">在校学生</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-green">
            <i class="fas fa-book"></i>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.byMajor?.length || 0 }}</span>
            <span class="stat-label">专业数量</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-purple">
            <i class="fas fa-layer-group"></i>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ stats.byGrade?.length || 0 }}</span>
            <span class="stat-label">年级覆盖</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon-orange">
            <i class="fas fa-venus-mars"></i>
          </div>
          <div class="stat-body">
            <span class="stat-value">{{ totalMale + totalFemale }}</span>
            <span class="stat-label">已登记性别</span>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">
            <h3>专业分布</h3>
          </div>
          <div ref="majorChartRef" class="chart-container" />
          <div v-if="!stats.byMajor?.length" class="chart-empty">
            暂无专业数据
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>年级分布</h3>
          </div>
          <div ref="gradeChartRef" class="chart-container" />
          <div v-if="!stats.byGrade?.length" class="chart-empty">
            暂无年级数据
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>性别比例</h3>
          </div>
          <div ref="genderChartRef" class="chart-container" />
          <div v-if="!totalMale && !totalFemale" class="chart-empty">
            暂无性别数据
          </div>
        </div>

        <div class="chart-card chart-card-wide">
          <div class="chart-header">
            <h3>入学趋势</h3>
          </div>
          <div ref="trendChartRef" class="chart-container" />
          <div v-if="!stats.enrollmentTrend?.length" class="chart-empty">
            暂无入学趋势数据
          </div>
        </div>
      </div>

      <!-- 最近入学 -->
      <div class="recent-section">
        <h3>最近入学</h3>
        <table v-if="stats.recentStudents?.length" class="recent-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>学号</th>
              <th>专业</th>
              <th>年级</th>
              <th>入学日期</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in stats.recentStudents" :key="s.studentId">
              <td class="cell-primary">{{ s.studentName }}</td>
              <td>{{ s.studentNumber }}</td>
              <td>{{ s.major || '-' }}</td>
              <td>{{ s.grade || '-' }}</td>
              <td>{{ formatDate(s.enrollmentDate) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="no-data">暂无可显示的数据</div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '../api/dashboard'

const loading = ref(true)
const error = ref('')
const stats = ref({})

const majorChartRef = ref(null)
const gradeChartRef = ref(null)
const genderChartRef = ref(null)
const trendChartRef = ref(null)

let majorChart = null
let gradeChart = null
let genderChart = null
let trendChart = null

const totalMale = computed(() => {
  const item = stats.value.byGender?.find(g => g.name === '男')
  return item ? item.value : 0
})

const totalFemale = computed(() => {
  const item = stats.value.byGender?.find(g => g.name === '女')
  return item ? item.value : 0
})

onMounted(() => { loadStats() })
onUnmounted(() => { disposeCharts() })

watch(() => stats.value, () => {
  nextTick(() => {
    initCharts()
  })
}, { deep: true })

async function loadStats() {
  loading.value = true
  error.value = ''
  try {
    const res = await getStats()
    if (res.data.success) {
      stats.value = res.data.data || {}
    } else {
      error.value = res.data.message || '获取数据失败'
    }
  } catch {
    error.value = '网络错误，请检查后端服务是否启动'
  } finally {
    loading.value = false
  }
}

function refresh() {
  disposeCharts()
  loadStats()
}

function disposeCharts() {
  [majorChart, gradeChart, genderChart, trendChart].forEach(c => {
    if (c) { c.dispose(); c = null }
  })
}

function initCharts() {
  initMajorChart()
  initGradeChart()
  initGenderChart()
  initTrendChart()
}

function initMajorChart() {
  if (!majorChartRef.value || !stats.value.byMajor?.length) return
  if (majorChart) majorChart.dispose()
  majorChart = echarts.init(majorChartRef.value)

  const data = stats.value.byMajor
  const total = data.reduce((s, d) => s + d.value, 0)

  majorChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['38%', '68%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: data.length <= 8,
        formatter: '{b}\n{d}%',
        fontSize: 11,
        color: '#52525b'
      },
      emphasis: {
        label: { show: true, fontSize: 13, fontWeight: 'bold' }
      },
      data: data.map(d => ({
        name: d.name,
        value: d.value
      }))
    }]
  })
}

function initGradeChart() {
  if (!gradeChartRef.value || !stats.value.byGrade?.length) return
  if (gradeChart) gradeChart.dispose()
  gradeChart = echarts.init(gradeChartRef.value)

  const data = stats.value.byGrade

  gradeChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}人'
    },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: { color: '#71717a' },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#71717a' },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: '#334155'
      },
      barMaxWidth: 48
    }]
  })
}

function initGenderChart() {
  if (!genderChartRef.value) return
  if (genderChart) genderChart.dispose()
  genderChart = echarts.init(genderChartRef.value)

  const data = [
    { name: '男', value: totalMale.value },
    { name: '女', value: totalFemale.value }
  ].filter(d => d.value > 0)

  if (!data.length) return

  genderChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['42%', '72%'],
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        formatter: '{b}\n{c}人',
        fontSize: 12,
        color: '#52525b'
      },
      color: ['#334155', '#94a3b8'],
      data
    }]
  })
}

function initTrendChart() {
  if (!trendChartRef.value || !stats.value.enrollmentTrend?.length) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const data = stats.value.enrollmentTrend

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}年: {c}人'
    },
    grid: { left: 50, right: 20, top: 25, bottom: 30 },
    xAxis: {
      type: 'category',
      data: data.map(d => String(d.year)),
      axisLabel: { color: '#71717a' },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#71717a' },
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)' } }
    },
    series: [{
      type: 'line',
      data: data.map(d => d.count),
      smooth: true,
      lineStyle: { color: '#334155', width: 2.5 },
      itemStyle: { color: '#334155' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(51,65,85,0.25)' },
            { offset: 1, color: 'rgba(51,65,85,0.02)' }
          ]
        }
      },
      symbol: 'circle',
      symbolSize: 7
    }]
  })
}

function formatDate(d) {
  return d ? new Date(d).toLocaleDateString('zh-CN') : '-'
}
</script>

<style scoped>
.dashboard {
  padding: var(--space-6) var(--space-8);
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-6);
}

.page-header h1 {
  font-size: var(--text-2xl);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.page-desc {
  color: var(--color-text-muted);
  font-size: var(--text-base);
  margin-top: var(--space-1);
}

/* ---- Loading / Error ---- */
.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--color-text-muted);
  gap: var(--space-4);
}

.loading-state p,
.error-state p {
  font-size: var(--text-md);
}

.error-state i {
  color: var(--color-danger);
  font-size: 32px;
}

/* ---- Stat Cards ---- */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  transition: var(--transition-fast);
}

.stat-card:hover {
  box-shadow: var(--shadow-sm);
  border-color: rgba(0,0,0,0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xl);
  flex-shrink: 0;
}

.stat-icon-blue { background: rgba(51, 65, 85, 0.1); color: #334155; }
.stat-icon-green { background: rgba(21, 128, 61, 0.1); color: var(--color-success); }
.stat-icon-purple { background: rgba(124, 58, 237, 0.1); color: #7c3aed; }
.stat-icon-orange { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }

.stat-body {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.1;
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: var(--space-1);
}

/* ---- Charts ---- */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.chart-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  position: relative;
}

.chart-card-wide {
  grid-column: 1 / -1;
}

.chart-header {
  margin-bottom: var(--space-4);
}

.chart-header h3 {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text);
}

.chart-container {
  width: 100%;
  height: 260px;
}

.chart-card-wide .chart-container {
  height: 220px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-size: var(--text-base);
  pointer-events: none;
}

/* ---- Recent ---- */
.recent-section {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}

.recent-section h3 {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-4);
}

.recent-table {
  width: 100%;
  border-collapse: collapse;
}

.recent-table th {
  text-align: left;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-weight: 600;
  border-bottom: 2px solid var(--color-border);
}

.recent-table td {
  padding: var(--space-3);
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--color-border-light);
}

.cell-primary {
  color: var(--color-text);
  font-weight: 500;
}

.no-data {
  padding: var(--space-8);
  text-align: center;
  color: var(--color-text-muted);
}

@media (max-width: 800px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .dashboard {
    padding: var(--space-4);
  }
}
</style>
