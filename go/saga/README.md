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


