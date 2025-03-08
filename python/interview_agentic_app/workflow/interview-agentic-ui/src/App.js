import React, { useState } from 'react';
import axios from 'axios';

function App() {
    const [status, setStatus] = useState('');

    const startWorkflow = async () => {
        try {
            console.log('Workflow started');
            setStatus('Workflow started');
            await axios.post('http://localhost:5000/start_workflow');
        } catch (error) {
            console.error('Error starting workflow', error);
        }
    };

    const checkStatus = async () => {
        try {
            const response = await axios.get('http://localhost:5000/workflow_status');
            console.log('Status:', response.data.status);
            setStatus(response.data.status);
        } catch (error) {
            console.error('Error fetching status', error);
        }
    };

    return (
        <div className="App">
            <h1>Interview Agentic Workflow</h1>
            <button onClick={startWorkflow}>Start Workflow</button>
            <button onClick={checkStatus}>Check Status</button>
            <p>Status: {status}</p>
        </div>
    );
}

export default App;