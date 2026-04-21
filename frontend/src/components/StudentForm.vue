<template>
  <form @submit.prevent>
    <div class="form-row">
      <div class="form-group">
        <label><i class="fas fa-user"></i> 姓名 <span class="required">*</span></label>
        <input
          v-model="form.studentName"
          type="text"
          placeholder="请输入姓名"
          :class="{ 'input-error': errors.studentName }"
          @blur="validate('studentName')"
        />
        <div v-if="errors.studentName" class="error-text">{{ errors.studentName }}</div>
        <div class="validation-hint">最大长度50字符</div>
      </div>
      <div class="form-group">
        <label><i class="fas fa-id-card"></i> 学号 <span class="required">*</span></label>
        <input
          v-model="form.studentNumber"
          type="text"
          placeholder="请输入学号"
          :class="{ 'input-error': errors.studentNumber }"
          @blur="validate('studentNumber')"
        />
        <div v-if="errors.studentNumber" class="error-text">{{ errors.studentNumber }}</div>
        <div class="validation-hint">最大长度20字符，必须唯一</div>
      </div>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label><i class="fas fa-venus-mars"></i> 性别</label>
        <select v-model="form.gender">
          <option value="">请选择</option>
          <option value="男">男</option>
          <option value="女">女</option>
        </select>
      </div>
      <div class="form-group">
        <label><i class="fas fa-calendar"></i> 年龄 <span class="required">*</span></label>
        <input
          v-model.number="form.age"
          type="number"
          min="15"
          max="50"
          placeholder="15-50岁"
          :class="{ 'input-error': errors.age }"
          @blur="validate('age')"
        />
        <div v-if="errors.age" class="error-text">{{ errors.age }}</div>
        <div class="validation-hint">请输入15-50之间的整数</div>
      </div>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label><i class="fas fa-book"></i> 专业</label>
        <input v-model="form.major" type="text" placeholder="请输入专业" />
      </div>
      <div class="form-group">
        <label><i class="fas fa-layer-group"></i> 年级</label>
        <select v-model="form.grade">
          <option value="">请选择</option>
          <option value="大一">大一</option>
          <option value="大二">大二</option>
          <option value="大三">大三</option>
          <option value="大四">大四</option>
        </select>
      </div>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label><i class="fas fa-envelope"></i> 邮箱</label>
        <input
          v-model="form.email"
          type="email"
          placeholder="请输入邮箱"
          :class="{ 'input-error': errors.email }"
          @blur="validate('email')"
        />
        <div v-if="errors.email" class="error-text">{{ errors.email }}</div>
        <div class="validation-hint">例如: example@domain.com</div>
      </div>
      <div class="form-group">
        <label><i class="fas fa-calendar-plus"></i> 入学日期</label>
        <input v-model="form.enrollmentDate" type="date" />
      </div>
    </div>

    <div class="form-group">
      <label><i class="fas fa-toggle-on"></i> 状态</label>
      <select v-model="form.isActive">
        <option :value="true">在读</option>
        <option :value="false">离校</option>
      </select>
    </div>
  </form>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  student: { type: Object, default: null }
})

const emit = defineEmits(['update:formData', 'valid'])

const form = reactive({
  studentName: '',
  studentNumber: '',
  gender: '',
  age: '',
  major: '',
  grade: '',
  email: '',
  enrollmentDate: '',
  isActive: true
})

const errors = reactive({})

watch(() => props.student, (s) => {
  if (s) {
    Object.assign(form, {
      studentName: s.studentName || '',
      studentNumber: s.studentNumber || '',
      gender: s.gender || '',
      age: s.age || '',
      major: s.major || '',
      grade: s.grade || '',
      email: s.email || '',
      enrollmentDate: s.enrollmentDate || '',
      isActive: s.isActive ?? true
    })
  } else {
    Object.assign(form, {
      studentName: '', studentNumber: '', gender: '', age: '',
      major: '', grade: '', email: '', enrollmentDate: '', isActive: true
    })
  }
  Object.keys(errors).forEach(k => delete errors[k])
}, { immediate: true })

watch(form, () => emit('update:formData', { ...form }), { deep: true })

function validate(field) {
  delete errors[field]
  if (field === 'studentName') {
    const v = form.studentName.trim()
    if (!v) errors.studentName = '姓名不能为空'
    else if (v.length > 50) errors.studentName = '姓名长度不能超过50字符'
  }
  if (field === 'studentNumber') {
    const v = form.studentNumber.trim()
    if (!v) errors.studentNumber = '学号不能为空'
    else if (v.length > 20) errors.studentNumber = '学号长度不能超过20字符'
  }
  if (field === 'age') {
    const v = form.age
    if (v !== '' && v !== null && (v < 15 || v > 50)) errors.age = '年龄必须在15-50岁之间'
  }
  if (field === 'email') {
    const v = form.email.trim()
    if (v && !v.includes('@')) errors.email = '邮箱格式不正确（必须包含@）'
  }
}

function validateAll() {
  ;['studentName', 'studentNumber', 'age', 'email'].forEach(validate)
  return Object.keys(errors).length === 0
}

function getFormData() {
  return {
    studentName: form.studentName.trim(),
    studentNumber: form.studentNumber.trim(),
    gender: form.gender,
    age: form.age !== '' ? Number(form.age) : null,
    major: form.major.trim(),
    grade: form.grade,
    email: form.email.trim(),
    enrollmentDate: form.enrollmentDate,
    isActive: form.isActive
  }
}

defineExpose({ validateAll, getFormData })
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}
.form-row .form-group { flex: 1; margin-bottom: 0; }

.form-group { margin-bottom: 15px; }

label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}
.required { color: red; }

input, select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}
input:focus, select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}
.input-error { border-color: #dc3545 !important; }
.error-text { color: #dc3545; font-size: 12px; margin-top: 4px; }
.validation-hint { color: #666; font-size: 12px; margin-top: 3px; }
</style>
