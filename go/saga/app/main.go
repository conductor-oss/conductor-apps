package main

import (
	"context"
	"fmt"
	"github.com/conductor-sdk/conductor-go/sdk/client"
	"github.com/conductor-sdk/conductor-go/sdk/model"
	"github.com/google/uuid"
	"os"
	"os/signal"
	"payment-processing-saga/worker"
	"payment-processing-saga/workflow"
	"syscall"
)

func main() {
	apiClient := client.NewAPIClientFromEnv()
	metadataClient := client.NewMetadataClient(apiClient)
	workflowClient := client.NewWorkflowClient(apiClient)

	// Register workflows
	name, err := workflow.CreateWorkflows(metadataClient)
	if err != nil {
		fmt.Println(err.Error())
		return
	}

	// Let's start the workers
	worker.RegisterAndStartWorkers(apiClient)

	input := map[string]interface{}{
		"amount":           500,
		"fromAccountId":    "Account1",
		"toAccountId":      "Account2",
		"paymentReference": "RefA1A2",
	}
	request := model.StartWorkflowRequest{
		Name:    *name,
		Version: 1,
		Input:   input,
	}
	requestId := uuid.New().String()
	workflowRun, _, err := workflowClient.ExecuteWorkflow(context.Background(), request, requestId, *name, 1, "")
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	fmt.Println("Started payment workflow with id", workflowRun.Status)
	fmt.Println("Workflow Id", workflowRun.WorkflowId)

	fmt.Println("\n -- executing a workflow that will fail because of business validation -- \n\n")

	// Sending a larger amount, that should trigger workflow to FAIL
	input = map[string]interface{}{
		"amount":           5000,
		"fromAccountId":    "Account11",
		"toAccountId":      "Account22",
		"paymentReference": "RefA11A22",
	}
	request = model.StartWorkflowRequest{
		Name:    *name,
		Version: 1,
		Input:   input,
	}
	requestId = uuid.New().String()
	workflowRun, _, err = workflowClient.ExecuteWorkflow(context.Background(), request, requestId, *name, 1, "")
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	fmt.Println("Started payment workflow with id", workflowRun.Status)
	fmt.Println("Workflow Id", workflowRun.WorkflowId)

	done := make(chan os.Signal, 1)
	signal.Notify(done, syscall.SIGINT, syscall.SIGTERM)
	fmt.Println("Blocking, press ctrl+c to continue...")
	<-done // Will block here until user hits ctrl+c
}
