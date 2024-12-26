using Conductor.Client.Authentication;
using Conductor.Client.Models;
using Conductor.Client;
using Conductor.Definition;
using Conductor.Executor;
using Conductor.Definition.TaskType;


// Sign up at https://developer.orkescloud.com and create an application.
// Use your application's Key ID and Key Secret here:
var conf = new Configuration
{
    BasePath = "https://developer.orkescloud.com/api",
    AuthenticationSettings = new OrkesAuthenticationSettings("_CHANGE_ME_", "_CHANGE_ME_")
};

// A WorkflowExecutor instance is used to register and execute workflows.
var executor = new WorkflowExecutor(conf);

// Create the workflow definition.
var worfklow = new ConductorWorkflow()
        .WithName("myFirstWorkflow")
        .WithDescription("Workflow that greets a user. Uses a Switch task, HTTP task, and Simple task.")
        .WithVersion(1)
        .WithTask(new HttpTask("get-user_ref", new HttpTaskSettings { uri = "https://randomuser.me/api/" }))
        .WithTask(new SwitchTask("user-criteria_ref", "${get-user_ref.output.response.body.results[0].location.country}")
            .WithDecisionCase("United States",
                [new SimpleTask("helloWorld", "simple_ref").WithInput("user", "${get-user_ref.output.response.body.results[0].name.first}")]));

// Register the workflow with overwrite = true.
executor.RegisterWorkflow(
    workflow: worfklow,
    overwrite: true
);

// Start the workflow.
var workflowId = executor.StartWorkflow(new StartWorkflowRequest(name: worfklow.Name, version: worfklow.Version));
Console.WriteLine($"Started Workflow: {workflowId}");
