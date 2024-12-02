# Annotated Java Worker

This project contains a simple Annotated Java worker.

To learn more about workers go to: https://orkes.io/content/developer-guides/using-workers.

To run this project: 

1) Create an account in https://developer.orkescloud.com
2) Create an application
3) Create an access key for the application
3) Use that access key in `Main.java`

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