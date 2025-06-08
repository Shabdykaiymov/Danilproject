import axios from "axios";
import { createPinia } from 'pinia';
import { useAuthStore } from "@/services/auth.js";

// ⚠️ Вручную создаём экземпляр Pinia
const pinia = createPinia();

// Создаем экземпляр axios с настройками по умолчанию
const api = axios.create({
    baseURL: "http://localhost:8082/api",  // Базовый URL для запросов
    headers: {
        "Content-type": "application/json",  // Заголовки для отправки JSON
    },
});

// Интерсептор запроса: добавляем токен авторизации в заголовки, если он существует
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            // Добавляем токен в заголовок для авторизации
            config.headers["Authorization"] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)  // В случае ошибки запроса, отклоняем промис
);

// Интерсептор ответа: обрабатываем ошибки, например, истечение срока действия токена (401)
api.interceptors.response.use(
    (response) => response,  // Если ответ успешен, возвращаем его
    (error) => {
        // Проверяем код ошибки 401 (неавторизован)
        if (error.response && error.response.status === 401) {
            console.error("Токен истёк. Выходим...");

            // Получаем доступ к хранилищу состояния для выполнения выхода
            const auth = useAuthStore(pinia);
            auth.logout();  // Выполняем выход из системы

            window.location.href = "/login";  // Перенаправляем на страницу логина
        }
        return Promise.reject(error);  // В случае других ошибок, отклоняем промис
    }
);

export default api;
