# Library of AWEsome Conductor example applications
Payment processing [SAGA](https://learn.microsoft.com/en-us/azure/architecture/reference-architectures/saga/saga) with Conductor and Go.

```shell
# Setup Conductor server connection
export CONDUCTOR_SERVER_URL=https://play.orkes.io/api
export CONDUCTOR_AUTH_KEY=<your api key>
export CONDUCTOR_AUTH_SECRET=<your api secret>
```

### Run the application
```shell
go mod tidy
go run app/main.go
```

### Sample output
```shell
INFO[0000] Updated poll interval for task: validate_payment_details, to: 100ms 
INFO[0000] Started 5 worker(s) for taskName validate_payment_details, polling in interval of 100 ms 
INFO[0000] Updated poll interval for task: debit_account, to: 100ms 
INFO[0000] Started 5 worker(s) for taskName debit_account, polling in interval of 100 ms 
INFO[0000] Updated poll interval for task: credit_account, to: 100ms 
INFO[0000] Started 5 worker(s) for taskName credit_account, polling in interval of 100 ms 
INFO[0000] Updated poll interval for task: send_notification, to: 100ms 
INFO[0000] Started 5 worker(s) for taskName send_notification, polling in interval of 100 ms 
Debiting 500 from account 
Crediting 500 from account 
2024/10/15 12:16:45 Payment of 500 from account Account1 to Account2 has been COMPLETED (Reference: )
Started payment workflow with id COMPLETED
Workflow Id 027bce18-8b2a-11ef-8c08-1e1fcef2cd2e

 -- executing a workflow that will fail because of business validation -- 


2024/10/15 12:16:46 Payment of 5000 from account Account11 to Account22 has been FAILED (Reference: )
Started payment workflow with id FAILED
Workflow Id 04bf5581-8b2a-11ef-8c08-1e1fcef2cd2e
2024/10/15 12:16:46 Payment of 5000 from account Account11 to Account22 has been FAILED (Reference: )
```

## SAGA Example
This code is designed to register workflows with a Conductor server. Here's a breakdown of what the code does:

It imports necessary packages, including the Conductor Go SDK.

#### The CreateWorkflows function:

* Reads workflow.json and compensation.json files.
* Unmarshals (parses) these JSON files into WorkflowDef structs.
* Registers these workflow definitions with a Conductor server using the MetadataClient.

####  The main function:

* Creates a new API client using environment variables.
* Creates a new metadata client using this API client.
* Calls the CreateWorkflows function to register the workflows.
* To run this successfully, you need to:



Set up the necessary environment variables as described above.

Make sure workflow.json and compensation.json are present in the workflow directory and contain valid workflow definitions.
Run the program: go run workflow/workflow.go


This script is meant to register workflows with a Conductor server. It doesn't execute the workflows; it just defines them in the Conductor system.

This file defines several worker functions for a payment processing system using the Conductor framework. Let's break down the main components and how to run it:

The code defines worker functions for:

Validating payment details
Debiting an account
Crediting an account
Sending notifications


It also includes a function to update worker configurations from a JSON file.
The main function sets up and starts these workers.

## To run this worker:

Ensure you have the Conductor Go SDK installed:
```shell
go get github.com/conductor-sdk/conductor-go
```
Set up the necessary environment variables for the Conductor server connection as explained above.



Make sure worker.json is present in the worker directory and contains valid task definitions.
Run the program:
```shell
go run worker/worker.go
```

This will start the workers, which will then poll the Conductor server for tasks to execute.
Important notes:

The workers will continue running until you interrupt the program (e.g., with Ctrl+C).
The validatePaymentDetails function includes some random behavior for demonstration purposes (it will sometimes return an error).
Make sure your Conductor server is running and accessible.
The worker functions are just placeholders and don't perform actual financial transactions. In a real system, you'd replace these with actual implementations.

## To run both the workflow registration (workflow.go) and the workers (worker.go):

First, run the workflow registration:
```shell
go run workflow/workflow.go
```
Then, in a separate terminal, run the workers:
```shell
go run worker/worker.go
```

This setup allows you to register the workflows and then have workers ready to execute tasks for those workflows.
