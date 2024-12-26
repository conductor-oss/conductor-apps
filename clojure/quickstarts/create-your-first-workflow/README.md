# Clojure - Workflow As Code

This project contains an example that shows how to create a workflow using C#.

Take a look at the quickstart: [https://orkes.io/content/quickstarts/create-first-workflow](https://orkes.io/content/quickstarts/create-first-workflow).

### To run this project

1) Create an account in https://developer.orkescloud.com.
2) Create an application.
3) Create an access key for the application.
4) Use that access key in `src/wac/core.clj`.

```clojure
(def options
  {:app-key "_CHANGE_ME_"
   :app-secret "_CHANGE_ME_"
   :url "https://developer.orkescloud.com/api/"})
```

```shell
clj -M -m wac.core
```