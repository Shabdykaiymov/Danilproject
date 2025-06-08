<template>
  <div class="routes-page">
    <div class="routes-header">
      <h1 class="routes-title">Маршруты</h1>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Загружаем ваши маршруты...</p>
    </div>

    <div v-else-if="routes.length === 0" class="empty-state">
      <div class="empty-illustration">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
          <circle cx="12" cy="10" r="3"></circle>
        </svg>
      </div>
      <h3>Маршрутов пока нет</h3>
      <p>Ожидайте появления новых маршрутов, после начните свое путешествовие</p>
    </div>

    <div v-else class="routes-grid">
      <div
          v-for="route in routes"
          :key="route.id"
          class="route-card"
          @click="goToRouteDetails(route.id)"
      >
        <div class="card-left">
          <div class="card-header">
            <div class="route-name">
              <h3>{{ route.name || 'Без названия' }}</h3>
              <span
                  v-if="isAuthenticated"
                  class="favorite-icon"
                  :class="{ active: route.favorite }"
                  @click.stop="toggleFavorite(route)"
              >
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path
            :d="route.favorite ? 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' : 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z'"></path>
    </svg>
</span>


            </div>
            <span class="date-badge">{{ formatDate(route.createdAt) }}</span>
          </div>

          <div class="route-path">
            <div class="location">
              <div class="location-marker start"></div>
              <span>{{ route.startLocation || 'Точка А' }}</span>
            </div>
            <div class="path-line">
              <div class="path-dots"></div>
            </div>
            <div class="location">
              <div class="location-marker end"></div>
              <span>{{ route.endLocation || 'Точка Б' }}</span>
            </div>
          </div>

          <div class="route-stats">
            <div class="stat">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              </svg>
              <span>{{ route.distance ? `${route.distance} км` : '—' }}</span>
            </div>
            <div class="stat">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>{{ formatDuration(route.duration) }}</span>
            </div>
          </div>

          <button class="details-button" @click.stop="goToRouteDetails(route.id)">
            Подробнее
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7"></path>
            </svg>
          </button>
            <button v-if="auth.isAdmin" class="delete-button" @click.stop="deleteRoute(route.id)">
              Удалить
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 6h18M9 6v12a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2V6"></path>
                <path d="M10 11v6M14 11v6"></path>
              </svg>
            </button>
        </div>

        <div class="card-right" v-if="route.image">
          <div class="route-image-container">
            <img :src="route.image" alt="Route image" class="route-image"/>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>


<script>
import axios from '/http-common.js';
import {useRouter} from 'vue-router';
import {ref} from "vue";
import { useAuthStore } from '@/services/auth.js'


export default {
  name: 'RoutesPage',
  data() {
    return {
      routes: [],
      loading: true,
      user_id: null,
      userData: null,
      userFavorites: [], // Список избранных маршрутов для текущего пользователя
      auth: useAuthStore()
    };
  },
  computed: {
    // Проверка, авторизован ли пользователь
    isAuthenticated() {
      return !!localStorage.getItem('token');
    }
  },
  methods: {
    /**
     * Получает ID пользователя из токена.
     * Если токен существует, расшифровывает его, чтобы получить данные пользователя.
     */
    async fetchUserId() {
      const token = localStorage.getItem('token');

      if (!token) {
        console.warn('Токен отсутствует — пользователь не авторизован');
        return;
      }

      // Расшифровываем JWT (если в нем есть username)
      const payload = JSON.parse(atob(token.split('.')[1]));
      const username = payload.sub || payload.username; // зависит от структуры токена

      try {
        const res = await axios.get(`/${username}`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        this.user_id = res.data.id;
        this.userData = res.data;
        return this.user_id;
      } catch (err) {
        console.error('Ошибка при получении пользователя:', err);
        alert('Не удалось получить данные пользователя');
      }
    },

    /**
     * Загружает список избранных маршрутов пользователя.
     * После этого обновляет статус избранных маршрутов в текущем списке маршрутов.
     */
    async fetchFavorites() {
      if (!this.user_id) {
        console.error('user_id is not set or empty');
        return;
      }

      try {
        const response = await axios.get(`/list?userId=${this.user_id}`);
        this.userFavorites = response.data;

        // Обновляем статус избранного для маршрутов
        this.routes.forEach(route => {
          route.favorite = this.userFavorites.includes(route.id);
        });
      } catch (err) {
        console.error('Ошибка при загрузке избранных маршрутов:', err);
      }
    },

    /**
     * Обрабатывает клик по маршруту, чтобы добавить или удалить его из избранного.
     * В зависимости от текущего состояния маршрута, либо добавляется, либо удаляется из избранных.
     */
    async toggleFavorite(route) {
      console.log('Toggle favorite for route:', route.id, 'Current favorite status:', route.favorite);
      const isFavorite = route.favorite;
      try {
        if (isFavorite) {
          console.log('Removing from favorites');
          await axios.delete('/delete', {
            params: {
              routeId: route.id,
              userId: this.user_id
            }
          });
          this.userFavorites = this.userFavorites.filter(id => id !== route.id);
        } else {
          console.log('Adding to favorites');
          await axios.post('/add', {
            routeId: route.id,
            userId: this.user_id
          }, {
            headers: {
              'Content-Type': 'application/json'
            }
          });
          this.userFavorites.push(route.id);
        }

        route.favorite = !isFavorite;
      } catch (err) {
        console.error('Ошибка при обновлении избранного:', err);
      }
    },

    /**
     * Загружает все маршруты с сервера, обогащает их дополнительной информацией (расстояние, время, изображение),
     * а затем обновляет список маршрутов.
     */
    async fetchAllRoutes() {
      try {
        this.loading = true;

        // Проверяем, авторизован ли пользователь
        if (!this.user_id) {
          await this.fetchUserId();
          await this.fetchFavorites();
        }

        const response = await axios.get('/all-route');
        const rawRoutes = response.data || [];

        // Обогащаем маршруты дополнительными данными
        const enrichedRoutes = await Promise.all(
            rawRoutes.map(async (route) => {
              const startCoordinates = [route.start_lng, route.start_lat];
              const endCoordinates = [route.end_lng, route.end_lat];

              let distance = null;
              let duration = null;

              // Получаем расстояние и продолжительность маршрута с помощью Mapbox API
              if (startCoordinates?.length === 2 && endCoordinates?.length === 2) {
                try {
                  const {distanceKm, durationMin} = await this.getRouteMetrics(
                      startCoordinates,
                      endCoordinates
                  );
                  distance = distanceKm;
                  duration = durationMin;
                } catch (e) {
                  console.warn('Ошибка при получении маршрута от Mapbox:', e);
                }
              }

              let image = null;
              if (route.id) {
                try {
                  const imageResponse = await axios.get(`/route/${route.id}/image`, {
                    responseType: 'arraybuffer'
                  });

                  const base64 = btoa(
                      new Uint8Array(imageResponse.data).reduce(
                          (data, byte) => data + String.fromCharCode(byte), ''
                      )
                  );

                  let imageType = 'jpeg';
                  const byteArray = new Uint8Array(imageResponse.data);
                  if (byteArray[0] === 0x89 && byteArray[1] === 0x50) {
                    imageType = 'png';
                  }

                  image = `data:image/${imageType};base64,${base64}`;
                } catch (error) {
                  console.error(`Ошибка загрузки изображения для маршрута ${route.id}:`, error);
                }
              }

              return {
                ...route,
                distance,
                duration,
                image,
                favorite: this.userFavorites.includes(route.id) // Устанавливаем статус избранного
              };
            })
        );

        this.routes = enrichedRoutes;

        // После загрузки маршрутов обновляем избранные маршруты
        await this.fetchFavorites();
      } catch (error) {
        console.error('Ошибка при загрузке маршрутов:', error);
      } finally {
        this.loading = false;
      }
    },
    async deleteRoute(routeId) {
      if (!confirm('Вы уверены, что хотите удалить этот маршрут?')) return;

      try {
        await axios.delete(`/delete-route/${routeId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });

        // Удаляем маршрут из списка на фронте
        this.routes = this.routes.filter(route => route.id !== routeId);
        alert('Маршрут успешно удалён');
      } catch (error) {
        console.error('Ошибка при удалении маршрута:', error);
        alert('Не удалось удалить маршрут');
      }
    },

    /**
     * Запрашивает API Mapbox для расчета расстояния и времени между двумя точками.
     * @param {Array} startCoords - Координаты начальной точки маршрута (долгота, широта)
     * @param {Array} endCoords - Координаты конечной точки маршрута (долгота, широта)
     */
    async getRouteMetrics(startCoords, endCoords) {
      const [startLng, startLat] = startCoords;
      const [endLng, endLat] = endCoords;

      const accessToken = 'pk.eyJ1Ijoia25vdmlrb3YiLCJhIjoiY205NzNmcmpzMDNkdzJyc2FkeXBqYm1yNCJ9.gVSV8RGo5Dz0V0kZQBw5PQ';

      const res = await fetch(
          `https://api.mapbox.com/directions/v5/mapbox/driving/${startLng},${startLat};${endLng},${endLat}?geometries=geojson&access_token=${accessToken}`
      );

      const data = await res.json();
      const route = data.routes?.[0];
      return {
        distanceKm: route ? (route.distance / 1000).toFixed(1) : null,
        durationMin: route ? Math.round(route.duration / 60) : null,
      };
    },

    /**
     * Форматирует продолжительность маршрута (в минутах) в формат "часы минуты".
     * @param {Number} durationInMinutes - Продолжительность маршрута в минутах
     */
    formatDuration(durationInMinutes) {
      if (durationInMinutes == null) return '—';
      const hours = Math.floor(durationInMinutes / 60);
      const minutes = durationInMinutes % 60;
      return `${hours > 0 ? `${hours} ч ` : ''}${minutes} мин`;
    },

    /**
     * Форматирует дату маршрута в читаемый вид (например, "24 апр 2025").
     * @param {String} dateString - Дата маршрута в строковом формате
     */
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      }).replace(' г.', '');
    },

    /**
     * Переход на страницу с деталями маршрута.
     * @param {String} routeId - ID маршрута, который нужно показать
     */
    goToRouteDetails(routeId) {
      this.$router.push({name: 'RouteDetails', params: {routeId}});
    }
  },

  mounted() {
    // Загружаем все маршруты при монтировании компонента
    try {
      this.fetchAllRoutes();
      this.auth.fetchRole()  // Загружаем роль пользователя

    } catch (error) {
      console.error('Ошибка инициализации:', error);
      this.loading = false;
    }
  }
};
</script>


<style scoped>
.delete-button {
  margin-top: 8px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 86px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.delete-button:hover {
  background-color: #d9363e;
}


.favorite-icon svg path {
  fill: none;
  stroke: #d1d1d6; /* Цвет для незаполненной звезды */
  transition: all 0.3s ease;
}

.favorite-icon.active svg path {
  fill: #ffcc00; /* Цвет для заполненной звезды */
  stroke: #ffcc00;
}


.routes-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.routes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
}

.routes-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}


.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  height: 300px;
  color: #8e8e93;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(0, 122, 255, 0.2);
  border-top-color: #007AFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 3rem 2rem;
  background-color: #f5f5f7;
  border-radius: 16px;
  color: #636366;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 1rem 0 0.5rem;
  color: #1a1a1a;
}

.empty-state p {
  margin: 0 0 1.5rem;
  max-width: 400px;
}

.empty-illustration {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a5a5b0;
}

.routes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

@media (max-width: 1200px) {
  .routes-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .routes-grid {
    grid-template-columns: 1fr;
  }
}

.route-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  cursor: pointer;
  display: flex;
  flex-direction: row; /* Изменено с column на row */
  border: 1px solid #e5e5ea;
  align-items: center;
}

.route-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #d1d1d6;
}

.card-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.route-image-container {
  width: 180px;
  height: 120px;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid #e5e5ea;
}

.route-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.25rem;
}

.route-name {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.route-name h3 {
  margin: 0;
  font-weight: 600;
  color: #1a1a1a;
}

.favorite-icon {
  color: #d1d1d6;
  transition: color 0.2s ease;
}

.favorite-icon.active {
  color: #ffcc00;
}

.date-badge {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background-color: #f5f5f7;
  border-radius: 12px;
  color: #636366;
}

.route-path {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.location {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  color: #3a3a3c;
}

.location-marker {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.location-marker.start {
  background-color: #007AFF;
}

.location-marker.end {
  background-color: #34C759;
}


.route-image-container {
  flex-shrink: 0;
  width: 150px;
  height: 100px;
  overflow: hidden;
  border-radius: 8px;
}

.route-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.path-line {
  height: 16px;
  width: 2px;
  background-color: #e5e5ea;
  margin-left: 7px;
  position: relative;
}

.path-dots {
  position: absolute;
  top: 0;
  left: 0;
  width: 2px;
  height: 100%;
  background-image: linear-gradient(to bottom, #e5e5ea 30%, transparent 30%);
  background-size: 2px 4px;
}

.route-stats {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #636366;
}

.stat svg {
  color: #8e8e93;
}

.details-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: #007AFF;
  font-weight: 500;
  cursor: pointer;
  padding: 0.5rem 0;
  margin-top: auto;
  align-self: flex-start;
  transition: color 0.2s ease;
}

.details-button:hover {
  color: #0066CC;
}

@media (max-width: 768px) {
  .routes-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .routes-title {
    font-size: 1.75rem;
  }

  .routes-grid {
    grid-template-columns: 1fr;
  }
}
</style>