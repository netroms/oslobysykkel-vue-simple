# oslobysykkel-vue-simple

## Api Key konfigurasjon
Din Oslobysykkel api nøkkel må settes inn i konfigurasjonsfilen "babel.config.js" under: API__KEY__REPLACE

Du kan opprette konto å få api nøkkel her: https://developer.oslobysykkel.no/sign-up

## For å installere dependencies første gang. 
```
npm install
```

### Kompiler og server koden med hot-reloads for utvikling
```
npm run serve
```

### For å kompilere og pakke for distribusjon
```
npm run build
```



## For å "serve" den pakkede distribusjonen lokalt

### Installer pakken 'serve' 
```
npm install -g serve
```
### Serve mappen "dist"  man bygger med "npm run build" lokalt
```
serve -s dist
```

## Alternativt, kjør alt i Docker.
```
docker build . -t origotest --build-arg=APIKEY=__SETT__INN__APIKEY__ ; docker run -it --rm -p 5000:5000 origotest:latest
```
