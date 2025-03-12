import { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { ChatFeed, Message } from 'react-chat-ui';
import { marked } from 'marked';

const API_BASE_URL = 'http://localhost:5000';

export default function Home() {
  const [messages, setMessages] = useState([]);
  const [userInput, setUserInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [isInterviewDone, setIsInterviewDone] = useState(false);
  const [isInitialStepDone, setIsInitialStepDone] = useState(false);
  const [isInterviewTerminating, setIsInterviewTerminating] = useState(false);

  const chatBoxRef = useRef(null);
  const textareaRef = useRef(null);
  const maxHeight = 150;

  useEffect(() => {
    startWorkflow();
  }, []);

  const startWorkflow = async () => {
    try {
      await axios.post(`${API_BASE_URL}/start_workflow`);
      const response = await axios.get(`${API_BASE_URL}/get_welcome_message`);
      addMessage(response.data.message, 'Bot');
    } catch (error) {
      handleError("The interview has terminated unexpectedly. There was an error starting the workflow.");
    }
  };

  const addMessage = (text, sender) => {
    if (!text) return;
    const messageHtml = marked(text.replace(/\\n/g, '  \n')); // Ensure line breaks are handled
    setMessages((prevMessages) => [
      ...prevMessages,
      new Message({ id: Date.now(), message: messageHtml, senderName: sender })
    ]);
  };

  const handleInputChange = (e) => {
    setUserInput(e.target.value);

    if (textareaRef.current) {
      // Temporarily reset the height to 'auto' to calculate the scrollHeight
      textareaRef.current.style.height = 'auto';

      // Expand height only when content exceeds initial height
      if (textareaRef.current.scrollHeight > textareaRef.current.clientHeight) {
        const scrollHeight = textareaRef.current.scrollHeight;
        // Set height to the scrollHeight, but not exceeding maxHeight
        textareaRef.current.style.height = `${Math.min(scrollHeight, maxHeight)}px`;
      }
    }
  };

  const handleSubmit = async () => {
    if (!userInput.trim()) return;
    addMessage(userInput, 'User');
    setUserInput('');
    setLoading(true);
    if (textareaRef.current) textareaRef.current.style.height = '40px';

    try {
      if (isInitialStepDone) {
        await handleCoreInterviewLoop();
        await checkInterviewStatus();
      } else {
        await processInitialStep();
      }
    } catch (error) {
      handleError("The interview has terminated unexpectedly. There was an error fetching the bot's response.");
    } finally {
      setLoading(false);
    }
  };

  const handleCoreInterviewLoop = async () => {
    const response = await axios.post(`${API_BASE_URL}/send_user_input`, { userInput });
    addMessage(response.data.message || "Sorry, I didn't understand that.", 'Bot');
  };

  const processInitialStep = async () => {
    try {
      const response = await axios.post(`${API_BASE_URL}/send_name_language`, { userInput });
      const initialLoopStatus = await axios.get(`${API_BASE_URL}/get_is_initial_step_done`);
      setIsInitialStepDone(initialLoopStatus.data.message);

      if (initialLoopStatus.data.message) {
        await getInterviewQuestion();
      } else {
        addMessage(response.data.message || "Sorry, I didn't understand that.", 'Bot');
      }
    } catch (error) {
      handleError("The interview has terminated unexpectedly. There was an error processing the initial step.");
    }
  };

  const getInterviewQuestion = async () => {
    const startQuestionMsg = await axios.get(`${API_BASE_URL}/get_question`);
    const botMessages = startQuestionMsg.data.message.map(msg => {
      const messageHtml = marked(msg);
      return {
        id: Date.now(),
        message: messageHtml,
        senderName: 'Bot'
      };
    });
    setMessages(prevMessages => [...prevMessages, ...botMessages]);
  };

  const checkInterviewStatus = async () => {
    const interviewStatus = await axios.get(`${API_BASE_URL}/get_interview_status`);
    if (interviewStatus.data.message === 'DONE') {
      setIsInterviewTerminating(true);
      const isFinalStepDone = await axios.get(`${API_BASE_URL}/get_is_final_step_done`);
      if (isFinalStepDone.data.message) {
        await axios.post(`${API_BASE_URL}/stop_workers`);
        setIsInterviewDone(true);
      }
    }
  };

  const handleError = async (errorMessage) => {
    await axios.post(`${API_BASE_URL}/stop_workflow`);
    addMessage(errorMessage, 'Bot');
    setIsInterviewDone(true);
  };

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [!loading]);

  return (
    <div className="chat-container">
      <h1>Interview Chat</h1>
      <div ref={chatBoxRef} className="chat-box">
        {messages.map((msg, index) => (
          <div key={index} className={`chat-bubble ${msg.senderName === 'User' ? 'user-message' : 'bot-message'}`}>
            <div dangerouslySetInnerHTML={{ __html: msg.message }} />
          </div>
        ))}
        {loading && !isInterviewTerminating && <div className="chat-bubble bot-message typing-indicator">...</div>}
      </div>
      <div className="input-box">
        <textarea
          ref={textareaRef}
          value={userInput}
          onChange={handleInputChange}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault(); // Prevent default enter behavior
              handleSubmit(); // Submit the message
            } else if (e.key === 'Enter' && e.shiftKey) {
              e.preventDefault();
              setUserInput((prev) => {
                const updatedInput = prev + '\n';
                setTimeout(() => handleInputChange({ target: { value: updatedInput } }), 0);
                return updatedInput;
              });
            }
          }}
          placeholder="Type your response..."
          disabled={loading || isInterviewDone}
        />
        <button onClick={handleSubmit} disabled={loading || isInterviewDone}>Send</button>
      </div>
    </div>
  );
}