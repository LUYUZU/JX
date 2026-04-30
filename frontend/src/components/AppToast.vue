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
  top: var(--space-5);
  right: var(--space-5);
  padding: var(--space-3) var(--space-6);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  z-index: 2000;
}
.toast.success { border-left: 4px solid var(--color-success); }
.toast.success i { color: var(--color-success); font-size: 20px; }
.toast.error { border-left: 4px solid var(--color-danger); }
.toast.error i { color: var(--color-danger); font-size: 20px; }

.slide-right-enter-active,
.slide-right-leave-active { transition: var(--transition-base); }
.slide-right-enter-from,
.slide-right-leave-to { transform: translateX(120%); opacity: 0; }
</style>
