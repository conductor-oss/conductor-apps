import logging
import logging
import time

from conductor.client.automator.task_handler import TaskHandler
from conductor.client.configuration.configuration import Configuration
from conductor.client.http.models import StartWorkflowRequest
from conductor.client.orkes_clients import OrkesClients
from worker.workers import *


def start_workers(api_config):
    task_handler = TaskHandler(
        workers=[],
        configuration=api_config,
        scan_for_annotated_workers=True,
    )
    task_handler.start_processes()
    return task_handler


def main():
    api_config = Configuration()
    api_config.apply_logging_config(level=logging.INFO)
    clients = OrkesClients(configuration=api_config)
    workflow_client = clients.get_workflow_client()
    task_handler = start_workers(api_config=api_config)

    request = StartWorkflowRequest(name='agentic_stock_trader_autonomous', version=1, input={
        'instructions': 'purchase 1 share of google'
    })
    workflow_id = workflow_client.start_workflow(start_workflow_request=request)
    workflow = workflow_client.get_workflow(workflow_id=workflow_id, include_tasks=False)
    while (workflow.is_running()):
        print(f'{workflow.variables["instructions"]}')
        workflow = workflow_client.get_workflow(workflow_id=workflow_id, include_tasks=False)
        time.sleep(5)

    print('Done')
    task_handler.stop_processes()


if __name__ == '__main__':
    main()
