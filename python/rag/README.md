# Agentic RAG Workflows with Conductor
Agentic RAG with [Conductor](https://github.com/conductor-oss/conductor)

### Install requirements
```shell
pip3 install -r requirements.txt
export CONDUCTOR_SERVER_URL=
python3 workflow/workflow.py 
```

### Get Conductor developer account keys
1. Login to https://developer.orkescloud.com/
2. Navigate to Applications https://developer.orkescloud.com/applicationManagement/applications
3. Create a new application key and secret

```shell
export CONDUCTOR_SERVER_URL=https://developer.orkescloud.com/api;
export CONDUCTOR_AUTH_KEY=<<YOUR_CONDUCTOR_AUTH_KEY>>
export CONDUCTOR_AUTH_SECRET=<<YOUR_CONDUCTOR_AUTH_SECRET>>
```

### Run the demo
```shell
python workflow/workflow.py
```