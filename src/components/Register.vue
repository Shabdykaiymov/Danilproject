<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2>Создать аккаунт</h2>
        <p>Заполните форму для регистрации</p>
      </div>
      <p v-if="errors.general" class="error-message" style="margin-top: 1rem;">{{ errors.general }}</p>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="input-group">
          <label for="username">Имя пользователя</label>
          <input
              v-model="username"
              type="text"
              id="username"
              placeholder="Ваше имя пользователя"
              required
              :class="{ 'input-error': errors.username }"
              @blur="checkUsernameAvailability"
          >
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>


        <div class="input-group">
          <label for="name">Имя</label>
          <input
              v-model="firstName"
              type="text"
              id="firstname"
              placeholder="Ваше имя"
              required
          >
          <div v-if="errors.firstName" class="error-message">{{ errors.firstName }}</div>

        </div>

        <div class="input-group">
          <label for="name">Фамилия</label>
          <input
              v-model="lastName"
              type="text"
              id="lastName"
              placeholder="Ваша фамилия"
              required
          >
          <div v-if="errors.lastName" class="error-message">{{ errors.lastName }}</div>

        </div>

        <div class="input-group">
          <label for="email">Email</label>
          <input
              v-model="email"
              type="email"
              id="email"
              placeholder="example@mail.com"
              required
          >
          <div v-if="errors.email" class="error-message">{{ errors.email }}</div>

        </div>

        <div class="input-group">
          <label for="password">Пароль</label>
          <input
              v-model="password"
              type="password"
              id="password"
              placeholder="••••••••"
              required
              minlength="8"
          >
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>

        <div class="input-group">
          <label for="confirmPassword">Подтвердите пароль</label>
          <input
              v-model="confirmPassword"
              type="password"
              id="confirmPassword"
              placeholder="••••••••"
              required
          >
        </div>

        <button type="submit" class="auth-submit">
          <span>Зарегистрироваться</span>
          <span class="arrow">→</span>
        </button>
      </form>

      <div class="auth-footer">
        <p>Уже есть аккаунт?
          <router-link to="/login">Войдите</router-link>
        </p>
      </div>
    </div>

    <div class="auth-decoration">
      <div class="decoration-circle"></div>
      <div class="decoration-circle"></div>
      <div class="decoration-circle"></div>
    </div>
  </div>
</template>

<script setup>
// 🔧 Импорты
import axios from '/http-common.js'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

// 📋 Реактивные переменные для полей формы
const username = ref('')
const firstName = ref('')
const lastName = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

// ⚠️ Ошибки по полям
const errors = ref({})

// 🚫 Флаг, что логин уже занят
const usernameExists = ref(false)

// 🔄 Роутер
const router = useRouter()

// 📤 Обработка формы регистрации
const handleRegister = async () => {
  errors.value = {}

  // ❗ Валидации до отправки
  if (password.value.length < 8) {
    errors.value.password = 'Пароль должен содержать минимум 8 символов'
    return
  }

  if (password.value !== confirmPassword.value) {
    errors.value.confirmPassword = 'Пароли не совпадают'
    return
  }

  if (usernameExists.value) {
    errors.value.username = 'Пользователь с таким именем уже существует'
    return
  }

  // 📦 Подготовка данных
  const userData = {
    login: username.value,
    code: password.value,
    mail: email.value,
    name: firstName.value,
    surname: lastName.value
  }

  try {
    // ⬆️ Отправка данных на сервер
    const response = await axios.post('/save-user', userData)
    alert('Регистрация прошла успешно!')

    if (response.status === 201) {
      await router.push('/login')
    }
  } catch (error) {
    // ❌ Обработка ошибок
    const responseData = error.response?.data

    if (error.response?.status === 400 && responseData?.errors) {
      responseData.errors.forEach((err) => {
        const field = err.field?.toLowerCase()
        const message = err.defaultMessage || 'Ошибка'

        if (field?.includes('login')) errors.value.username = message
        else if (field?.includes('code')) errors.value.password = message
        else if (field?.includes('mail')) errors.value.email = message
        else if (field === 'name') errors.value.firstName = message
        else if (field === 'surname') errors.value.lastName = message
        else errors.value.general = message
      })
    } else if (typeof responseData === 'string') {
      errors.value.general = responseData
    } else {
      errors.value.general = 'Произошла неизвестная ошибка при регистрации'
    }
  }
}

// 🔍 Проверка доступности логина
const checkUsernameAvailability = async () => {
  if (username.value.trim() !== '') {
    try {
      const response = await axios.get(`/check/${username.value}`)
      if (response.status === 200 && response.data === true) {
        errors.value.username = 'Этот пользователь уже существует'
        usernameExists.value = true
      } else {
        errors.value.username = ''
        usernameExists.value = false
      }
    } catch {
      errors.value.username = ''
      usernameExists.value = false
    }
  }
}

</script>


<style scoped>

.auth-container {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
  padding-top: 70px;
}
.input-error {
  border-color: red;
  background-color: #f8d7da;
}

.error-message {
  color: red;
  font-size: 12px;
  margin-top: 5px;
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
  opacity: 0.1;
}

.decoration-circle:nth-child(1) {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
}

.decoration-circle:nth-child(2) {
  width: 500px;
  height: 500px;
  bottom: -200px;
  right: -200px;
  background: linear-gradient(135deg, #ec4899, #f97316);
}

.decoration-circle:nth-child(3) {
  width: 400px;
  height: 400px;
  top: 50%;
  right: -150px;
  background: linear-gradient(135deg, #10b981, #3b82f6);
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