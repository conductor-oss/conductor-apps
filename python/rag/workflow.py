import uuid

from conductor.client.configuration.configuration import Configuration
from conductor.client.http.models import StartWorkflowRequest
from conductor.client.orkes_clients import OrkesClients
from conductor.client.workflow_client import WorkflowClient


def main():

    api_config = Configuration()
    clients = OrkesClients(configuration=api_config)
    workflow_client = clients.get_workflow_client()

    request = StartWorkflowRequest()
    request.name = 'site-search'
    request.version = 1
    request.input = {
        "query": "How can I terminate a workflow using go? give me tabular format in formatted plain text that I can "
                 "print in console"
    }
    response = workflow_client.execute_workflow(start_workflow_request=request, request_id=str(uuid.uuid4()))
    print(response.output["result"])

if __name__ == '__main__':
    main()
