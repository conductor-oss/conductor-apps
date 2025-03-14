import json
import logging
import logging
import os
import time

from conductor.client.automator.task_handler import TaskHandler
from conductor.client.configuration.configuration import Configuration
from conductor.client.http.models import StartWorkflowRequest
from conductor.client.metadata_client import MetadataClient
from conductor.client.orkes_clients import OrkesClients
from worker.workers import *

os.environ['CONDUCTOR_SERVER_URL'] = 'FILL IN HERE'
os.environ['CONDUCTOR_AUTH_KEY'] = 'FILL IN HERE'
os.environ['CONDUCTOR_AUTH_SECRET'] = 'FILL IN HERE'

# Define task_handler as a global variable
task_handler = None

def start_workers(api_config):
    task_handler = TaskHandler(
        workers=[],
        configuration=api_config,
        scan_for_annotated_workers=True,
    )
    task_handler.start_processes()
    return task_handler


def add_agentic_workflow(metadata_client: MetadataClient):
    with open('../resources/workflow.json', 'r') as file:
        data = json.load(file)
    return metadata_client.register_workflow_def(workflow_def=data, overwrite=True)

def stop_workflow():
    print('The interview process is complete.')
    if task_handler:
        task_handler.stop_processes()


def main():
    api_config = Configuration()
    api_config.apply_logging_config(level=logging.INFO)
    clients = OrkesClients(configuration=api_config)
    workflow_client = clients.get_workflow_client()
    metadata_client = clients.get_metadata_client()

    task_handler = start_workers(api_config=api_config)

    #configure_integrations(api_config=api_config)
    add_agentic_workflow(metadata_client=metadata_client)

    request = StartWorkflowRequest(name='InterviewAgenticWorkflow', version=1)

    workflow_id = workflow_client.start_workflow(start_workflow_request=request)
    os.environ['WORKFLOW_ID'] = workflow_id
    workflow = workflow_client.get_workflow(workflow_id=workflow_id, include_tasks=False)
    print(f'track the agent execution here {os.getenv("CONDUCTOR_SERVER_URL")}/../execution/{workflow.workflow_id}')
    
    '''
    while workflow.is_running():
        #print(f'{workflow.variables["interview_status"]}')
        workflow = workflow_client.get_workflow(workflow_id=workflow_id, include_tasks=False)
        time.sleep(5)

    print('The interview process is complete.')
    task_handler.stop_processes()
    '''


if __name__ == '__main__':
    main()
