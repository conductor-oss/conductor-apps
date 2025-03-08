import time
from flask import Flask, request, jsonify
from flask_cors import CORS
from workflow import main as start_workflow
import os
import requests
from conductor.client.configuration.configuration import Configuration
from conductor.client.orkes_clients import OrkesClients
from conductor.client.http.models.task_result_status import TaskResultStatus

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Orkes server configuratoin
ORKES_CONDUCTOR_URL = "FILL IN HERE"

# Get authentication keys from environment variables
CONDUCTOR_AUTH_KEY = "FILL IN HERE"
CONDUCTOR_AUTH_SECRET = "FILL IN HERE"
YOUR_JWT = "FILL IN HERE"

# Initialize the configuration
config = Configuration()
config.server_api_url = ORKES_CONDUCTOR_URL
config.orkes_api_key = CONDUCTOR_AUTH_KEY
config.orkes_api_secret = CONDUCTOR_AUTH_SECRET

# Initialize OrkesClients
orkes_clients = OrkesClients(configuration=config)
# Get the WorkflowClient
workflow_client = orkes_clients.get_workflow_client()
# Get the TaskClient
task_client = orkes_clients.get_task_client()
# old timestamp
prev_timestamp = ["timestamp"]

@app.route('/start_workflow', methods=['POST'])
def start_workflow_endpoint():
    # Start the workflow
    start_workflow()
    return jsonify({"message": "Workflow started"}), 200

@app.route('/workflow_status', methods=['GET'])
def workflow_status():
    # Implement logic to get workflow status
    status = "Running"  # Placeholder
    return jsonify({"message": status})

# assistant: welcome statement
# assistant: convert human_task to wait_task: for name & lang
# user: input name & lang
# assistant: generate question

@app.route('/get_welcome_message', methods=['GET'])
def get_welcome_message():
    try:
        print("INSIDE WELCOME MESSAGE")
        reply_text = "We are working on your request. Please wait for the response..."
        max_wait_time_in_sec = 10
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
        messages_list = updated_workflow.variables.get('messages', [])
        curr_timestamp = messages_list[-1].get('timestamp')
        
        for _ in range(max_wait_time_in_sec):
            messages_list = updated_workflow.variables.get('messages', [])
            curr_timestamp = messages_list[-1].get('timestamp')
            if prev_timestamp[0] != curr_timestamp and messages_list and messages_list[-1].get('role') == 'assistant':
                reply_text = messages_list[-1].get('message')
                prev_timestamp[0] = curr_timestamp
                break
            else:
                updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
                time.sleep(2)

        print("INITIAL MSG:", reply_text)
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500
    
@app.route('/is_initial_step_done', methods=['GET'])
def is_initial_step_done():
    try:
        print("INSIDE IS INITIAL STEP DONE")
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
        is_initial_step_done = updated_workflow.variables.get('isInitialStepDone', False)
        return jsonify({"message": is_initial_step_done}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

@app.route('/send_name_language', methods=['POST'])
def send_name_language():
    try:
        print("INSIDE SEND NAME & LANGUAGE")
        data = request.get_json()
        # Send user input to the workflow's WAIT task
        wait_task_id = task_client.update_task_by_ref_name(workflow_id=os.environ['WORKFLOW_ID'], task_ref_name="initial_response_ref", status=TaskResultStatus.COMPLETED, output={"response": data.get('userInput')})
        print("WAIT TASK ID:", wait_task_id)

        reply_text = "You are missing your name or programming language. Please provide both to proceed."
        max_wait_time_in_sec = 10
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
        messages_list = updated_workflow.variables.get('messages', [])
        curr_timestamp = messages_list[-1].get('timestamp')
        
        for i in range(max_wait_time_in_sec):
            print(i, prev_timestamp[0], curr_timestamp)
            messages_list = updated_workflow.variables.get('messages', [])
            curr_timestamp = messages_list[-1].get('timestamp')
            if prev_timestamp[0] != curr_timestamp and messages_list and messages_list[-1].get('role') == 'assistant':
                reply_text = messages_list[-1].get('message')
                prev_timestamp[0] = curr_timestamp
                break
            else:
                updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
                time.sleep(2)

        print("RESPONSE MSG:", reply_text)
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500
    
@app.route('/get_question', methods=['GET'])
def get_question():
    try:
        print("INSIDE GET QUESTION")
        reply_text = "We are working on your request. Please wait for the response..."
        max_wait_time_in_sec = 10
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
        messages_list = updated_workflow.variables.get('messages', [])
        old_timestamp = messages_list[-2].get('timestamp')
        
        for i in range(max_wait_time_in_sec):
            print(i, prev_timestamp[0], old_timestamp)
            messages_list = updated_workflow.variables.get('messages', [])
            old_timestamp = messages_list[-2].get('timestamp')
            if prev_timestamp[0] == old_timestamp and messages_list and messages_list[-1].get('role') == 'assistant':
                reply_text = [messages_list[-2].get('message'), messages_list[-1].get('message')]
                curr_timestamp = messages_list[-1].get('timestamp')
                prev_timestamp[0] = curr_timestamp
                break
            else:
                updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
                time.sleep(2)

        print("GET QUESTION MSG:", reply_text)
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

@app.route('/wait_task', methods=['POST'])
def wait_task():
    # how long to wait for the LLM task to complete? Basically when do I know when it loops back to the start of the DO-WHILE loop?
    # want to poll to check whether the last msg in messages is from 'assistant'
    # use looping code from James: waitForTaskCompletion() -> https://github.com/orkes-io/savant/blob/main/api-server/src/main/java/io/orkes/savant/services/AgentInteractionService.java#L243

    # send_user_response(): update_task_by_ref_name()
    # wait_for_loop_completion(): looping code on messages to determine if last msg is from assistant
    #   get_llm_output(): get_workflow().variables.get('messages', [])[-1]
    #   get_interview_status(): get_workflow().variables.get('interview_status'): SIMPLIFY/HINT/DONE

    try:
        print("INSIDE WAIT TASK")
        data = request.get_json()
        print("#" * 100)

        # Send user input to the workflow's WAIT task
        wait_task_id = task_client.update_task_by_ref_name(workflow_id=os.environ['WORKFLOW_ID'], task_ref_name="interviewee_response_ref", status=TaskResultStatus.COMPLETED, output={"response": data.get('userInput')})
        print("WAIT TASK ID:", wait_task_id)
        
        # Wait for the loop to complete to obtain LLM output
        reply_text = "We are working on your request. Please wait for the response..."
        max_wait_time_in_sec = 10
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
        messages_list = updated_workflow.variables.get('messages', [])
        curr_timestamp = messages_list[-1].get('timestamp')

        # Ensure that the last message is from the assistant & is in response to the user's input
        for i in range(max_wait_time_in_sec):
            messages_list = updated_workflow.variables.get('messages', [])
            curr_timestamp = messages_list[-1].get('timestamp')
            if prev_timestamp[0] != curr_timestamp and messages_list and messages_list[-1].get('role') == 'assistant':
                reply_text = messages_list[-1].get('message')
                prev_timestamp[0] = curr_timestamp
                break
            else:
                updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
                time.sleep(2)

        print("REPLY TEXT:", reply_text)
        return jsonify({"message": reply_text}), 200

        '''
        workflow_info = workflow_client.get_workflow(workflow_id=WORKFLOW_ID) # include_output=True) #, include_tasks=True)
        print("##############################################")
        print("Workflow information:", workflow_info.variables.get('messages', []))
        messages = workflow_info.variables.get('messages', [])
        print("Get LAST MSG:", messages[-1])
        print("##############################################")
        output = task_client.update_task_by_ref_name(workflow_id=WORKFLOW_ID, task_ref_name="interviewee_response_ref", status=TaskResultStatus.COMPLETED, output={"response": "I love Python, it's a great programming language!"})
        print("TASK UPDATE OUTPUT:", output)
        print("##############################################")

        task_poll_data = task_client.get_task_poll_data(task_type="WAIT")
        print("Task poll data:", task_poll_data)

        task = task_client.poll_task(task_type="WAIT")
        print("TASK:", task)

        ###
        
        # Get task ID from the request body
        data = request.get_json()
        task_id = "3f6a4df6-f9f9-11ef-8e08-220acc00a260" #data.get('task_id')  # Extract task_id from the payload
        
        if not task_id:
            return jsonify({"message": "task_id is required"}), 400
        
        # Optional: Add any additional payload you want to send with the request
        payload = {"response": "I love Python, it's a great programming language!"}

        # Define the headers with authentication details
        headers = {
            'X-Authorization': f'{YOUR_JWT}',  # Authorization header using Bearer token
            'Content-Type': 'application/json'
        }

        print("HERE ARE THE POST REQ INFORMATION")
        print(payload)
        print(headers)
        print(f"{ORKES_CONDUCTOR_URL}/execution/{WORKFLOW_ID}?taskId={task_id}&taskReferenceName=interviewee_response_ref")

        # Send the POST request to the Orkes/Conductor wait task endpoint
        response = requests.post(f"{ORKES_CONDUCTOR_URL}/execution/{WORKFLOW_ID}?taskId={task_id}&taskReferenceName=interviewee_response_ref", json=payload, headers=headers)
        
        if response.status_code == 200:
            return jsonify({"message": "Task is waiting", "response": response.json()}), 200
        else:
            return jsonify({"message": "Failed to wait task", "error": response.text}), 500
        '''
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500
    
if __name__ == '__main__':
    app.run(debug=True)