#!/bin/bash
mvn clean install
eval $(minikube -p minikube docker-env)
docker build -t order-service:1.0 .
kubectl apply -f deployment.yml
kubectl apply -f svc.yml
