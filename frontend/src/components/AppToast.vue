<script>
import { reactive } from 'vue'

const state = reactive({ visible: false, message: '', type: 'success' })
let timer = null

export function showToast(message, type = 'success') {
  state.message = message
  state.type = type
  state.visible = true
  clearTimeout(timer)
  timer = setTimeout(() => { state.visible = false }, 3000)
}

export default {
  setup() { return { state } }
}
</script>

<template>
  <Transition name="slide-right">
    <div v-if="state.visible" :class="['toast', state.type]">
      <i :class="['fas', state.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle']"></i>
      <span>{{ state.message }}</span>
    </div>
  </Transition>
</template>

<style scoped>
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 24px;
  background: white;
  border-radius: 5px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 2000;
}
.toast.success { border-left: 4px solid #28a745; }
.toast.success i { color: #28a745; font-size: 20px; }
.toast.error { border-left: 4px solid #dc3545; }
.toast.error i { color: #dc3545; font-size: 20px; }

.slide-right-enter-active,
.slide-right-leave-active { transition: all 0.3s; }
.slide-right-enter-from,
.slide-right-leave-to { transform: translateX(120%); opacity: 0; }
</style>
