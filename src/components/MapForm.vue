<script setup>
// 🔧 Импорты
import { ref, onMounted, watch } from 'vue'
import mapboxgl from 'mapbox-gl'
import 'mapbox-gl/dist/mapbox-gl.css'
import axios from '/http-common.js'

// 🔑 Ключ доступа к Mapbox
mapboxgl.accessToken = 'pk.eyJ1Ijoia25vdmlrb3YiLCJhIjoiY205NzNmcmpzMDNkdzJyc2FkeXBqYm1yNCJ9.gVSV8RGo5Dz0V0kZQBw5PQ'

// 📌 Переменные маршрута
const name = ref('')
const description = ref('')
const startLat = ref(null)
const startLng = ref(null)
const endLat = ref(null)
const endLng = ref(null)
const startLocation = ref('')
const endLocation = ref('')
const routeImage = ref(null)

// 📍 Переменные точек
const points = ref([])
const showPointModal = ref(false)
const currentPointCoords = ref(null)
const pointName = ref('')
const pointDescription = ref('')
const isRouteDrawn = ref(false)
const userId = ref(null)

// 🗺️ Контейнер карты и связанные переменные
const mapContainer = ref(null)
let map
let markers = []
let popup = null

// 📷 Обработка изображения маршрута
function onRouteImageChange(e) {
  const file = e.target.files[0]
  if (file) {
    routeImage.value = file
  }
}

// ➕ Добавление маркера на карту
function addMarker(lng, lat, type, pointId = null) {
  const el = document.createElement('div')
  el.className = 'custom-marker'

  const icons = {
    'Еда': '🍔',
    'Заправка': '⛽',
    'Туалет': '🚽',
    'Видовая точка': '🌄',
    'Точка интереса': '📍',
    'Start': '📍',
    'End': '🏁',
    'Current Location': '📡'
  }

  el.innerHTML = icons[type] || '❓'

  const marker = new mapboxgl.Marker(el)
      .setLngLat([lng, lat])
      .addTo(map)

  // Добавляем слушатель для клика по точке
  if (pointId) {
    marker.getElement().dataset.pointId = pointId
    marker.getElement().addEventListener('click', () => showPointDetails(pointId))
  }

  markers.push(marker)
}

// ❌ Удаление всех маркеров с карты
function clearMarkers() {
  markers.forEach(m => m.remove())
  markers = []
}

// 🔄 Очистка маршрута и полей
function clearRoute() {
  startLat.value = null
  startLng.value = null
  endLat.value = null
  endLng.value = null
  startLocation.value = ''
  endLocation.value = ''
  name.value = ''
  description.value = ''
  routeImage.value = null
  points.value = []
  isRouteDrawn.value = false
  clearMarkers()

  // Удаление линии маршрута
  if (map.getSource('route')) {
    map.removeLayer('route')
    map.removeSource('route')
  }
}

// 👆 Обработка клика по карте
function handleMapClick(e) {
  const { lng, lat } = e.lngLat

  if (!startLat.value) {
    // Ставим начальную точку
    startLat.value = lat
    startLng.value = lng
    addMarker(lng, lat, 'Start')
  } else if (!endLat.value) {
    // Ставим конечную точку и рисуем маршрут
    endLat.value = lat
    endLng.value = lng
    addMarker(lng, lat, 'End')
    drawRoute([startLng.value, startLat.value], [lng, lat])
  } else if (isRouteDrawn.value) {
    // Добавляем пользовательскую точку
    currentPointCoords.value = { lng, lat }
    showPointModal.value = true
  }
}

// 🧾 Отображение информации о точке
function showPointDetails(pointId) {
  const point = points.value.find(p => p.id === pointId)
  if (!point) return

  if (popup) popup.remove()

  popup = new mapboxgl.Popup({ offset: 25 })
      .setLngLat([point.longitude, point.latitude])
      .setHTML(`
      <div class="point-popup">
        <h4>${point.name}</h4>
        <p>${point.description || 'Нет описания'}</p>
      </div>
    `)
      .addTo(map)
}

// 💾 Сохранение пользовательской точки
async function savePoint() {
  if (!currentPointCoords.value || !pointName.value) {
    alert('Укажите название точки')
    return
  }

  const newPoint = {
    name: pointName.value,
    description: pointDescription.value,
    latitude: parseFloat(currentPointCoords.value.lat.toFixed(6)),
    longitude: parseFloat(currentPointCoords.value.lng.toFixed(6)),
  }

  points.value.push(newPoint)
  addMarker(currentPointCoords.value.lng, currentPointCoords.value.lat, pointName.value)
  closePointModal()
}

// ❌ Закрытие модального окна точки
function closePointModal() {
  showPointModal.value = false
  pointName.value = ''
  pointDescription.value = ''
  currentPointCoords.value = null
}

// 📈 Построение маршрута между двумя точками
async function drawRoute(start, end) {
  const url = `https://api.mapbox.com/directions/v5/mapbox/driving/${start.join(',')};${end.join(',')}?geometries=geojson&access_token=${mapboxgl.accessToken}`
  const res = await fetch(url)
  const data = await res.json()
  const route = data.routes[0]?.geometry

  if (map.getSource('route')) {
    map.removeLayer('route')
    map.removeSource('route')
  }

  map.addSource('route', {
    type: 'geojson',
    data: { type: 'Feature', geometry: route }
  })

  map.addLayer({
    id: 'route',
    type: 'line',
    source: 'route',
    layout: { 'line-join': 'round', 'line-cap': 'round' },
    paint: { 'line-color': '#3b82f6', 'line-width': 5 }
  })

  isRouteDrawn.value = true

  // Центрируем карту на маршруте
  const bounds = new mapboxgl.LngLatBounds()
  route.coordinates.forEach(coord => bounds.extend(coord))
  map.fitBounds(bounds, { padding: 50, maxZoom: 14 })
}

// 📍 Геокодинг адресов
async function geocodeAddress(address) {
  const res = await fetch(
      `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(address)}.json?` +
      new URLSearchParams({
        access_token: mapboxgl.accessToken,
        proximity: '74.5698,42.8746',
        country: 'KG'
      })
  )
  const data = await res.json()
  return data.features[0]?.geometry?.coordinates
}

// 🧪 Попробовать нарисовать маршрут по вводу адресов
async function tryDrawFromInput() {
  if (startLocation.value && endLocation.value) {
    const start = await geocodeAddress(startLocation.value)
    const end = await geocodeAddress(endLocation.value)

    if (start && end) {
      startLng.value = start[0]
      startLat.value = start[1]
      endLng.value = end[0]
      endLat.value = end[1]

      clearMarkers()
      addMarker(start[0], start[1], 'Start')
      addMarker(end[0], end[1], 'End')

      await drawRoute(start, end)
    }
  }
}

// 🔁 Слежение за вводом адресов
watch([startLocation, endLocation], () => {
  tryDrawFromInput()
})

// 📍 Получить текущее местоположение пользователя
async function locateMe() {
  if (!navigator.geolocation) {
    alert('Геолокация не поддерживается.')
    return
  }

  navigator.geolocation.getCurrentPosition(async position => {
    const { latitude, longitude } = position.coords
    startLat.value = latitude
    startLng.value = longitude

    const res = await fetch(
        `https://api.mapbox.com/geocoding/v5/mapbox.places/${longitude},${latitude}.json?access_token=${mapboxgl.accessToken}`
    )
    const data = await res.json()
    const place = data.features[0]?.place_name || 'Текущее местоположение'
    startLocation.value = place

    addMarker(longitude, latitude, 'Current Location')
    map.setCenter([longitude, latitude])
  }, err => {
    alert('Не удалось получить геолокацию: ' + err.message)
  })
}

// 👤 Получить ID текущего пользователя
async function fetchUserId() {
  const token = localStorage.getItem('token')
  if (!token) return

  const payload = JSON.parse(atob(token.split('.')[1]))
  const username = payload.sub || payload.username

  try {
    const res = await axios.get(`/${username}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    userId.value = res.data.id
  } catch (err) {
    console.error('Ошибка при получении пользователя:', err)
    alert('Не удалось получить данные пользователя')
  }
}

// 📤 Отправка маршрута на сервер
async function submitRoute() {
  if (!name.value || !startLat.value || !endLat.value) {
    alert('Заполните все обязательные поля')
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    alert('Вы не авторизованы!')
    return
  }

  const formData = new FormData()
  formData.append('name', name.value)
  formData.append('description', description.value)
  formData.append('startLat', startLat.value)
  formData.append('startLng', startLng.value)
  formData.append('endLat', endLat.value)
  formData.append('endLng', endLng.value)
  formData.append('startLocation', startLocation.value)
  formData.append('endLocation', endLocation.value)
  formData.append('userId', userId.value)
  formData.append('points', JSON.stringify(points.value))

  if (routeImage.value) {
    formData.append('finishImage', routeImage.value)
  }

  try {
    await axios.post('/save-route', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })
    alert('Маршрут сохранён!')
    clearRoute()
  } catch (err) {
    alert('Ошибка: ' + (err.response?.data?.message || err.message))
  }
}

// 🗺️ Инициализация карты
function initMap() {
  map = new mapboxgl.Map({
    container: mapContainer.value,
    style: 'mapbox://styles/mapbox/streets-v11',
    center: [74.5698, 42.8746],
    zoom: 10
  })

  map.on('click', handleMapClick)
}

// ▶️ Выполняем при загрузке компонента
onMounted(() => {
  initMap()
  fetchUserId()
})
</script>


<template>
  <div class="form-wrapper">
    <h2>Создать маршрут</h2>
    <form @submit.prevent="submitRoute" class="route-form">
      <div class="form-section">
        <div class="form-group">
          <label>Название маршрута *</label>
          <input type="text" v-model="name" placeholder="Введите название" required>
        </div>

        <div class="form-group">
          <label>Описание маршрута</label>
          <textarea v-model="description" placeholder="Добавьте описание"></textarea>
        </div>

        <div class="form-group">
          <label>Изображение маршрута</label>
          <input type="file" @change="onRouteImageChange" accept="image/*">
        </div>
      </div>

      <div class="location-section">
        <div class="form-group">
          <label>Начальная точка *</label>
          <div class="input-with-action">
            <input type="text" v-model="startLocation" placeholder="Адрес или место" required>
            <button type="button" @click="locateMe" class="geo-btn">📍</button>
          </div>
        </div>

        <div class="form-group">
          <label>Конечная точка *</label>
          <input type="text" v-model="endLocation" placeholder="Адрес или место" required>
        </div>
      </div>

      <div class="map-container">
        <div ref="mapContainer" class="map"></div>
        <div v-if="isRouteDrawn" class="map-hint">
          👆 Кликните на карту, чтобы добавить точку маршрута
        </div>
      </div>

      <div v-if="points.length > 0" class="points-section">
        <h3>Добавленные точки ({{ points.length }})</h3>
        <div class="points-list">
          <div v-for="(point, index) in points" :key="point.id" class="point-item">
            <div class="point-info">
              <span class="point-type">{{ point.name }}</span>
              <span class="point-desc">{{ point.description || 'Без описания' }}</span>
            </div>
            <button @click="points.splice(index, 1); clearMarkers(); renderMarkers()" class="delete-btn">
              Удалить
            </button>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="button" @click="clearRoute" class="secondary-btn">
          Очистить
        </button>
        <button type="submit" class="primary-btn">
          Сохранить маршрут
        </button>
      </div>
    </form>

    <!-- Модальное окно добавления точки -->
    <div v-if="showPointModal" class="modal-overlay">
      <div class="point-modal">
        <h3>Добавить точку маршрута</h3>

        <div class="form-group">
          <label>Тип точки *</label>
          <select v-model="pointName" required>
            <option value="">Выберите тип</option>
            <option>Еда</option>
            <option>Заправка</option>
            <option>Туалет</option>
            <option>Видовая точка</option>
            <option>Точка интереса</option>
          </select>
        </div>

        <div class="form-group">
          <label>Описание</label>
          <textarea v-model="pointDescription" placeholder="Добавьте описание"></textarea>
        </div>

        <div class="modal-actions">
          <button @click="closePointModal" class="secondary-btn">Отмена</button>
          <button @click="savePoint" class="primary-btn">Добавить точку</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.route-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-section, .location-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #374151;
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 0.8rem 1rem;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  font-size: 1rem;
  background: #F9FAFB;
  width: 100%;
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.input-with-action {
  position: relative;
}

.geo-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  font-size: 1.2rem;
}

.map-container {
  position: relative;
}

.map {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #E5E7EB;
}

.map-hint {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.points-section {
  background: #F9FAFB;
  padding: 1.5rem;
  border-radius: 12px;
}

.points-section h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.points-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.point-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.point-info {
  display: flex;
  flex-direction: column;
}

.point-type {
  font-weight: 600;
  color: #1F2937;
}

.point-desc {
  font-size: 0.85rem;
  color: #6B7280;
}

.delete-btn {
  padding: 0.5rem 1rem;
  background: #FEE2E2;
  color: #B91C1C;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
}

.delete-btn:hover {
  background: #FECACA;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.primary-btn {
  padding: 0.9rem 1.5rem;
  background: #4F46E5;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.primary-btn:hover {
  background: #4338CA;
}

.secondary-btn {
  padding: 0.9rem 1.5rem;
  background: #F3F4F6;
  color: #4B5563;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  background: #E5E7EB;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.point-modal {
  background: white;
  padding: 2rem;
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.point-modal h3 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #1F2937;
}


.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}



@media (max-width: 768px) {
  .form-section, .location-section {
    grid-template-columns: 1fr;
  }

  .form-wrapper {
    padding: 1rem;
  }

  .map {
    height: 400px;
  }
}
</style>