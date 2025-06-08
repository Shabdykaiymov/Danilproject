<template>
  <div id="app" class="app-container">
    <Header/>

    <div class="main-content">
      <Sidebar/>

      <div class="map-and-routes">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import Header from '@/components/Header.vue'
import Sidebar from "@/components/Sidebar.vue";

export default {
  name: 'App',
  components: {
    Header,
    Sidebar,

  },
};
</script>

<style>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  font-family: Arial, sans-serif;
}

#app {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.main-content {
  display: flex;
  flex-grow: 1;
  height: 100%;
}

.map-and-routes {
  flex-grow: 1;
margin-left: 290px;
  gap: 1rem;
  padding: 1rem;
  overflow: auto;
  margin-top: 100px;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;

  }

  .map-and-routes {
    flex-direction: column;
  }
}
</style>
