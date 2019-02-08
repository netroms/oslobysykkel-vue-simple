
# Oslobysykkel API proxy

Dette prosjektet proxier data fra oslybysykkel sitt API og sammenstiller data fra /stations og /availability til et endepunkt.

## Prerequisites

1. Maven 3.5 or newer
2. Java 11 or newer
3. Docker 17 or newer to build and run docker images
4. Kubernetes minikube v0.24 or newer to deploy to Kubernetes (or access to a K8s 1.7.4 or newer cluster)
5. Kubectl 1.7.4 or newer to deploy to Kubernetes

Verify prerequisites
```
java -version
mvn --version
docker --version
minikube version
kubectl version --short
```

## Configure api key

Insert you api key in "src/main/resources/application.yaml"


## Build

```
mvn package
```

## Start the application

```
java -jar target/api-proxy-server.jar
```


## Try health and metrics

```
curl -s -X GET http://localhost:8081/health
{"outcome":"UP",...
. . .

# Prometheus Format
curl -s -X GET http://localhost:8081/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8081/metrics
{"base":...
. . .

```

## Build the Docker Image

```
docker build -t api-proxy-server target
```

## Start the application with Docker

```
docker run --rm -p 8081:8081 api-proxy-server:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```
kubectl cluster-info                # Verify which cluster
kubectl get pods                    # Verify connectivity to cluster
kubectl create -f target/app.yaml   # Deply application
kubectl get service quickstart-se  # Get service info
```
