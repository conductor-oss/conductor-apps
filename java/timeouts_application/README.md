# Workflow Timeouts with Orkes Conductor
Example application and workflow that shows various timeout scenarios with Orkes Conductor

# Credentials
* Client credentials to be placed inside ClientUtil under package io.orkes.conductor.util
* Obtain credentials for this application from an Orkes cluster
* If you do not have your own cluster, use https://developer.orkescloud.com
* Navigate to Access Control, Applications, create an application and generate key/secret

# Running the application
* Run:- mvn exec:java -Dexec.mainClass="io.orkes.conductor.Main"
* Select from the options displayed to run the time of example timeout 

## Global Timeouts
* Global timeouts refer to the timeouts applied to the entire workflow from the moment a task is picked up by a worker. Once the global timeout is set, the application will timeout after the specified duration. The following options are available after a global timeout occurs:
* When set, the application times out after the set timeout. There would be mutiple options after a timeout: 
  * TIME_OUT_WF: The task is marked as TIMED_OUT and terminated, which also terminates the workflow as TIMED_OUT.
  * ALERT_ONLY: A counter is registered and an alert is sent. No further action is taken.
    * Note: The ALERT_ONLY option should be used only when you have your own metrics monitoring system to send alerts.

## Task Timeouts
* Task-level timeouts allow you to specify a timeout for individual tasks within the workflow. Once the task timeout is reached, the following actions can be triggered:
* When set, the application timesout after the set timeout. There would be mutiple options after a timeout:
    * RETRY: The task will be retried a certain number of times before being considered as timed out.
    * TIME_OUT_WF: The task is marked as TIMED_OUT and terminated, which also terminates the workflow as TIMED_OUT.
    * ALERT_ONLY: A counter is registered and an alert is sent. No further action is taken.