# Change Impact Microservice Evolution Tool

This project tracks microservice system evolution changes across repositories.


## Prerequisites

* Maven 3.6+
* Java 11+ (11 Recommended)  

## To Compile:
    ``mvn clean install -DskipTests``

## Extracting an Intermediate Representation:
- Run or compile the main method of ``IntermediateExtraction.java`` in the IDE of your choice or via the command line.
- Default input configuration is defined in `./config.json`
- Optional parameters: ``/path/to/config/file``

Sample input config file:

```json
{
  "clonePath": "./repos",
  "outputPath": "./out",
  "repositories": [
    {
      "repoUrl": "https://github.com/cloudhubs/train-ticket-microservices.git",
      "baseCommit": "f34c476",
      "paths": [ "path/to/microservice", "ts-admins-service", ... ]
    }
  ]
}
```

Sample output produced:
```json
{
    "systemName": "this-system",
    "version": "0.0.1",
    "services": [
        {
            "id": "C:/Users/…/ts-preserve-service",
            "msName": "ts-preserve-service",
            "msPath": "train-ticket-microservices/ts-preserve-service",
            "commitId": "f34c476",
            "controllers": [
                {
                    "className": "OrderController",
                    "classPath": "C:/Users/…/OrderController.java",
                    "variables": [
                        {
                            "variableName": "orderService",
                            "variableType": "OrderService"
                        },
                        {
                            "variableName": "LOGGER",
                            "variableType": "Logger"
                        }
                    ],
                    "restEndpoints": [
                        {
                            "id": "GET:ts-preserve-service.home#0",
                            "api": "/api/v1/orderservice/welcome",
                            "type": "GetMapping",
                            ...                            
                        },
		     ...
                     ],
                 },
                 ...
             ],
            "services": [
             {
                    "className": "OrderOtherServiceImpl",
                    "classPath": "C:/Users/…/OrderOtherServiceImpl.java",
                    "methods": [
                        {
                            "methodName": "getSoldTickets",
                            "parameter": "[Seat seatRequest, HttpHeaders headers]",
                            "returnType": "Response"
                        },
                        ...
                    ],
                    "restCalls": [...]
            },
            ...
          ],
          "repositories": [ ... ],
          "dtos": [ ... ],
          "entities": [ ... ]
      },
      ...
}
```

## Extracting a Delta Change Impact:
- Run or compile the main method of ``DeltaExtraction.java`` in the IDE of your choice or via the command line.
- Command line args list containing ``/path/to/repo(s)``

Sample output produced:
```json
{
        "localPath": "./repos/train-ticket-microservices/.../ContactsController.java",
        "changeType": "MODIFY",
        "commitId": "901fffa66a5dd85b30862b97c3f5013388a265f1",
        "changes": {
            "controllers": [
                {
                    "className": "ContactsController",
                    "classPath": "./repos/train-ticket-microservices/.../ContactsController.java",
                    "variables": [
                        {
                            "variableName": "contactsService",
                            "variableType": "ContactsService"
                        },
                        {
                            "variableName": "LOGGER",
                            "variableType": "Logger"
                        }
                    ],
                    "restEndpoints": [...]
		     },
	     ]
	     "services": [...],
	     ...
	}
}
```

## Merging an IR & Delta Change:
- Run or compile the main method of ``IRMergeRunner.java`` in the IDE of your choice or via the command line.
- Provide command line args containing ``<intermediate-system-file>.json`` and ``<delta-change-file>.json``

Sample output produced:
```json
{
    "systemName": "this-system",
    "version": "0.0.2", // incremented version number
    "services": [
        {
            "id": "ts-preserve-service",
            "msName": "ts-preserve-service",
            "commitId": "b17b69ef75919704d6329f82530ca0e5313061a9", // mapped changes, updated commit Id
            "controllers": [...],
            "services": [...],
            ...
        }
    ]
}
```
