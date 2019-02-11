<template>
  <div id="app">
    <div id="mapCont">
      <mapbox :access-token="accessToken"
              :map-options="{
                style: 'mapbox://styles/mapbox/dark-v9',
                center: [10.7264695, 59.9190443],
                zoom: 13
              }"
              :scale-control="{
                show: true,
                position: 'top-left'
              }"
              :fullscreen-control="{
                show: true,
                position: 'top-left'
              }"
              @map-load="mapLoaded"
              @geolocate-error="geolocateError"
              @geolocate-geolocate="geolocate"></mapbox>
    </div>
  </div>
</template>

<script>
  /*eslint no-console: ["warn", { allow: ["log"] }] */
  import Vue from 'vue';
  import Mapbox from 'mapbox-gl-vue';
  import axios from "axios";
  import VueAxios from "vue-axios";


  Vue.use(VueAxios, axios);

  const PROXY_HOST_PORT = __PROXY_HOST_PORT__;
  const MAPBOX_ACCESS_TOKEN = __MAPBOX_ACCESS_TOKEN__;

  export default {
    name: 'app',
    components: {
      Mapbox
    }
    , data: function data() {
      return {
        accessToken: MAPBOX_ACCESS_TOKEN,
        refreshInterval: -1,
        status: '',
        timer: '',
        lastUpdated: '',
        stations: null,
        loading: true,
        errored: false
      };
    },

    methods: {
      getStatus: function () {
        return axios.get(PROXY_HOST_PORT + '/api/status');
      },
      getAvailability: function () {
        return axios.get(PROXY_HOST_PORT + '/api/stativer');
      },
      mapLoaded(map) {
        console.log("map loaded() ");
        let geolocateControl = new mapboxgl.GeolocateControl({
          positionOptions: {
            enableHighAccuracy: true
          },
          trackUserLocation: true
        });
        map.addControl(geolocateControl);

        let all = [];
        let _this = this;
        Promise.all([this.getStatus(), this.getAvailability()])
        .then(function ([status, availability]) {
          _this.stations = availability.data.stations;
          for (var v of _this.stations) {
            let loc = v.location.split(",");
            let lat = loc[0];
            let long = loc[1];
            let properties = {
              'hasBikes': (v.availability.bikes > 0),
              'locks': v.availability.locks,
              'bikes': v.availability.bikes,
              'title': v.title,
              'icon': 'bicycle'

            };
            all.push({
              'type': 'Feature',
              'geometry': {
                'type': 'Point',
                'coordinates': [long, lat]
              },
              'properties': properties
            })
          }

          let stationsCont = {
            'type': 'geojson',
            'data': {
              'type': 'FeatureCollection',
              'features': all
            },
            cluster: true,
            clusterMaxZoom: 14, // Max zoom to cluster points on
            clusterRadius: 30
          };

          map.addSource("stations", stationsCont);

          map.addLayer({
            id: 'clusters',
            type: 'circle',
            source: stationsCont,

            filter: ["has", "point_count"],
            paint: {
              'circle-color': {
                property: 'point_count',
                type: 'interval',
                stops: [
                  [2, '#494445'],
                  [5, '#747171'],
                  [10, '#848c86']
                ]
              },
              'circle-radius': {
                property: 'point_count',
                type: 'interval',
                stops: [
                  [0, 5],
                  [2, 12],
                  [5, 17],
                  [10, 20]
                ]
              }
            }
          });

          map.addLayer({
            id: "unclustered-point",
            type: "circle",
            source: stationsCont,
            filter: ["!", ["has", "point_count"]],
            paint: {
              // "circle-color": "#ff6e00",
              "circle-radius": 6,
              "circle-stroke-width": 1,
              "circle-stroke-color": "#989898",
              'circle-color': {
                property: 'bikes',
                type: 'interval',
                stops: [
                  [0, '#414241'],
                  [1, '#0c7600'],
                ]
              },
            },
          });

          map.addLayer({
            id: "lock-count",
            type: "symbol",
            source: stationsCont,
            // filter: ["has", "locks"],
            filter: ['gt', ['get', 'locks'], 9],
            layout: {
              "text-field": "{locks}",
              "text-font": ["DIN Offc Pro Medium", "Arial Unicode MS Bold"],
              "text-size": 30
            }
          });

          // map.addLayer({
          //   id: 'clustersActive',
          //   type: 'circle',
          //   source: stationsCont,
          //   filter: ["has", "point_count"],
          //   paint: {
          //     "circle-radius": 6,
          //     "circle-stroke-width": 1,
          //     // "circle-stroke-color": "#989898",
          //
          //     'circle-color': {
          //       property: 'bikes',
          //       // type: 'interval',
          //       stops: [
          //         [0, "#214AED"],
          //         [1, "#44ed32"],
          //       ]
          //     }
          //
          //   }
          // });

          map.on('click', 'clusters', function (e) {
            var features = map.queryRenderedFeatures(e.point, {layers: ['clusters']});
            var clusterId = features[0].properties.cluster_id;
            map.getSource('stations').getClusterExpansionZoom(clusterId, function (err, zoom) {
              if (err) {
                return;
              }
              map.easeTo({
                center: features[0].geometry.coordinates,
                zoom: zoom
              });
            });
          });

          map.on('mouseenter', 'clusters', function () {
            map.getCanvas().style.cursor = 'pointer';
          });
          map.on('mouseleave', 'clusters', function () {
            map.getCanvas().style.cursor = '';
          });

          // Create a popup, but don't add it to the map yet.
          var popup = new mapboxgl.Popup({
            closeButton: false,
            closeOnClick: false
          });

          map.on('mouseenter', 'unclustered-point', function (e) {
            map.getCanvas().style.cursor = 'pointer';
            popup.setLngLat(e.features[0].geometry.coordinates)
            .setHTML(
                "" +
                "<h2>" + e.features[0].properties.title + "</h2>" +
                "<h3>  Bikes: " + e.features[0].properties.bikes + "</h3>" +
                "<h3>  Locks: " + e.features[0].properties.locks + "</h3>" +
                "")
            .addTo(map);
          });

          map.on('mouseleave', 'unclustered-point', function () {
            map.getCanvas().style.cursor = '';
            popup.remove();
          });

          geolocateControl.trigger();

        }).catch(function (error) {
          console.log(error);
          _this.errored = true;
        }).finally(function () {
          return _this.loading = false;
        });

      },
      mapClicked(map, e) {
        //alert('Map Clicked!');
      },
      geolocateError(control, positionError) {
        console.log(positionError);
      },
      geolocate(control, position) {
        console.log(`User position: ${position.coords.latitude}, ${position.coords.longitude}`);
      }
    }

  }
</script>

<style>

  #mapCont {
    width: 800px;
  }

  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  body {
    margin: 0;
    padding: 0;
  }

  #map {
    position: absolute;
    top: 0;
    bottom: 0;
    width: 100%;
  }
</style>
