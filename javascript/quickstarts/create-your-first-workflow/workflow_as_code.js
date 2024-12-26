import {
  orkesConductorClient,
  WorkflowExecutor,
  httpTask,
  simpleTask,
  switchTask,
} from "@io-orkes/conductor-javascript";

// Sign up at https://developer.orkescloud.com and create an application.
// Use your application's Key ID and Key Secret here:
const config = {
  serverUrl: "https://developer.orkescloud.com/api",
  keyId: "_CHANGE_ME_",
  keySecret: "_CHANGE_ME_",
};

const client = await orkesConductorClient(config);
// A WorkflowExecutor instance is used to register and execute workflows.
const executor = new WorkflowExecutor(client);

// Create the workflow definition.
const wf = {
  name: "myFirstWorkflow",
  version: 1,
  description:
    "Workflow that greets a user. Uses a Switch task, HTTP task, and Simple task.",
  tasks: [
    httpTask("get-user_ref", { uri: "https://randomuser.me/api/" }),
    switchTask(
      "user-criteria_ref",
      "${get-user_ref.output.response.body.results[0].location.country}",
      {
        "United States": [
          simpleTask("simple_ref", "helloWorld", {
            user: "${get-user_ref.output.response.body.results[0].name.first}",
          }),
        ],
      }
    ),
  ],
};

// Register the workflow with overwrite = true.
await executor.registerWorkflow(true, wf);

// Start the workflow.
const id = await executor.startWorkflow({name: wf.name, version: wf.version});
console.log(`Started workflow: ${id}`);
client.stop()
