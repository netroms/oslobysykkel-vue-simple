<template>
  <div id="app">

    <h1>Oslo bysykkel - stativer (ledige):</h1>

    <section v-if="errored">
      <p>Noe gikk galt under innlastning av data fra Bysykkel API'et, venligst prøv igjen senere...</p>
    </section>

    <section v-else>
      <h3>{{lastUpdated}}</h3>
      <h4>{{status}}</h4>
      <div v-if="loading">Laster data...</div>
      <div v-else v-for="s in stations" :key="s.id" class="currency">
        {{ s.title }} : {{ s.availability.bikes }} av {{ s.availability.locks }}
      </div>
    </section>
  </div>
</template>

<script>
  /*eslint no-console: ["warn", { allow: ["log"] }] */
  import Vue from 'vue';
  import axios from "axios";
  import VueAxios from "vue-axios";

  Vue.use(VueAxios, axios);

  /*eslint-disable no-undef*/
  const API_KEY = __API_KEY__;
  const PROXY_HOST_PORT = __PROXY_HOST_PORT_API_KEY__;
  /*eslint-enable no-undef*/

  const DEFAULT_REFRESH_INTERVAL_SECONDS = 5;
  const DATE_FORMAT_OPTIONS_NO = {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
    hour: 'numeric', minute: 'numeric', second: 'numeric'
  };

  axios.defaults.headers.common['Client-Identifier'] = API_KEY;

  export default {
    name: 'ProxiedBysykkelListe',
    data: function data() {
      return {
        refreshInterval: -1,
        status: '',
        timer: '',
        lastUpdated: '',
        stations: null,
        loading: true,
        errored: false
      };
    },
    created: function () {
      console.log("created()");

      let _this = this;
      Promise.all([this.getStatus(), this.getAvailability()])
      .then(function ([status, availability]) {
        _this.renderStatusAndRefreshTime(status.data.status, "" + new Date());
        _this.stations = availability.data.stations;
      }).catch(function (error) {
        console.log(error);
        _this.errored = true;
      }).finally(function () {
        return _this.loading = false;
      });
      this.updateRefreshInterval(DEFAULT_REFRESH_INTERVAL_SECONDS);
    },
    methods: {
      renderStatusAndRefreshTime: function (status, refreshTime) {
        let _this = this;
        if (status.all_stations_closed) {
          _this.status = "NB: Alle stativer er fortiden stengt!"
        } else {
          _this.status = ""
        }
        let dateObj = new Date(Date.parse(refreshTime));
        _this.lastUpdated = "(" + dateObj.toLocaleDateString('NB', DATE_FORMAT_OPTIONS_NO) + ")";
      },

      getStatus: function () {
        return axios.get(PROXY_HOST_PORT + '/api/status');
      },
      getAvailability: function () {
        return axios.get(PROXY_HOST_PORT + '/api/stativer');
      },

      refreshData: function () {
        console.log("refreshData()");
        let _this = this;
        Promise.all([this.getStatus(), this.getAvailability()])
        .then(function ([status, availability]) {
          _this.renderStatusAndRefreshTime(status.data.status, "" + new Date());
          _this.stations = availability.data.stations;
        }).catch(function (error) {
          console.log(error);
          _this.errored = true;
        }).finally(function () {
          return _this.loading = false;
        });
      },

      updateRefreshInterval: function (newInterval) {
        console.log("updateRefreshInterval() :" + newInterval);
        if (newInterval !== this.refreshInterval) {
          console.log("updateRefreshInterval() - set new interval: " + newInterval);
          this.clearRefresh();
          this.refreshInterval = newInterval;
          this.timer = setInterval(this.refreshData, newInterval * 1000)
        }
      },
      clearRefresh: function () {
        console.log("clearRefresh()");
        clearInterval(this.timer)
      }
    },
    beforeDestroy() {
      console.log("beforeDestroy()");
      this.clearRefresh();
    },
  }
</script>

<style scoped>

</style>
