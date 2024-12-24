package main

import (
	"fmt"
	"log"

	"github.com/conductor-sdk/conductor-go/sdk/client"
	"github.com/conductor-sdk/conductor-go/sdk/model"
	"github.com/conductor-sdk/conductor-go/sdk/settings"
	"github.com/conductor-sdk/conductor-go/sdk/workflow"
	"github.com/conductor-sdk/conductor-go/sdk/workflow/executor"
)

// Sign up on https://developer.orkescloud.com and create an application.
// Use your application key id and key secret
const SERVER_URL = "https://developer.orkescloud.com/api"
const KEY_ID = "_CHANGE_ME_"
const SECRET = "_CHANGE_ME_"

var (
	apiClient = client.NewAPIClient(
		settings.NewAuthenticationSettings(KEY_ID, SECRET),
		settings.NewHttpSettings(SERVER_URL))
	workflowExecutor = executor.NewWorkflowExecutor(apiClient)
)

func main() {
	// Create and register the workflow definition
	wf := workflow.NewConductorWorkflow(workflowExecutor).
		Name("myFirstWorkflowGo").
		Version(1).
		Description("Workflow that greets a user. Uses a Switch task, HTTP task, and Simple task.")

	httpTask := workflow.NewHttpTask("get-user_ref", &workflow.HttpInput{Uri: "https://randomuser.me/api/"})
	switchTask := workflow.NewSwitchTask("user-criteria_ref", "${get-user_ref.output.response.body.results[0].location.country}").
		SwitchCase("United States", workflow.NewSimpleTask("helloWorld", "simple_ref").Input("user", "${get-user_ref.output.response.body.results[0].name.first}"))

	wf.Add(httpTask)
	wf.Add(switchTask)
	err := wf.Register(true)
	if err != nil {
		log.Fatalf("Failed to register workflow: %v", err)
	}
	fmt.Printf("Registered workflow: %s\n", wf.GetName())

	// Start the workflow
	id, err := workflowExecutor.StartWorkflow(&model.StartWorkflowRequest{
		Name:    wf.GetName(),
		Version: wf.GetVersion(),
	})

	if err != nil {
		log.Fatalf("Error when starting workflow: %v", err)
	}
	fmt.Printf("Started workflow: %s\n", id)
}
