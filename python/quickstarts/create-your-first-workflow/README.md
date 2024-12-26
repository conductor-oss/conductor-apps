# Python - Workflow As Code

This project contains an example that shows how to create a workflow using Python.

Take a look at the quickstart: [https://orkes.io/content/quickstarts/create-first-workflow](https://orkes.io/content/quickstarts/create-first-workflow).

### To run this project

1) Create an account in https://developer.orkescloud.com.
2) Create an application.
3) Create an access key for the application.
4) Use that access key in `workflow_as_code.py`.

```python
conf = Configuration(base_url='https://developer.orkescloud.com',
                        authentication_settings=AuthenticationSettings(key_id='_CHANGE_ME_',
                                                                       key_secret='_CHANGE_ME_'))
```

```shell
python3 -m venv venv
source venv/bin/activate
pip3 install -r requirements.txt
python3 workflow_as_code.py
```
