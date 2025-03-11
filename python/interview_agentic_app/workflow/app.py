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

# Orkes server configuration
ORKES_CONDUCTOR_URL = "https://pg-qa.orkesconductor.com"

# Get authentication keys from environment variables
CONDUCTOR_AUTH_KEY = os.getenv("CONDUCTOR_AUTH_KEY")
CONDUCTOR_AUTH_SECRET = os.getenv("CONDUCTOR_AUTH_SECRET")

# Initialize the configuration
config = Configuration()
config.server_api_url = ORKES_CONDUCTOR_URL
config.orkes_api_key = CONDUCTOR_AUTH_KEY
config.orkes_api_secret = CONDUCTOR_AUTH_SECRET

# Initialize OrkesClients
orkes_clients = OrkesClients(configuration=config)
workflow_client = orkes_clients.get_workflow_client()
task_client = orkes_clients.get_task_client()
prev_timestamp = [None]

def poll_for_response(custom_message="Processing request...", extract_fn=None, max_wait_time=10):
    reply_text = custom_message
    updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
    messages_list = updated_workflow.variables.get('messages', [])
    
    for _ in range(max_wait_time):
        messages_list = updated_workflow.variables.get('messages', [])
        curr_timestamp = messages_list[-1].get('timestamp')
        if prev_timestamp[0] != curr_timestamp and messages_list and messages_list[-1].get('role') == 'assistant':
            reply_text = extract_fn(messages_list) if extract_fn else messages_list[-1].get('message')
            prev_timestamp[0] = curr_timestamp
            break
        time.sleep(2)
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
    
    return reply_text

@app.route('/start_workflow', methods=['POST'])
def start_workflow_endpoint():
    start_workflow()
    return jsonify({"message": "Workflow started"}), 200

@app.route('/workflow_status', methods=['GET'])
def workflow_status():
    status = "Running"  # Placeholder
    return jsonify({"message": status})

@app.route('/get_welcome_message', methods=['GET'])
def get_welcome_message():
    try:
        reply_text = poll_for_response("We are working on your request. Please wait for the response...")
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

@app.route('/is_initial_step_done', methods=['GET'])
def is_initial_step_done():
    try:
        updated_workflow = workflow_client.get_workflow(workflow_id=os.environ['WORKFLOW_ID'])
        is_initial_step_done = updated_workflow.variables.get('isInitialStepDone', False)
        return jsonify({"message": is_initial_step_done}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

@app.route('/send_name_language', methods=['POST'])
def send_name_language():
    try:
        data = request.get_json()
        task_client.update_task_by_ref_name(workflow_id=os.environ['WORKFLOW_ID'], task_ref_name="initial_response_ref", status=TaskResultStatus.COMPLETED, output={"response": data.get('userInput')})
        reply_text = poll_for_response("Processing response...")
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

@app.route('/get_question', methods=['GET'])
def get_question():
    try:        
        def extract_question(messages_list):
            return [messages_list[-2].get('message'), messages_list[-1].get('message')]
        
        reply_text = poll_for_response("Fetching the next question...", extract_fn=extract_question)
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

@app.route('/send_user_input', methods=['POST'])
def send_user_input():
    try:
        data = request.get_json()
        task_client.update_task_by_ref_name(workflow_id=os.environ['WORKFLOW_ID'], task_ref_name="interviewee_response_ref", status=TaskResultStatus.COMPLETED, output={"response": data.get('userInput')})
        reply_text = poll_for_response("We are working on your request. Please wait for the response...")
        return jsonify({"message": reply_text}), 200
    except Exception as e:
        return jsonify({"message": "An error occurred", "error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
