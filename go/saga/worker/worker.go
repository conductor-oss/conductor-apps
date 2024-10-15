package main

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/conductor-sdk/conductor-go/sdk/client"
	"github.com/conductor-sdk/conductor-go/sdk/model"
	"github.com/conductor-sdk/conductor-go/sdk/worker"
	"log"
	"math/rand"
	"os"
	"os/signal"
	"syscall"
	"time"
)

const (
	batchSize    = 5
	pollInterval = 100 * time.Millisecond
)

// PaymentTransferRequest struct in Go
type PaymentTransferRequest struct {
	FromAccountId    string
	ToAccountId      string
	Amount           float64
	PaymentReference string
}

// Worker function to validate payment details
func validatePaymentDetails(task *model.Task) (interface{}, error) {

	request, err := getTransferRequest(task)
	if err != nil {
		fmt.Println(err.Error())
		return nil, errors.New("error getting request")
	}

	// Validation logic
	if request.Amount > 1000 {
		return map[string]interface{}{
			"reason": fmt.Sprintf("Amount exceeded the limit ($1000). Amount: %v", request.Amount),
			"status": false,
		}, nil
	}

	rand.Seed(time.Now().UnixNano())
	random := rand.Intn(10)

	if random < 5 {
		return nil, errors.New("can't call the API to validate payment details")
	}

	// All OK
	return map[string]interface{}{
		"status": true,
	}, nil
}

// Worker function to debit an account
func debitAccount(task *model.Task) (interface{}, error) {
	request, err := getTransferRequest(task)
	if err != nil {
		fmt.Println(err.Error())
		return nil, errors.New("error getting request")
	}

	// Implement logic to debit money from the account here
	// Example: debitAccountLogic(accountId, amount)

	fmt.Printf("Debiting %v from account %s\n", request.Amount, request.FromAccountId)
	return nil, nil
}

// Worker function to credit an account
func creditAccount(task *model.Task) (interface{}, error) {
	request, err := getTransferRequest(task)
	if err != nil {
		fmt.Println(err.Error())
		return nil, errors.New("error getting request")
	}

	// Implement logic to debit money from the account here
	// Example: debitAccountLogic(accountId, amount)

	fmt.Printf("Crediting %v from account %s\n", request.Amount, request.ToAccountId)
	return nil, nil
}

// Worker function to send a notification
func sendNotification(task *model.Task) (interface{}, error) {

	request, err := getTransferRequest(task)
	if err != nil {
		fmt.Println(err.Error())
		return nil, errors.New("error getting request")
	}
	status, err := GetValueFromTaskInputData(task, "status")

	// Logging the notification
	log.Printf("Payment of %v from account %s to %s has been %s (Reference: %s)\n", request.Amount, request.FromAccountId, request.ToAccountId, status, request.PaymentReference)

	return nil, nil
}

func getTransferRequest(task *model.Task) (*PaymentTransferRequest, error) {

	var paymentTransferRequest PaymentTransferRequest

	fromAccountId, err := GetValueFromTaskInputData(task, "fromAccountId")
	if err != nil {
		return nil, err
	}

	toAccountId, err := GetValueFromTaskInputData(task, "toAccountId")
	if err != nil {
		return nil, err
	}

	amount, err := GetValueFromTaskInputData(task, "amount")
	if err != nil {
		return nil, err
	}
	//amount.SetString(*amountStr)

	paymentReference, err := GetValueFromTaskInputData(task, "paymentReference")
	if err != nil {
		return nil, err
	}

	paymentTransferRequest = PaymentTransferRequest{
		FromAccountId:    fromAccountId.(string),
		ToAccountId:      toAccountId.(string),
		Amount:           amount.(float64),
		PaymentReference: paymentReference.(string),
	}

	return &paymentTransferRequest, nil
}

func GetValueFromTaskInputData(t *model.Task, key string) (interface{}, error) {
	rawValue, ok := t.InputData[key]
	if !ok {
		return "", nil
	}
	return rawValue, nil
}

func updateWorkerConfig(metadataClient client.MetadataClient) {

	// Read the JSON file contents
	byteValue, _ := os.ReadFile("worker/worker.json")
	var taskDef []model.TaskDef
	err := json.Unmarshal(byteValue, &taskDef)
	if err != nil {
		fmt.Println("Error reading worker configuration")
		fmt.Println(err)
		return
	}
	_, err = metadataClient.RegisterTaskDef(context.Background(), taskDef)
	if err != nil {
		fmt.Println("Error registering worker configuration", err)
		return
	}
}

func main() {

	apiClient := client.NewAPIClientFromEnv()
	metadataClient := client.NewMetadataClient(apiClient)
	updateWorkerConfig(metadataClient)

	taskRunner := worker.NewTaskRunnerWithApiClient(
		client.NewAPIClientFromEnv(),
	)
	err := taskRunner.StartWorker("validate_payment_details", validatePaymentDetails, batchSize, pollInterval)
	if err != nil {
		fmt.Errorf("Error starting workers ", err.Error())
	}
	err = taskRunner.StartWorker("debit_account", debitAccount, batchSize, pollInterval)
	if err != nil {
		fmt.Errorf("Error starting workers ", err.Error())
	}
	err = taskRunner.StartWorker("credit_account", creditAccount, batchSize, pollInterval)
	if err != nil {
		fmt.Errorf("Error starting workers ", err.Error())
	}
	err = taskRunner.StartWorker("send_notification", sendNotification, batchSize, pollInterval)
	if err != nil {
		fmt.Errorf("Error starting workers ", err.Error())
	}

	done := make(chan os.Signal, 1)
	signal.Notify(done, syscall.SIGINT, syscall.SIGTERM)
	fmt.Println("Blocking, press ctrl+c to continue...")
	<-done // Will block here until user hits ctrl+c

}
