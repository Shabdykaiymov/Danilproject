<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from '/http-common.js';

const routes = ref([]);
const loading = ref(true);
const userId = ref(null);
const router = useRouter();

// Проверка, авторизован ли пользователь (по наличию токена в localStorage)
const isAuthenticated = computed(() => !!localStorage.getItem('token'));

// Форматирование продолжительности маршрута в формат "X ч Y мин"
function formatDuration(durationInMinutes) {
  if (durationInMinutes == null) return '—';
  const hours = Math.floor(durationInMinutes / 60);
  const minutes = durationInMinutes % 60;
  return `${hours > 0 ? `${hours} ч ` : ''}${minutes} мин`;
}

// Преобразование даты в читаемый формат "день мес. год"
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).replace(' г.', '');
}

// Переход на страницу деталей маршрута
function goToRouteDetails(routeId) {
  router.push({ name: 'RouteDetails', params: { routeId } });
}

// Извлечение userId из JWT токена, затем запрос к API для получения ID пользователя
async function fetchUserIdFromToken() {
  const token = localStorage.getItem('token');
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const username = payload.sub || payload.username;
    const res = await axios.get(`/${username}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    return res.data.id;
  } catch (e) {
    console.error('Ошибка получения userId:', e);
    return null;
  }
}

// Запрос к Mapbox API для расчета расстояния и времени маршрута по координатам
const getRouteMetrics = async (startCoords, endCoords) => {
  const [startLng, startLat] = startCoords;
  const [endLng, endLat] = endCoords;

  const accessToken = 'pk.eyJ1Ijoia25vdmlrb3YiLCJhIjoiY205NzNmcmpzMDNkdzJyc2FkeXBqYm1yNCJ9.gVSV8RGo5Dz0V0kZQBw5PQ';

  try {
    const res = await fetch(
        `https://api.mapbox.com/directions/v5/mapbox/driving/${startLng},${startLat};${endLng},${endLat}?geometries=geojson&access_token=${accessToken}`
    );
    const data = await res.json();
    const route = data.routes?.[0];
    return {
      distanceKm: route ? (route.distance / 1000).toFixed(1) : null,
      durationMin: route ? Math.round(route.duration / 60) : null,
    };
  } catch (error) {
    console.error('Ошибка при получении данных маршрута:', error);
    return { distanceKm: null, durationMin: null };
  }
};

// Получение и отображение избранных маршрутов текущего пользователя
async function fetchFavorites() {
  try {
    loading.value = true;
    userId.value = await fetchUserIdFromToken();

    if (!userId.value) return;

    // Получение списка ID избранных маршрутов
    const idsResponse = await axios.get(`/list`, {
      params: { userId: userId.value }
    });
    const routeIds = idsResponse.data;

    if (!routeIds || routeIds.length === 0) {
      routes.value = [];
      return;
    }

    // Запрос подробностей по каждому маршруту
    const params = new URLSearchParams();
    routeIds.forEach(id => params.append('routeIds', id));

    const detailsResponse = await axios.get(`/routes-by-ids?${params.toString()}`);
    const rawRoutes = detailsResponse.data || [];

    // Обогащение маршрутов данными о расстоянии, продолжительности и изображением
    const enrichedRoutes = await Promise.all(
        rawRoutes.map(async (route) => {
          const startCoordinates = [route.start_lng, route.start_lat];
          const endCoordinates = [route.end_lng, route.end_lat];

          let distance = null;
          let duration = null;

          if (startCoordinates?.length === 2 && endCoordinates?.length === 2) {
            try {
              const { distanceKm, durationMin } = await getRouteMetrics(startCoordinates, endCoordinates);
              distance = distanceKm;
              duration = durationMin;
            } catch (e) {
              console.warn('Ошибка при получении маршрута от Mapbox:', e);
            }
          }

          // Загрузка изображения маршрута
          let image = null;
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

          return {
            ...route,
            favorite: true,
            image,
            distance,
            duration
          };
        })
    );

    routes.value = enrichedRoutes;
  } catch (error) {
    console.error('Ошибка при загрузке избранных маршрутов:', error);
  } finally {
    loading.value = false;
  }
}

// Загружаем избранные маршруты после монтирования компонента, если пользователь авторизован
onMounted(() => {
  if (isAuthenticated.value) {
    fetchFavorites();
  } else {
    loading.value = false;
  }
});
</script>



<template>
  <div class="routes-page">
    <div class="routes-header">
      <h1 class="routes-title">Избранные маршруты</h1>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Загружаем ваши избранные маршруты...</p>
    </div>

    <div v-else-if="routes.length === 0" class="empty-state">
      <div class="empty-illustration">❤️</div>
      <h3>Нет избранных маршрутов</h3>
      <p>Добавьте маршруты в избранное, чтобы они появились здесь</p>
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

<style scoped>
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