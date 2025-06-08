<template>
  <aside class="sidebar">
    <nav class="navigation">
      <ul>
        <li>
          <router-link to="/" class="nav-link" active-class="active">
            <span class="icon">🏠</span>
            <span class="text">Главная</span>
          </router-link>
        </li>
        <li>
          <router-link to="/routes" class="nav-link" active-class="active">
            <span class="icon">🗺️</span>
            <span class="text">Маршруты</span>
          </router-link>
        </li>
        <li>
          <a
              href="#"
              class="nav-link"
              @click="goToFavorites"
          >
            <span class="icon">🗺️</span>
            <span class="text">Избранные маршруты</span>
          </a>
        </li>
        <li v-if="auth.isAdmin">
          <router-link to="/map-form" class="nav-link" active-class="active">
            <span class="icon">❤️</span>
            <span class="text">Создать маршрут</span>
          </router-link>
        </li>
      </ul>
    </nav>

    <div class="route-search">
      <h3 class="search-title">
        <span class="search-icon">🔍</span>
        Поиск маршрута
      </h3>
      <div class="input-group">
        <div class="input-wrapper">
          <input v-model="fromCity" type="text" placeholder="Откуда" class="input" />
          <span class="input-icon">📍</span>
        </div>
        <div class="input-wrapper">
          <input v-model="toCity" type="text" placeholder="Куда" class="input" />
          <span class="input-icon">🏁</span>
        </div>
        <button @click="findRoute" class="btn-search">
          <span>Найти маршрут</span>
          <span class="arrow">→</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import router from '@/router/router.js'
import axios from '/http-common.js'
import { useAuthStore } from '@/services/auth.js'

// Стейт для хранения введенных городов
const fromCity = ref('')
const toCity = ref('')
const route = ref(null)

// Получение информации об авторизации пользователя
const auth = useAuthStore()

// Метод, который загружает данные о пользователе и его роли при монтировании компонента
onMounted(async () => {
  auth.loadUsernameFromToken()  // Загружаем имя пользователя из токена
  await auth.fetchRole()  // Загружаем роль пользователя
})

// Метод для перехода в раздел избранных маршрутов
const goToFavorites = () => {
  if (!auth.isAuthenticated) {
    // Если пользователь не авторизован, перенаправляем на страницу логина
    router.push('/login')
  } else {
    // Если пользователь авторизован, переходим в раздел избранных маршрутов
    router.push('/favorite')
  }
}

// Метод для поиска маршрута между двумя городами
const findRoute = async () => {
  if (fromCity.value && toCity.value) {
    try {
      const token = localStorage.getItem('token')  // Получаем токен из localStorage
      const config = {
        params: {
          startLocation: fromCity.value,  // Начальный город
          endLocation: toCity.value  // Конечный город
        }
      }

      // Если токен существует, добавляем его в заголовки запроса
      if (token) {
        config.headers = {
          Authorization: `Bearer ${token}`
        }
      }

      // Отправляем запрос на сервер для поиска маршрута
      const response = await axios.get('/route/search', config)
      const foundRoute = Array.isArray(response.data) ? response.data[0] : response.data

      // Если маршрут найден, переходим на страницу с деталями маршрута
      if (foundRoute?.id) {
        route.value = foundRoute
        fromCity.value = ''
        toCity.value = ''
        await router.push({ name: 'RouteDetails', params: { routeId: foundRoute.id } })
      } else {
        alert('Маршрут не найден или ID отсутствует.')
      }
    } catch (error) {
      console.error(error)
      alert('Ошибка при поиске маршрута. Попробуйте позже.')
    }
  } else {
    alert('Пожалуйста, введите оба города.')
  }
}
</script>



<style scoped>
.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #2c3e50 0%, #1a2530 100%);
  padding: 1.5rem;
  height: 100vh;
  position: fixed;
  left: 0;

  color: white;
  box-shadow: 4px 0 15px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: all 0.3s ease;
  padding-top: 90px;

}


.navigation ul {
  list-style: none;
  padding: 0;
  margin: 0 0 2rem 0;
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
  background: rgba(255, 255, 255, 0.1);
  color: white;
  transform: translateX(5px);
}

.nav-link.active {
  background: rgba(0, 201, 255, 0.2);
  color: white;
  font-weight: 500;
}

.icon {
  font-size: 1.2rem;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.text {
  font-size: 1rem;
}

.route-search {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 1.2rem;
  margin-bottom: 1.5rem;
  backdrop-filter: blur(5px);
}

.search-title {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
  margin: 0 0 1rem 0;
  color: white;
}

.search-icon {
  margin-right: 10px;
  font-size: 1.2rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.input-wrapper {
  position: relative;
}

.input {
  width: 100%;
  padding: 0.8rem 1rem 0.8rem 2.5rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: white;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.input:focus {
  outline: none;
  border-color: #00c9ff;
  background: rgba(0, 201, 255, 0.1);
  box-shadow: 0 0 0 2px rgba(0, 201, 255, 0.2);
}

.input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1rem;
}

.btn-search {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #00c9ff 0%, #92fe9d 100%);
  color: #1a2530;
  padding: 0.8rem 1.2rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95rem;
  margin-top: 0.5rem;
  transition: all 0.3s ease;
}

.btn-search:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 201, 255, 0.3);
}

.arrow {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.btn-search:hover .arrow {
  transform: translateX(3px);
}


.weather-header h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 500;
}


/* Анимации */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.route-search, .weather-widget {
  animation: fadeIn 0.5s ease-out forwards;
}

/* Адаптивность */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
    padding: 1rem;
  }

  .navigation ul {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
  }

  .nav-link {
    padding: 0.5rem;
  }

  .text {
    display: none;
  }
}
</style>