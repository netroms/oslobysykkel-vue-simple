const pluginsArr = [];
pluginsArr.push(['inline-replace-variables', {
  __API_KEY__: 'API__KEY__REPLACE'
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