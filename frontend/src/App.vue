<template>
  <AppToast />
  <router-view />
</template>

<script setup>
import AppToast from './components/AppToast.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Microsoft YaHei', sans-serif;
}
</style>
