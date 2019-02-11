import Vue from 'vue'
import App from './App.vue'
import Mapbox from "mapbox-gl";
import VueMapbox from "vue-mapbox";

Vue.use(VueMapbox, {mapboxgl: Mapbox});

Vue.config.productionTip = false;

new Vue({
  render: h => h(App),
}).$mount('#app');