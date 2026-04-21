<template>
  <Teleport to="body">
    <div v-if="modelValue" class="confirm-overlay">
      <div class="confirm-dialog">
        <h4><i class="fas fa-exclamation-triangle" style="color:#dc3545;margin-right:8px;"></i>确认删除</h4>
        <p>{{ message }}</p>
        <div class="confirm-buttons">
          <button class="btn-cancel" @click="$emit('update:modelValue', false)">取消</button>
          <button class="btn-delete" @click="handleConfirm">删除</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: Boolean,
  message: { type: String, default: '确定要删除该记录吗？此操作不可恢复。' }
})
const emit = defineEmits(['update:modelValue', 'confirm'])

function handleConfirm() {
  emit('confirm')
  emit('update:modelValue', false)
}
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2001;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-dialog {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 350px;
  max-width: 90%;
  animation: scaleIn 0.3s;
}
@keyframes scaleIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

h4 { margin: 0 0 15px 0; color: #333; }
p { margin: 0 0 20px 0; color: #666; font-size: 14px; }

.confirm-buttons { display: flex; justify-content: flex-end; gap: 10px; }

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

.btn-delete {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}
.btn-delete:hover { background: #c82333; }
</style>
