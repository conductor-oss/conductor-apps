# Workflow Rate Limits with conductor
Example application and workflow that shows how to rate limit workflow executions with Orkes Conductor

# Credentials
* Obtain credentials for this application from an Orkes cluster
* If you do not have your own cluster, use https://developer.orkescloud.com
* Navigate to Access Control, Applications, create an application and generate key/secret

# Configuration
Modify `application.properties` with the credentials obtained from the Orkes cluster

# Running the application
* Run `./mvnw spring-boot:run`
* Open Swagger UI at http://localhost:8080/swagger-ui/index.html in your browser

## Static Rate Limits
* Expand the `/api/rate-limit/static` endpoint and click `Try it out`, then `Execute`
* This deploys a sample workflow and sets the rate limit at `2` with a fixed key
* The workflow is executed 3 times
* The first two instances will each have 1 task in running state while the third one will have 0 running tasks
* The first instance will be marked as complete, which allows the third to enter the running state
* Observe that the third instance now has 1 running task

## Dynamic Rate Limits
* Expand the `/api/rate-limit/dynamic` endpoint and click `Try it out`, then `Execute`
* This deploys a sample workflow and sets the rate limit at `2` with a dynamic key using correlation id
* The workflow is executed 4 times, 2 with `correlationId=correlationId1` and 2 with `correlationId=correlationId2`
* The first two instances of each `correlationId` will each have 1 task in running state while the third one will have 0 running tasks
* The first instance of each `correlationId` will be marked as complete, which allows the third to enter the running state
* Observe that the third instance of each `correlationId` now has 1 running task
