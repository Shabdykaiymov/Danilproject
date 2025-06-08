<script setup>
import {ref, onMounted, nextTick} from 'vue';
import {useRoute} from 'vue-router';
import axios from '/http-common.js';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import { useAuthStore } from '@/services/auth.js'
const auth = useAuthStore()

const routeData = ref(null);
const routeId = useRoute().params.routeId;
const mapContainer = ref(null);
let map = null;
const distance = ref(null);
const duration = ref(null);
const points = ref(null);
const commentData = ref(null);
const comment = ref(null);
const userId = ref(null);
const userData = ref(null);
const createAt = new Date().toISOString().split('.')[0] + "Z";
const isAuthenticated = ref(false);

const showEditModal = ref(false);
const editField = ref('');
const editValue = ref('');

function openEditModal(field) {
  if (!routeData.value) return;

  editField.value = field;
  if (field === 'start') editValue.value = routeData.value.startLocation;
  if (field === 'end') editValue.value = routeData.value.endLocation;
  if (field === 'description') editValue.value = routeData.value.description;

  showEditModal.value = true;
}

async function deleteComment(id) {
  console.log('ID комментария для удаления:', id); // ← ВАЖНО
  if (!id) {
    alert("ID комментария отсутствует");
    return;
  }

  const confirmDelete = confirm("Вы уверены, что хотите удалить этот комментарий?");
  if (!confirmDelete) return;

  try {
    const response = await axios.delete(`/del/${id}`);
    console.log(response);
    await fetchComments();
  } catch (error) {
    console.error('Ошибка при удалении комментария:', error);
    alert('Не удалось удалить комментарий');
  }
}


async function applyEdit() {
  if (!routeData.value) return;

  const updatedData = {
    ...routeData.value,
    [editFieldToKey(editField.value)]: editValue.value,
  };

  try {
    await axios.put(`/put/${routeId}`, updatedData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    routeData.value = {...updatedData};
    showEditModal.value = false;
  } catch (err) {
    console.error('Ошибка при сохранении:', err);
    alert('Не удалось сохранить изменения.');
  }
}

function editFieldToKey(field) {
  if (field === 'start') return 'startLocation';
  if (field === 'end') return 'endLocation';
  if (field === 'description') return 'description';
  return field;
}


// 📥 Получение комментариев и данных пользователей
async function fetchComments() {
  try {
    const response = await axios.get(`/get-comment/${routeId}`);
    commentData.value = response.data;
    console.log("Comment response", response);
    const token = localStorage.getItem('token');
    const userMap = {};

    for (const comment of commentData.value) {
      const uid = comment.userId;
      if (!userMap[uid]) {
        try {
          const res = await axios.get(`/user/${uid}`);
          userMap[uid] = res.data;
        } catch (userErr) {
          console.warn(`Ошибка получения данных пользователя ${uid}:`, userErr);
        }
      }
    }

    userData.value = userMap;
  } catch (e) {
    console.error('Ошибка при загрузке комментариев:', e);
  }
}

// 🔐 Получение ID пользователя из токена
async function fetchUserId() {
  const token = localStorage.getItem('token');

  if (!token) {
    isAuthenticated.value = false;
    return;
  }

  const payload = JSON.parse(atob(token.split('.')[1]));
  const username = payload.sub || payload.username;

  try {
    const res = await axios.get(`/${username}`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    userId.value = res.data.id;
    userData.value = res.data;
    isAuthenticated.value = true;
  } catch (err) {
    isAuthenticated.value = false;
    console.error('Ошибка при получении пользователя:', err);
    alert('Не удалось получить данные пользователя');
  }
}

// 📝 Отправка комментария
async function sendComment() {
  if (!comment.value) return;

  try {
    const formData = new FormData();
    formData.append('routeId', routeId);
    formData.append('userId', userId.value);
    formData.append('comment', comment.value);
    formData.append('createAt', createAt);

    await axios.post(`/create-comment`, formData);
    comment.value = '';
    await fetchComments();
  } catch (e) {
    console.error('Ошибка при отправке комментария:', e);
  }
}

// 🚚 Получение данных маршрута и точек маршрута
async function fetchRouteDetails() {
  try {
    const response = await axios.get(`/route/details/${routeId}`);
    routeData.value = response.data;

    const responsePoint = await axios.get(`/point/${routeId}`);
    points.value = responsePoint.data
        .filter(point => point.longitude && point.latitude)
        .map(point => ({
          ...point,
          longitude: point.latitude,  // Исправление координат
          latitude: point.longitude
        }));

    if (routeData.value.start_lat && routeData.value.start_lng &&
        routeData.value.end_lat && routeData.value.end_lng) {
      await nextTick();
      initializeMap(
          [routeData.value.start_lng, routeData.value.start_lat],
          [routeData.value.end_lng, routeData.value.end_lat]
      );
    }
  } catch (error) {
    console.error('Ошибка при получении данных маршрута:', error);
  }
}

// 🗺 Инициализация карты Mapbox
function initializeMap(startCoords, endCoords) {
  mapboxgl.accessToken = 'pk.eyJ1Ijoia25vdmlrb3YiLCJhIjoiY205NzNmcmpzMDNkdzJyc2FkeXBqYm1yNCJ9.gVSV8RGo5Dz0V0kZQBw5PQ';

  if (!mapContainer.value) return;

  mapContainer.value.style.height = '400px';
  mapContainer.value.style.width = '100%';

  if (map) {
    map.remove();
    map = null;
  }

  map = new mapboxgl.Map({
    container: mapContainer.value,
    style: 'mapbox://styles/mapbox/streets-v11',
    center: calculateCenter(startCoords, endCoords),
    zoom: 10
  });

  map.on('load', () => {
    new mapboxgl.Marker({color: '#3FB1CE'}).setLngLat(startCoords).addTo(map);
    new mapboxgl.Marker({color: '#E74C3C'}).setLngLat(endCoords).addTo(map);
    addRoutePoints();
    fetchRoute(startCoords, endCoords);
  });
}

// 📍 Добавление пользовательских маркеров на карту
function addMarker(lng, lat, name) {
  const el = document.createElement('div');
  el.className = 'custom-marker';
  el.style.fontSize = '20px';
  el.style.textAlign = 'center';

  const icons = {
    'Еда': '🍔',
    'Заправка': '⛽',
    'Туалет': '🚽',
    'Видовая точка': '🌄',
    'Точка интереса': '📍',
    'Start': '📍',
    'End': '🏁',
    'default': '❓'
  };

  el.innerHTML = icons[name] || icons['default'];

  new mapboxgl.Marker({
    element: el,
    anchor: 'bottom'
  }).setLngLat([lng, lat]).addTo(map);
}

// 📌 Добавление точек маршрута на карту
function addRoutePoints() {
  if (!points.value || !map) return;

  const bounds = new mapboxgl.LngLatBounds();

  points.value.forEach(point => {
    bounds.extend([point.longitude, point.latitude]);
    addMarker(point.longitude, point.latitude, point.name || point.type);
  });

  map.fitBounds(bounds, {
    padding: 50,
    maxZoom: 12
  });
}

// 🧭 Получение маршрута между двумя точками через Mapbox Directions API
async function fetchRoute(startCoords, endCoords) {
  const url = `https://api.mapbox.com/directions/v5/mapbox/driving/${startCoords.join(',')};${endCoords.join(',')}?geometries=geojson&access_token=${mapboxgl.accessToken}`;

  try {
    const res = await fetch(url);
    const data = await res.json();
    const route = data.routes[0].geometry;
    const routeDistance = data.routes[0].distance;
    const routeDuration = data.routes[0].duration;

    distance.value = (routeDistance / 1000).toFixed(2);
    duration.value = Math.round(routeDuration / 60);

    if (map.getSource('route')) {
      map.removeLayer('route');
      map.removeSource('route');
    }

    map.addSource('route', {
      type: 'geojson',
      data: {
        type: 'Feature',
        geometry: route
      }
    });

    map.addLayer({
      id: 'route',
      type: 'line',
      source: 'route',
      layout: {
        'line-join': 'round',
        'line-cap': 'round'
      },
      paint: {
        'line-color': '#00c9ff',
        'line-width': 4
      }
    });

    const bounds = new mapboxgl.LngLatBounds();
    route.coordinates.forEach(coord => bounds.extend(coord));
    map.fitBounds(bounds, {
      padding: 50,
      maxZoom: 12
    });
  } catch (error) {
    console.error('Ошибка при получении маршрута:', error);
  }
}

// 🎯 Иконка в зависимости от типа точки
function getPointIcon(type) {
  const icons = {
    'Еда': '🍔',
    'Заправка': '⛽',
    'Туалет': '🚽',
    'Видовая точка': '🌄',
    'Точка интереса': '📍',
    'Start': '🟢',
    'End': '🏁',
    'default': '❓'
  };
  return icons[type] || icons['default'];
}

// ⌛ Форматирование продолжительности в часы и минуты
function formatDuration(durationInMinutes) {
  if (durationInMinutes == null) return '—';
  const hours = Math.floor(durationInMinutes / 60);
  const minutes = durationInMinutes % 60;
  return `${hours > 0 ? `${hours} ч ` : ''}${minutes} мин`;
}

// 🧭 Расчёт центра между стартовой и конечной точками
function calculateCenter(start, end) {
  return [
    (start[0] + end[0]) / 2,
    (start[1] + end[1]) / 2
  ];
}

// 🏁 Инициализация при монтировании компонента
onMounted(() => {
  fetchRouteDetails();
  fetchUserId();
  fetchComments();
  auth.fetchRole();

});
</script>


<template>
  <div class="container">
    <div v-if="routeData" class="route-card">
      <div class="route-header">
        <h2>
          📍 {{ routeData.startLocation }}
            <button v-if="auth.isAdmin" @click="openEditModal('start')">✏️</button>
          <span class="arrow">→</span>
          🏁 {{ routeData.endLocation }}
            <button v-if="auth.isAdmin" @click="openEditModal('end')">✏️</button>
        </h2>

        <p class="description">
          {{ routeData.description }}
            <button v-if="auth.isAdmin" @click="openEditModal('description')">✏️ Изменить</button>
        </p>

        <div v-if="showEditModal" class="modal-overlay">
          <div class="modal-content">
            <h3>Редактировать
              {{ editField === 'start' ? 'начальную точку' : editField === 'end' ? 'конечную точку' : 'описание' }}</h3>
            <textarea v-model="editValue" rows="4" style="width: 100%"></textarea>
            <div class="modal-buttons">
              <button @click="applyEdit">💾 Сохранить</button>
              <button @click="showEditModal = false">❌ Отмена</button>
            </div>
          </div>
        </div>
      </div>

      <div class="stats">
        <div class="stat-box">
          <span class="stat-icon">🚗</span>
          <span class="stat-label">Дистанция</span>
          <strong>{{ distance }} км</strong>
        </div>
        <div class="stat-box">
          <span class="stat-icon">⏱</span>
          <span class="stat-label">Время</span>
          <strong>{{ formatDuration(duration) }} </strong>
        </div>
      </div>

      <div class="map" ref="mapContainer"></div>

      <div class="points-section" v-if="points && points.length">
        <h3>📌 Точки маршрута</h3>
        <div class="points-grid">
          <div
              v-for="point in points"
              :key="point.id"
              class="point-card"
              :class="point.type"
          >
            <div class="icon">
              {{ getPointIcon(point.name || point.type) }}
            </div>
            <div class="info">
              <h4>{{ point.name || point.type }}</h4>
              <p>{{ point.description || 'Описание отсутствует' }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="comments-section">
        <h3>💬 {{ commentData?.length || 0 }} комментариев</h3>

        <!-- 📝 Форма ввода -->
        <div v-if="isAuthenticated" class="comment-form">
          <textarea v-model="comment" placeholder="Напишите комментарий..." rows="3"></textarea>
          <button @click="sendComment">Отправить</button>
        </div>

        <!-- 🚫 Неавторизованный пользователь -->
        <div v-else class="not-auth">
          <p>🚫 Только авторизованные пользователи могут оставлять комментарии.</p>
          <router-link to="/login" class="login-btn">Войти</router-link>
        </div>

        <!-- 📜 Список комментариев -->
        <div class="comment-list" v-if="commentData && commentData.length">
          <div class="comment-item" v-for="comment in commentData" :key="comment.id">
            <div class="comment-avatar">👤</div>
            <div class="comment-content">
              <div class="comment-meta">
                <span class="username">Пользователь #{{ userData[comment.userId]?.username || 'Неизвестный' }}</span>
                <span class="timestamp">{{ new Date(comment.createAt).toLocaleString() }}</span>
                <button
                    v-if="auth.isAdmin || userId === comment.userId"
                    @click="deleteComment(comment.id)"
                    class="delete-button"
                >
                  🗑 Удалить
                </button>
              </div>
              <p>{{ comment.comment }}</p>
              <!-- Только для админа или владельца комментария -->

            </div>
          </div>
        </div>

        <div v-else>
          <p>Комментариев пока нет. Будьте первым!</p>
        </div>
      </div>

    </div>

    <div v-else class="loading">
      <div class="spinner"></div>
      <p>Загрузка маршрута...</p>
    </div>
  </div>
</template>


<style scoped>
.delete-button {
  background-color: transparent;
  color: red;
  border: none;
  cursor: pointer;
  font-size: 14px;
  margin-left: 10px;
}


.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.modal-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem 1rem;
  font-family: 'Inter', sans-serif;
}

.route-card {
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.route-header h2 {
  font-size: 1.6rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.route-header .start,
.route-header .end {
  color: #4f46e5;
}

.arrow {
  margin: 0 0.5rem;
  color: #6b7280;
}

.description {
  font-size: 1rem;
  color: #6b7280;
  margin-top: 0.5rem;
}

.stats {
  display: flex;
  justify-content: space-around;
  background: #f9fafb;
  border-radius: 12px;
  padding: 1rem;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  color: #374151;
}

.stat-icon {
  font-size: 1.4rem;
}

.stat-label {
  font-size: 0.9rem;
  color: #6b7280;
}

.map {
  width: 100%;
  height: 450px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.points-section {
  margin-top: 2rem;
}

.points-section h3 {
  font-size: 1.5rem;
  color: #1f2937;
  margin-bottom: 1rem;
  font-weight: 600;
}

.points-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.point-card {
  background: #fff;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.point-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.point-card .icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.point-card .info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 0.3rem;
}

.point-card .info p {
  font-size: 0.9rem;
  color: #6b7280;
}

/* Цветовые акценты по типу */
.point-card.Еда {
  border-left: 6px solid #f59e0b;
}

.point-card.Заправка {
  border-left: 6px solid #10b981;
}

.point-card.Туалет {
  border-left: 6px solid #6366f1;
}

.point-card.Видовая {
  border-left: 6px solid #3b82f6;
}

.point-card.Точка {
  border-left: 6px solid #f43f5e;
}


.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem 0;
  color: #6b7280;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 5px solid #e5e7eb;
  border-top: 5px solid #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .stats {
    flex-direction: column;
    gap: 1rem;
  }

  .route-header h2 {
    font-size: 1.3rem;
  }

  .map {
    height: 350px;
  }
}

/* Стили для блока комментариев */
.comments-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
  color: black;
}

.not-auth {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
}

.not-auth a {
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;
}

.not-auth a:hover {
  text-decoration: underline;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin-bottom: 1.5rem;
}

.comment-form textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-height: 80px;
  resize: vertical;
}

.comment-form button {
  align-self: flex-end;
  background: #4f46e5;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.comment-form button:hover {
  background: #4338ca;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.comment-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
}

.comment-avatar {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.comment-content {
  flex-grow: 1;
}

.comment-meta {
  justify-content: space-between;
  margin-bottom: 0.4rem;
  font-size: 0.9rem;
  color: #64748b;
}

.username {
  font-weight: 500;
  color: #1e293b;
  margin-right: 404px;
}

.comment-content p {
  margin: 0;
  color: #334155;
}


</style>
