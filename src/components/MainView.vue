<template>
  <div class="main-view">
    <header class="header">
      <h1 class="title">RouteMate</h1>
      <p class="subtitle">Планируйте свои путешествия легко и удобно</p>
    </header>

    <section class="search-section">
      <h2>Поиск маршрута</h2>
      <div class="input-group">
        <input
            v-model="fromCity"
            type="text"
            placeholder="📍 Откуда (город или адрес)"
            class="input"
        />
        <input
            v-model="toCity"
            type="text"
            placeholder="🏁 Куда (город или адрес)"
            class="input"
        />
        <button @click="findRoute" class="btn-search">
          Найти маршрут
          <span class="icon">→</span>
        </button>
      </div>
    </section>

    <section class="features">
      <h2>Возможности сервиса</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🗺️</div>
          <h3>Планирование маршрутов</h3>
          <p>Создавайте оптимальные маршруты между городами и достопримечательностями</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">⏱️</div>
          <h3>Расчет времени</h3>
          <p>Узнайте примерное время в пути для разных видов транспорта</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">❤️</div>
          <h3>Избранное</h3>
          <p>Сохраняйте любимые места и маршруты для быстрого доступа</p>
        </div>
      </div>
    </section>

    <section class="about">
      <h2>О проекте</h2>
      <p>RouteMate - это сервис для планирования путешествий, который помогает туристам и путешественникам оптимально выстраивать свои маршруты. Наш сервис использует современные технологии для предоставления актуальной информации.</p>
      <p>Начните планировать свое следующее приключение прямо сейчас!</p>
    </section>

    <footer class="footer">
      <p>© 2025 RouteMate. Все права защищены.</p>
      <div class="footer-links">
        <a href="#">Политика конфиденциальности</a>
        <a href="#">Условия использования</a>
        <a href="#">Контакты</a>
      </div>
    </footer>

    <!-- Decorative background -->
    <div class="main-decoration">
      <div class="main-circle circle-1"></div>
      <div class="main-circle circle-2"></div>
    </div>
  </div>
</template>

<script setup>
// 🛠 Импорты необходимых модулей
import { ref } from 'vue'
import router from '@/router/router.js'
import axios from '/http-common.js' // подключаем настроенный экземпляр axios

// 📌 Переменные для ввода пользователем городов
const fromCity = ref('')
const toCity = ref('')

// 🔍 Функция поиска маршрута
const findRoute = async () => {
  // Проверяем, что оба поля заполнены
  if (fromCity.value && toCity.value) {
    try {
      // Получаем токен из localStorage
      const token = localStorage.getItem('token')

      // Готовим параметры запроса
      const config = {
        params: {
          startLocation: fromCity.value,
          endLocation: toCity.value
        }
      }

      // Если есть токен — добавляем заголовок авторизации
      if (token) {
        config.headers = {
          Authorization: `Bearer ${token}`
        }
      }

      // Выполняем GET-запрос на маршрут
      const response = await axios.get('/route/search', config)

      // Проверяем и извлекаем маршрут (если он в виде массива)
      const foundRoute = Array.isArray(response.data) ? response.data[0] : response.data

      if (foundRoute?.id) {
        // Если маршрут найден — переходим на страницу маршрута
        await router.push({ name: 'RouteDetails', params: { routeId: foundRoute.id } })

        // Очищаем поля после перехода
        fromCity.value = ''
        toCity.value = ''
      } else {
        alert('Маршрут не найден.')
      }
    } catch (error) {
      // Ошибка запроса
      console.error(error)
      alert('Ошибка при поиске маршрута. Попробуйте позже.')
    }
  } else {
    // Предупреждение если поля не заполнены
    alert('Пожалуйста, введите оба города.')
  }
}
</script>


<style scoped>
.main-view {
  max-width: 1200px;
  padding: 2rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 3rem;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header {
  text-align: center;
  padding: 2rem 0;
}

.title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 1.2rem;
  color: #374151;
  max-width: 600px;
  margin: 0 auto;
}

.search-section {
  background: #f9fafb;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  color: #1f2937;
}

.search-section h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 500px;
  margin: 0 auto;
}

.input {
  padding: 0.8rem 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.btn-search {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background-color: #2563eb;
  color: white;
  padding: 0.8rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
}

.btn-search:hover {
  background-color: #1d4ed8;
  transform: translateY(-2px);
}

.icon {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.btn-search:hover .icon {
  transform: translateX(3px);
}

.features {
  margin-top: 2rem;
}

.features h2 {
  text-align: center;
  margin-bottom: 2rem;
  font-size: 1.8rem;
  color: #1f2937;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
  color: #1f2937;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  margin-bottom: 0.5rem;
  color: #1e40af;
}

.about {
  background: #f9fafb;
  padding: 2rem;
  border-radius: 12px;
  line-height: 1.6;
  color: #1f2937;
}

.about h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
}

.footer {
  text-align: center;
  padding: 2rem 0;
  border-top: 1px solid #e5e7eb;
  margin-top: 2rem;
  color: #374151;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.footer-links a {
  color: #4b5563;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #2563eb;
}

/* Decorative gradient background */
.main-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.main-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  opacity: 0.08;
  filter: blur(60px);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 500px;
  height: 500px;
  bottom: -200px;
  left: -150px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .title {
    font-size: 2rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .footer-links {
    flex-direction: column;
    gap: 0.5rem;
  }

  .main-decoration {
    display: none;
  }
}
</style>
