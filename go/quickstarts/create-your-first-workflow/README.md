# Go - Workflow As Code

This project contains an example that shows how to create a workflow using Go.

Take a look at the quickstart: [https://orkes.io/content/quickstarts/create-first-workflow](https://orkes.io/content/quickstarts/create-first-workflow).

### To run this project

1) Create an account in https://developer.orkescloud.com.
2) Create an application.
3) Create an access key for the application.
4) Use that access key in `workflow_as_code.go`.

```go
const KEY_ID = "_CHANGE_ME_"
const SECRET = "_CHANGE_ME_"
```

```shell
go run workflow_as_code.go
```
