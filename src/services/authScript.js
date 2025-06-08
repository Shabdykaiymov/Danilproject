import api from '/http-common.js';
import { useAuthStore } from '@/services/auth.js'; // Импортируем хранилище

export default {
    /**
     * Выполняет вход пользователя, отправляя запрос на сервер.
     * Сохраняет токен и имя пользователя в localStorage и обновляет состояние в хранилище.
     * @param {string} username - Имя пользователя
     * @param {string} password - Пароль пользователя
     * @returns {Promise<string>} - Токен авторизации
     * @throws {Error} - Если не удалось выполнить вход
     */
    async login(username, password) {
        try {
            // Отправляем запрос на сервер для получения токена
            const response = await api.post('/login', { username, password });
            const token = response.data.token;

            // Декодируем токен и проверяем его срок действия
            const decodedToken = this.decodeToken(token);
            const expTime = decodedToken.exp * 1000; // Время истечения токена в миллисекундах
            const currentTime = Date.now();

            // Если токен истек, выводим ошибку и выполняем выход
            if (currentTime > expTime) {
                console.error("Токен истек. Пожалуйста, войдите снова.");
                this.logout();
                return;
            }

            // Сохраняем токен и имя пользователя в localStorage
            localStorage.setItem('token', token);
            localStorage.setItem('username', username);

            // Обновляем состояние в хранилище
            const authStore = useAuthStore();
            authStore.setUsername(username);

            return token;
        } catch (error) {
            console.error('Login failed', error);
            throw error;
        }
    },

    /**
     * Декодирует JWT токен и извлекает его payload.
     * @param {string} token - JWT токен
     * @returns {Object} - Декодированное содержимое токена (payload)
     */
    decodeToken(token) {
        const base64Url = token.split('.')[1]; // Извлекаем часть токена с данными (payload)
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/'); // Преобразуем в корректный base64
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join('')); // Декодируем из base64 в строку

        return JSON.parse(jsonPayload); // Возвращаем объект JSON с данными
    },

    /**
     * Выполняет выход пользователя, удаляя данные из localStorage.
     */
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
    }
};
