from conductor.client.automator.task_handler import TaskHandler
from conductor.client.configuration.configuration import Configuration
from conductor.client.configuration.settings.authentication_settings import AuthenticationSettings
from worker import *


def main():
    configuration = Configuration(base_url='https://developer.orkescloud.com',
                                  authentication_settings=AuthenticationSettings(key_id='_CHANGE_ME_',
                                                                                 key_secret='_CHANGE_ME_'))

    task_handler = TaskHandler(
        configuration=configuration,
        scan_for_annotated_workers=True
    )
    task_handler.start_processes()


if __name__ == '__main__':
    main()