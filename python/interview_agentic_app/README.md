# Agentic Workflows with Conductor
Simulated Software Engineer Interview agentic workflow with [Conductor](https://github.com/conductor-oss/conductor)

### Install backend requirements
```shell
pip3 install -r requirements.txt
```

### Get Conductor developer account key
1. Login to https://developer.orkescloud.com/
2. Navigate to Applications https://developer.orkescloud.com/applicationManagement/applications
3. Create a new application key and secret
4. Set the following variables:
```shell
export CONDUCTOR_SERVER_URL=https://developer.orkescloud.com/api;
export CONDUCTOR_AUTH_KEY=<<YOUR_CONDUCTOR_AUTH_KEY>>
export CONDUCTOR_AUTH_SECRET=<<YOUR_CONDUCTOR_AUTH_SECRET>>
```

### Get OpenAI API key
1. Login to https://platform.openai.com/api-keys
2. Follow the instructions to generate an OpenAI API Key
3. Set the following variables:
```shell
export OPENAI_API_KEY=<<YOUR_OPENAI_KEY>>
```

### Get Google Authentication credentials
1. Follow the quickstart tutorial https://developers.google.com/drive/api/quickstart/python
2. Set up your environment and a Google Cloud project
3. Configure OAuth consent screen
4. Move the downloaded JSON file `credentials.json` into the workflow directory
5. Upon an initial run of the workflow, the server will prompt you to login with your google credentials to generate `token.json`

### Get SendGrid API key
1. Login to https://sendgrid.com/en-us/solutions/email-api
2. Follow the instructions to generate a Twilio SendGrid API Key
3. Access your Orkes account at https://developer.orkescloud.com/
4. Click on Environment Variables tab
5. Add SendGrid credentials:
    - Key: `sendgrid_api_key`
    - Value: `[INSERT API KEY VALUE]`

### Run the backend server
```shell
export PYTHONPATH=/[PATH_TO_REPO]/conductor-apps/python/interview_agentic_app
cd workflow
python app.py
```

## Source code

**Primary backend server with API endpoints to interact with workflow**
[workflow/app.py](workflow/app.py)

**Main method that triggers the workflow agent**
[workflow/workflow.py](workflow/workflow.py)

**Tools (Service worker implementing the tools used by the agent)**
[worker/workers.py](worker/workers.py)