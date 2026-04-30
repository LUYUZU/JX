<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-backdrop" @click.self="$emit('update:modelValue', false)">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ title }}</h3>
          <button class="btn btn-ghost close-btn" @click="$emit('update:modelValue', false)">&times;</button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
        <div class="modal-footer">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({ modelValue: Boolean, title: String })
defineEmits(['update:modelValue'])
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 30px;
  animation: fadeIn var(--transition-base);
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.modal-content {
  background: var(--color-surface);
  width: 600px;
  max-width: 95%;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  animation: slideIn var(--transition-slow);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}
@keyframes slideIn {
  from { transform: translateY(-50px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  background: var(--color-primary);
  color: white;
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}
.modal-header h3 { margin: 0; font-size: var(--text-lg); }

.close-btn {
  width: 36px;
  min-width: 36px;
  padding: 0;
  color: white;
  font-size: 24px;
  line-height: 1;
}
.close-btn:hover {
  background: rgba(255, 255, 255, 0.14);
}

.modal-body {
  padding: var(--space-5);
  overflow-y: auto;
}

.modal-footer {
  background: var(--color-surface-hover);
  padding: var(--space-4) var(--space-5);
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  flex-shrink: 0;
}
</style>
