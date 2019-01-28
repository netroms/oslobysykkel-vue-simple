FROM node:10.15.0
LABEL maintainer "Morten Svanaes"

ARG APIKEY
WORKDIR /app

RUN git clone https://github.com/netroms/oslobysykkel-vue-simple.git .
RUN mv babel.config.js babel.config.js.bak ; cat babel.config.js.bak | sed -e "s/API__KEY__REPLACE/${APIKEY}/g" > babel.config.js
RUN npm install
RUN npm install -g serve
RUN npm run build
CMD serve -s dist