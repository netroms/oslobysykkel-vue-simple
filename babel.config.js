const pluginsArr = [];
pluginsArr.push(['inline-replace-variables', {
  __API_KEY__: '',
  __PROXY_HOST_PORT_API_KEY__: 'http://localhost:8081'
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