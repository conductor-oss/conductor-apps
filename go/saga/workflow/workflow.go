package workflow

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/conductor-sdk/conductor-go/sdk/client"
	"github.com/conductor-sdk/conductor-go/sdk/model"
	"os"
)

// CreateWorkflows Creates the workflows
func CreateWorkflows(metadataClient client.MetadataClient) (*string, error) {

	// Read the JSON file contents
	byteValue, _ := os.ReadFile("workflow/workflow.json")
	var wf model.WorkflowDef
	err := json.Unmarshal(byteValue, &wf)
	if err != nil {
		fmt.Println("Error reading workflow")
		fmt.Println(err)
		return nil, err
	}
	_, err = metadataClient.RegisterWorkflowDef(context.Background(), true, wf)
	if err != nil {
		fmt.Println("Error registering workflow", err)
		return nil, err
	}

	var compensationWf model.WorkflowDef
	byteValue, _ = os.ReadFile("workflow/compensation.json")
	err = json.Unmarshal(byteValue, &compensationWf)
	if err != nil {
		fmt.Println("Error reading compensation workflow definition")
		fmt.Println(err)
		return nil, err
	}
	_, err = metadataClient.RegisterWorkflowDef(context.Background(), true, compensationWf)
	if err != nil {
		fmt.Println("Error registering compensation workflow", err)
		return nil, err
	}

	return &wf.Name, nil
}
