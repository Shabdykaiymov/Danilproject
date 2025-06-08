<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2>Вход в аккаунт</h2>
        <p>Введите свои данные для входа</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="input-group">
          <label for="name">Имя пользователя</label>
          <input
              v-model="username"
              type="text"
              id="username"
              placeholder="Ваше имя пользователя"
              required
          >
        </div>
        <div class="input-group">
          <label for="password">Пароль</label>
          <input
              v-model="password"
              type="password"
              id="password"
              placeholder="••••••••"
              required
          >
        </div>

        <button type="submit" class="auth-submit">
          <span>Войти</span>
          <span class="arrow">→</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>Ещё нет аккаунта? <router-link to="/register">Зарегистрируйтесь</router-link></p>
      </div>
    </div>

    <div class="auth-decoration">
      <div class="decoration-circle"></div>
      <div class="decoration-circle"></div>
    </div>
  </div>
</template>

<script setup>
// 📦 Импорт нужных модулей
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import authService from '/src/services/authScript.js';
import { useAuthStore } from "@/services/auth.js";

// 📝 Локальные переменные для формы авторизации
const username = ref('');
const password = ref('');

// 🚦 Навигация
const router = useRouter();

// 🔐 Хранилище авторизации
const auth = useAuthStore();

// 🚀 Обработка отправки формы входа
const handleLogin = async () => {
  try {
    // Выполняем запрос на вход и получаем токен
    const token = await authService.login(username.value, password.value);
    console.log(token);

    if (token) {
      // Расшифровываем токен и извлекаем имя пользователя
      const payload = JSON.parse(atob(token.split('.')[1]));
      const nameFromToken = payload.sub || payload.username;

      // Сохраняем имя пользователя в store
      auth.setUsername(nameFromToken);

      // Загружаем роль пользователя
      await auth.fetchRole();

      // Перенаправляем на главную страницу
      await router.push('/');
    }
  } catch (error) {
    // Обработка ошибок авторизации
    alert('Ошибка при входе: ' + (error.response?.data || error.message));
  }
};
</script>



<style scoped>
.auth-container {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
  padding-top: 70px; /* учитываем высоту хедера */
}

.auth-card {
  flex: 1;
  max-width: 450px;
  margin: 1px 233px;
  padding: 2.5rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  z-index: 1;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-header h2 {
  font-size: 1.8rem;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.auth-header p {
  color: #64748b;
  font-size: 0.95rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-group label {
  font-size: 0.9rem;
  color: #475569;
  font-weight: 500;
}

.input-group input {
  padding: 0.8rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.input-group input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.auth-submit {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.9rem;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.auth-submit:hover {
  background: #4338ca;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.arrow {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.auth-submit:hover .arrow {
  transform: translateX(3px);
}

.auth-footer {
  text-align: center;
  margin-top: 2rem;
  color: #64748b;
  font-size: 0.9rem;
}

.auth-footer a {
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;
}

.auth-decoration {
  position: fixed;
  top: 0;
  right: 0;
  width: 40%;
  height: 100vh;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  opacity: 0.1;
}

.decoration-circle:nth-child(1) {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
}

.decoration-circle:nth-child(2) {
  width: 500px;
  height: 500px;
  bottom: -200px;
  right: -200px;
}

@media (max-width: 768px) {
  .auth-decoration {
    display: none;
  }

  .auth-card {
    margin: 1rem;
    padding: 1.5rem;
  }
}
</style>