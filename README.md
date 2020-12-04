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
   * User should be able to take the order based on the product availability in Inventory and generate the invoice using REST API
   * Once any invoice is generated post the Order to Shipping service using message broker Rabbit MQ
----
## Deploying the applications

Create a kubernetes namespace to deploy services:

    kubectl create namespace orderapp

Download helm charts private repo:
    
    helm repo add srcharts https://srihas.github.com/charts

Deploy rabbitmq:
 
    helm install rabbitmq srcharts/rabbitmq -n orderapp

Check deployment status of helm chart:

    helm list -n orderapp

Deploy postgres:
    
    helm install postgresdb srcharts/postgres -n orderapp

Deploy Inventory-service:

    helm install inventory-service srcharts/inventory-service -n orderapp

Deploy Order-service:

    helm install order-service srcharts/order-service -n orderapp

Deploy Shipping-service:

    helm install shipping-service srcharts/shipping-service -n orderapp
    



> NOTE: Use the same service names as above, else the service discovery does not work, if any changes are required, change the values in corresponding values.yaml in respective charts and install the dependent services.

----
## Verify status 
### Local deployment (Minikube) 

    → minikube service list

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

#### Check kubectl entities:

    kubectl get all -n orderapp

> In case of errors in deployment of pod, like ImgPullBack etc:

    kubectl describe pod <pod-container-id>


### Service URL's:

#### inventory-service:

* swagger-url: `http://192.168.99.102:30702/swagger-ui.html`
* api-endpoint: `http://192.168.99.102:30702/inventory/`

#### order-service:

* swagger-url: `http://192.168.99.102:32115/swagger-ui.html`
* api-endpoint: `http://192.168.99.102:32115/orders/` 

#### rabbitmq:

* web-ui: `http://192.168.99.102:31672/`
* credentials: `guest/guest`


#### postgres:

* Credentials:
* ip: `192.168.99.102`
* port: `30544`
* database: `postgresdb`
* username: `postgresadmin`
* password: `admin123`

### Verify sending amqp message to rabbitmq:

Install amqp cli client:

    pip install amqp_client_cli
    
Publish message to `messagequeue-exchange`:

    → amqpcli send 192.168.99.102 30672 messagequeue-exchange test -m "Hello there"
    User: "guest"
    Password: guest
    Connecting to queue @ 192.168.99.102:30672... SUCCESS!
    Message successfully published to exchange [messagequeue-exchange]!

----
### Google Cloud Console

The hostname and port details for each service can be obtained from "Service & Ingress" section in Kubernetes Engine.

In case of Rabbitmq, ingress can be created post which web-ui can be accessed with the same credentials as above.

----
## Updating helm charts for order-mgmt-app-k8s services

Untar helm charts from srcharts repo which is added previously (before deployment of services)

    helm pull srcharts/<chart-name> --untar

Update image and tag in values.yaml and before installing perform a dry run to inspect kubernetes descriptor yaml files for correctness

    helm install --dry-run --debug <chart-name> -n <namespace> <chart-dir>
    * chart-dir is the directory which is untarred previously and has Chart.yaml
    * e.g. helm install --dry-run --debug inventory-service -n orderapp inventory-service/
    
After verifying the yaml content, the chart can be installed by running 

    helm install inventory-service -n orderapp inventory-service/
    
Refer to "Verify Status" section for further instructions.

----
## Uninstalling helm charts for order-mgmt-app-k8s 

Uninstall helm charts for the services in the reverse order of installation.

     helm uninstall -n orderapp <chart-name>


----
## changelog
* 28-Apr-2020 Initial commit

----
## Thanks
* [amqp-client-cli](https://github.com/ownaginatious/amqp-client-cli)
* [tscharts](https://github.com/technosophos/tscharts)
* [bitnami - Create a Helm chart](https://youtu.be/TJ9hPLn0oAs)
* [minikube - loadbalancer](https://blog.codonomics.com/2019/02/loadbalancer-support-with-minikube-for-k8s.html)
* [markdown - live preview](https://markdownlivepreview.com/)
