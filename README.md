# oslobysykkel-vue-simple

## Api Key konfigurasjon
Oslobysykkel ApiKey må settes inn i konfigurasjonsfilen "babel.config.js" under: API__KEY__REPLACE

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

### Installere serve 
```
npm install -g serve
```
### Serve mappen "dist"  man bygger med "npm run build" lokalt
```
serve -s dist
```

## Dockerize og kjør alt i docker
```
docker build . -t origotest --build-arg=APIKEY=__SETT__INN__APIKEY__ ; docker run -it --rm -p 5000:5000 origotest:latest
```