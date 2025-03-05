# Agentic Workflows with Conductor
Simulated Software Engineer Interview agentic workflow with [Conductor](https://github.com/conductor-oss/conductor)

### Install requirements
```shell
pip3 install -r requirements.txt
```

### Get Conductor developer account keys
1. Login to https://developer.orkescloud.com/
2. Navigate to Applications https://developer.orkescloud.com/applicationManagement/applications
3. Create a new application key and secret

### Get Google Authentication credentials
1. Follow the quickstart tutorial https://developers.google.com/drive/api/quickstart/python
2. Set up your environment and a Google Cloud project
3. Configure OAuth consent screen
4. Move the downloaded JSON file `credentials.json` into the workflow directory

```shell

export OPENAI_API_KEY=<<YOUR_OPENAI_KEY>>

export CONDUCTOR_SERVER_URL=https://developer.orkescloud.com/api;
export CONDUCTOR_AUTH_KEY=<<YOUR_CONDUCTOR_AUTH_KEY>>
export CONDUCTOR_AUTH_SECRET=<<YOUR_CONDUCTOR_AUTH_SECRET>>
```

### Run the demo
```shell
export PYTHONPATH=/[PATH_TO_REPO]/conductor-apps/python/interview_agentic_app
cd workflow
python workflow.py
```

## Source code

**Tools (Service worker implementing the tools used by the agent)**
[worker/workers.py](worker/workers.py)

**Main method that triggers the agent**
[workflow/workflow.py](workflow/workflow.py)

