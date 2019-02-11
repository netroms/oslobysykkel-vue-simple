const pluginsArr = [];
pluginsArr.push(['inline-replace-variables', {
  __API_KEY__: 'TEST',
  __MAPBOX_ACCESS_TOKEN__: '',
  __PROXY_HOST_PORT__: 'http://localhost:8081'
}]);

if (process.env.NODE_ENV === 'production') {
  pluginsArr.push("transform-remove-console")
}

module.exports = {
  presets: [
    '@vue/app'
  ],
  plugins: pluginsArr
};