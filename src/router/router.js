import {createRouter, createWebHistory} from 'vue-router';
import MainView from "@/components/MainView.vue";
import MapForm from "@/components/MapForm.vue";
import FindRoutes from "@/components/FindRoutes.vue";
import ViewRoutes from "@/components/ViewRoutes.vue";
import Login from "@/components/Login.vue";
import Register from "@/components/Register.vue";
import FavoriteRoutes from "@/components/FavoriteRoutes.vue";

const routes = [
    {
        path: '/',
        name: 'MainView',
        component: MainView
    },
    {
        path: '/map-form',
        name: 'MapForm',
        component: MapForm
    },
    {
        path: '/route/:routeId',
        name: 'RouteDetails',
        component: FindRoutes
    },
    {
        path: '/routes',
        name: 'ViewRoutes',
        component: ViewRoutes
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
    {
        path: '/favorite',
        component: FavoriteRoutes,
    }
];


const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;