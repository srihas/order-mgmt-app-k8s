Inventory & Order management microservices based application deployable on Kubernetes - Local(Minikube) and Google cloud
----

This is a sample microservices based application developed in java using Spring boot framework to demonstrate 

* Interaction between different microservices.
* Usage of helm to deploy microservices in Kubernetes cluster - Local(minikube) and on Google cloud.


----
## Tags

`Spring Boot`
`RabbitMQ`
`Postgres`
`Helm`
`Kubernetes`
`minikube`

----
## Usecase:

   * User should be able to add the products to Inventory using REST API. (Persist in DB)
   2. User should be able to take the order based on the product availability in Inventory and generate the invoice using REST API
   3. Once any invoice is generated post the Order to Shipping service using message broker Rabbit MQ
----
## Deploying the applications

Create a kubernetes namespace to deploy services in:

    kubectl create namespace orderapp

 Deploy rabbitmq:
 
    helm install rabbitmq srcharts/rabbitmq -n orderapp

 Deploy postgres:
    
    helm install postgresdb srcharts/postgres -n orderapp

Deploy Inventory-service:

    helm install inventory-service srcharts/inventory-service -n orderapp

Deploy Order-service:

    helm install order-service srcharts/order-service -n orderapp

Deploy Shipping-service:

    helm install shipping-service srcharts/shipping-service -n orderapp

----
##Verify status 
###Local deployment (Minikube) 

    → minikube service list
|-------------|-------------------|---------------------|--------------------------------|
|  NAMESPACE  |       NAME        |     TARGET PORT     |              URL               |
|-------------|-------------------|---------------------|--------------------------------|
| default     | kubernetes        | No node port        |
| kube-system | kube-dns          | No node port        |
| orderapp    | inventory-service |                9200 | http://192.168.99.102:30702    |
| orderapp    | order-service     |                9300 | http://192.168.99.102:32115    |
| orderapp    | postgresdb        |                5432 | http://192.168.99.102:30544    |
| orderapp    | rabbitmq          | amqp/5672           | http://192.168.99.102:30672    |
|             |                   | http/15672          | http://192.168.99.102:31672    |
| orderapp    | shipping-service  | No node port        |
|-------------|-------------------|---------------------|--------------------------------|

####Service URL's:

####inventory-service:

* swagger-url: `http://192.168.99.102:30702/swagger-ui.html`
* api-endpoint: `http://192.168.99.102:30702/inventory/`

####Order-service:

* swagger-url: `http://192.168.99.102:32115/swagger-ui.html`
* api-endpoint: `http://192.168.99.102:32115/orders/` 

####RabbitMQ:

* web-ui: `http://192.168.99.102:31672/`
* credentials: `guest/guest`

####Postgres:

* Credentials:
* ip: `192.168.99.102`
* port: `30544`
* database: `postgresdb`
* username: `postgresadmin`
* password: `admin123`

----
###Google Cloud Console

The hostname and port details for each service can be obtained from "Service & Ingress" section in Kubernetes Engine.

In case of Rabbitmq, ingress can be created post which web-ui can be accessed with the same credentials as above.


----
## changelog
* 28-Apr-2020 Initial commit

----
## Thanks
* [tscharts](https://github.com/technosophos/tscharts)
* [bitnami - Create a Helm chart](https://youtu.be/TJ9hPLn0oAs)
