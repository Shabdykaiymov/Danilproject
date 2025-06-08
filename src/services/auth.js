import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import axios from '/http-common.js'; // Убедитесь, что путь правильный

export const useAuthStore = defineStore('auth', () => {
    // Состояние для хранения имени пользователя и информации о роли администратора
    const username = ref(null);
    const isAdmin = ref(false);

    /**
     * Устанавливает имя пользователя и сохраняет его в localStorage
     * @param {string} name - Имя пользователя
     */
    const setUsername = (name) => {
        username.value = name;
        localStorage.setItem('username', name);
    };

    /**
     * Загружает имя пользователя из localStorage или из токена (если есть)
     */
    const loadUsernameFromToken = () => {
        const storedUsername = localStorage.getItem('username');
        const token = localStorage.getItem('token');

        // Если имя пользователя сохранено в localStorage, загружаем его
        if (storedUsername) {
            username.value = storedUsername;
        }

        // Если токен существует, пытаемся извлечь имя из его payload
        if (token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const name = payload.sub || payload.username; // Извлекаем имя из токена
                setUsername(name);
            } catch (e) {
                console.error('Ошибка при декодировании токена:', e);
            }
        }
    };

    /**
     * Запрашивает роль пользователя (админ ли он) с сервера
     * @returns {Promise<void>}
     */
    const fetchRole = async () => {
        const token = localStorage.getItem('token');

        if (!token || !username.value) {
            isAdmin.value = false;
            return;
        }

        try {
            // Получаем роль пользователя по имени из API
            const response = await axios.get(`/role/${username.value}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            // Устанавливаем флаг админа в зависимости от ответа
            isAdmin.value = response.data === true;
        } catch (e) {
            console.error('Ошибка при получении роли:', e);
            isAdmin.value = false;
        }
    };

    /**
     * Выполняет выход пользователя — очищает данные и localStorage
     */
    const logout = () => {
        username.value = null;
        isAdmin.value = false;
        localStorage.removeItem('token');
        localStorage.removeItem('username');
    };

    /**
     * Проверяет, авторизован ли пользователь, основываясь на токене
     * @returns {boolean} - true, если токен действителен и не истек
     */
    const isAuthenticated = computed(() => {
        const token = localStorage.getItem('token');
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const now = Date.now() / 1000;
            // Проверка на истечение срока действия токена
            return payload.exp > now;
        } catch (e) {
            return false;
        }
    });

    // Возвращаем состояния и методы для использования в компоненте
    return {
        username,
        isAdmin,
        setUsername,
        loadUsernameFromToken,
        fetchRole,
        logout,
        isAuthenticated
    };
});
