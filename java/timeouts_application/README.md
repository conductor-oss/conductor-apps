# Workflow Timeouts with Orkes Conductor
Example application and workflow that shows various timeout scenarios with Orkes Conductor

# Credentials
* Obtain credentials for this application from an Orkes cluster
* If you do not have your own cluster, use https://developer.orkescloud.com
* Navigate to Access Control, Applications, create an application and generate key/secret

# Running the application
* Run:- mvn exec:java -Dexec.mainClass="org.demo.Main"
* Select from the options displayed to run the time of example timeout 

## Global Timeouts
* Global timeouts refer to the timeouts on the entire application from the time a task gets picked up by a worker.
* When set, the application timesout after the set timeout. There would be mutiple options after a timeout: 
  * TIME_OUT_WF for no action after the timeout.
  * ALERT_ONLY to show an alert of timeout.

## Task Timeouts
* Task level timeouts where we can specify timeouts at a task level
* When set, the application timesout after the set timeout. There would be mutiple options after a timeout:
    * RETRY for a certain number of times before actually timing out.
    * TIME_OUT_WF for no action after the timeout.
    * ALERT_ONLY to show an alert of timeout and continue the workflow.