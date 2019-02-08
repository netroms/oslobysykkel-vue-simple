<template>
  <div id="app">
    <BysykkelListe/>
  </div>
</template>

<script>
  import BysykkelListe from './components/ProxiedBysykkelListe.vue'

  export default {
    name: 'app',
    components: {
      BysykkelListe
    }
  }
</script>

<style>
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
  }
</style>
