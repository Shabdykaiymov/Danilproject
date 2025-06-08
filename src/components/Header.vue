<script setup>
// 🛂 Импорт хранилища авторизации и роутера
import { useAuthStore } from '@/services/auth.js';
import { onMounted } from 'vue';
import router from "@/router/router.js";

// 💾 Получение состояния авторизации
const auth = useAuthStore();

// 🔐 Выход пользователя из системы
const logout = () => {
  auth.logout();
  router.push('/login'); // Перенаправление на страницу входа
};

// 🔄 При монтировании — загружаем имя пользователя из токена
onMounted(() => {
  auth.loadUsernameFromToken();
});
</script>


<template>
  <header class="app-header">
    <router-link to="/" active-class="active">
      <h1 class="logo">RouteMate</h1>
    </router-link>

    <div class="auth-buttons">
      <li v-if="!auth.username">
        <router-link to="/login" class="nav-link" active-class="active">
          <span class="icon">🔑</span>
          <span class="text">Вход</span>
        </router-link>
      </li>
      <li v-else class="user-info">
        <div class="user-controls">
          <div class="nav-link">
            <span class="icon">👤</span>
            <span class="text">{{ auth.username }}</span>
          </div>
          <button @click="logout" class="logout-btn">Выйти</button>
        </div>
      </li>
    </div>
  </header>
</template>


<style scoped>
.user-controls {
  display: flex;
  align-items: center;
}

.logout-btn {
  margin-right: 30px;
  padding: 0.4rem 1rem;
  background-color: transparent;
  border: 1px solid white;
  border-radius: 8px;
  color: white;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: scale(1.05);
}


.icon {
  font-size: 1.6rem;
  margin-right: 12px;
  text-align: center;
}

.text {
  font-size: 1.2rem;
  padding-right: 15px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 0.8rem 1rem;
  margin: 0.3rem 0;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s ease;
}


.nav-link:hover {
  /* убрано изменение фона и цвет текста */
  transform: none;
  color: rgba(255, 255, 255, 0.8);
  background: transparent;
}
.nav-link.active {
  background: rgba(0, 201, 255, 0.2);
  color: white;
  font-weight: 500;
}


.logo {
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(90deg, #00c9ff 0%, #92fe9d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  pointer-events: none; /* делаем логотип некликабельным, если не нужен переход */
}

.app-header {
  position: fixed;
  top: 0;
  left: 0;
  height: 70px; /* высота хедера */
  width: 100%;
  background: linear-gradient(180deg, #2c3e50 0%, #1a2530 100%);
  color: white;
  padding-left: 31px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 2rem;
  font-weight: bold;
  backdrop-filter: blur(10px);
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
  z-index: 1000; /* выше сайдбара */
}


.auth-buttons {
  display: flex;
  gap: 1.5rem;
}

</style>
