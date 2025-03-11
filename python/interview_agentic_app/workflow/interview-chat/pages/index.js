import { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { ChatFeed, Message } from 'react-chat-ui';
import { marked } from 'marked';

const API_BASE_URL = 'http://localhost:5000';

export default function Home() {
  const [messages, setMessages] = useState([]);
  const [userInput, setUserInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [isInitialStepDone, setIsInitialStepDone] = useState(false);

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
      console.error('Error starting workflow:', error);
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
        // (1) Core interview loop
        if (isInitialStepDone) {
            const response = await axios.post(`${API_BASE_URL}/send_user_input`, { userInput });
            addMessage(response.data.message || "Sorry, I didn't understand that.", 'Bot');
        // (2) Initial step (name & language)
        } else {
            await processInitialStep();
        }
    } catch (error) {
        console.error('Error fetching bot response:', error);
    } finally {
        setLoading(false);
    }
  };

  const processInitialStep = async () => {
    try {
        const response = await axios.post(`${API_BASE_URL}/send_name_language`, { userInput });
        const initialLoopStatus = await axios.get(`${API_BASE_URL}/is_initial_step_done`);
        setIsInitialStepDone(initialLoopStatus.data.message);

        if (initialLoopStatus.data.message) {
            // Successfully completed name & language step, get interview question
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
        } else {
            // Still in initial loop, respond with validation message
            addMessage(response.data.message || "Sorry, I didn't understand that.", 'Bot');
        }
    } catch (error) {
        console.error('Error processing initial step:', error);
    }
  };

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  return (
  <div className="chat-container">
    <h1>Interview Chat</h1>
    <div ref={chatBoxRef} className="chat-box">
      {messages.map((msg, index) => (
        <div key={index} className={`chat-bubble ${msg.senderName === 'User' ? 'user-message' : 'bot-message'}`}>
          <div dangerouslySetInnerHTML={{ __html: msg.message }} />
        </div>
      ))}
      {loading && <div className="chat-bubble bot-message typing-indicator">...</div>}
    </div>
    <div className="input-box">
      <textarea
        ref={textareaRef}
        value={userInput}
        onChange={handleInputChange}
        onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleSubmit())}
        placeholder="Type your response..."
      />
      <button onClick={handleSubmit}>Send</button>
    </div>
  </div>
);

}
