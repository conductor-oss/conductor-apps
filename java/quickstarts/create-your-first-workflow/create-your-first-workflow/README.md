# Java-SDK - Workflow As Code

This project contains an example that shows how to create a workflow using the Java SDK.

Take a look at the quickstart: [https://orkes.io/content/quickstarts/create-first-workflow](https://orkes.io/content/quickstarts/create-first-workflow).

### To run this project

1) Create an account in https://developer.orkescloud.com.
2) Create an application.
3) Create an access key for the application.
4) Use that access key in `WorkflowAsCode.java`.

```java
var client = ApiClient.builder()
        .basePath("https://developer.orkescloud.com/api")
        .credentials("_CHANGE_ME_", "_CHANGE_ME_")
        .build();
```

```shell
./gradlew build
./gradlew run
```