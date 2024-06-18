# Change Impact Microservice Evolution Tool 2

This project tracks microservice system evolution changes across repositories.


## Prerequisites

* Maven 3.6+
* Java 11+ (11 Recommended)  

## To Compile:
    ``mvn clean install -DskipTests``

## Extracting an Intermediate Representation:
- Run or compile the main method of ``IRExtractionRunner.java`` in the IDE of your choice or via the command line.
- Command line args list containing ``/path/to/config/<Config-File>.json``

Sample input config file:

```json
{
  "systemName": "Train-ticket",
  "repositoryURL": "https://github.com/g-goulis/train-ticket-microservices-test.git",
  "endCommit": "06f3e1efe2e2539d05d91b0699cc8d9fe7be29d7",
  "baseBranch": "main"
}
```

Sample output produced:
```json
{
  "name": "Train-ticket",
  "commitID": "1.0",
  "microservices": [
    {
      "name": "ts-rebook-service",
      "path": ".\\clone\\train-ticket-microservices-test\\ts-rebook-service",
      "controllers": [
        {
          "packageName": "com.cloudhubs.trainticket.rebook.controller",
          "name": "WaitListOrderController.java",
          "path": ".\\clone\\train-ticket-microservices-test\\ts-rebook-service\\src\\main\\java\\com\\cloudhubs\\trainticket\\rebook\\controller\\WaitListOrderController.java",
          "classRole": "CONTROLLER",
          "annotations": [
            {
              "name": "RequestMapping",
              "contents": "\"/api/v1/waitorderservice\""
            },
            ...
          ],
          "fields": [
            {
              "name": "waitListOrderService",
              "type": "WaitListOrderService"
            },
            ...
          ],
          "methods": [
            {
              "name": "getAllOrders",
              "annotations": [
                {
                  "name": "GetMapping",
                  "contents": "[path \u003d \"/orders\"]"
                }
              ],
              "parameters": [
                {
                  "name": "HttpHeaders",
                  "type": "headers"
                }
              ],
              "returnType": "HttpEntity",
              "url": "/api/v1/waitorderservice/orders",
              "httpMethod": "GET",
              "microserviceName": "ts-rebook-service"
            },
            ...
          ],
          "methodCalls": [
            {
              "name": "info",
              "objectName": "LOGGER",
              "calledFrom": "getWaitListOrders",
              "parameterContents": "\"[getWaitListOrders][Get All Wait List Orders]\""
            },
            ...
          ]
        },
        ...
      ],
      "Services": [...],
      "Repositories": [...],
      "Entities": [...],
    ],
    "orphans": [...]
}
```

## Extracting a Delta Change Impact:
- Run or compile the main method of ``DeltaExtractionRunner.java`` in the IDE of your choice or via the command line.
- Command line args list containing ``/path/to/config/<Config-File>.json  <oldCommit>  <newCommit>``

Sample output produced:
```json
{
  "oldCommit": "06f3e1efe2e2539d05d91b0699cc8d9fe7be29d7",
  "newCommit": "82949fa07dcf82f66641f5807d629d15bab663a6",
  "changes": [
    {
      "oldPath": ".\\clone\\train-ticket-microservices-test\\ts-price-service\\src\\main\\java\\com\\cloudhubs\\trainticket\\price\\controller\\PriceController.java",
      "newPath": ".\\clone\\train-ticket-microservices-test\\ts-price-service\\src\\main\\java\\com\\cloudhubs\\trainticket\\price\\controller\\PriceController.java",
      "changeType": "MODIFY",
      "classChange": {}
    },
    ...
  ]
}
```

## Merging an IR & System Change:
- Run or compile the main method of ``IRMergeRunner.java`` in the IDE of your choice or via the command line.
- Provide command line args containing ``path/to/IR/<IR-File>.json  path/to/Delta/<IR-File>.json  /path/to/config/<Config-File>.json``
