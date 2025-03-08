import { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { ChatFeed, Message } from 'react-chat-ui';

export default function Home() {
  const [messages, setMessages] = useState([]);
  const [userInput, setUserInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [isInitialStepDone, setIsInitialStepDone] = useState(false);

  const chatBoxRef = useRef(null); // Reference to chat-box to scroll it
  const textareaRef = useRef(null); // Reference to textarea to control height

  // Maximum height for the input box
  const maxHeight = 120; // Max height in pixels

  // Call start_workflow on component mount
  useEffect(() => {
    const startWorkflow = async () => {
      try {
        await axios.post('http://localhost:5000/start_workflow');
        const response = await axios.get('http://localhost:5000/get_welcome_message');
        const welcomeMessageText = response.data.message;

        const welcomeMessage = new Message({
          id: Date.now(), // Use a unique ID
          message: welcomeMessageText,
          senderName: 'Bot',
        });

        setMessages((prevMessages) => [...prevMessages, welcomeMessage]);
      } catch (error) {
        console.error('Error starting workflow:', error);
      }
    };

    startWorkflow();
  }, []); // Empty dependency array ensures this runs only once on mount

  // Adjusting the input height based on content
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

  // Handler for submitting user input
  const handleSubmit = async () => {
    if (!userInput.trim()) return;

    // Add user's message to the chat
    const userMessage = new Message({
      id: Date.now(), // Use a unique ID
      message: userInput,
      senderName: 'User',
    });

    setMessages((prevMessages) => [...prevMessages, userMessage]);
    setUserInput(''); // Clear the input field after sending
    setLoading(true); // Show typing animation

    // Reset textarea height after sending
    if (textareaRef.current) {
      textareaRef.current.style.height = '40px'; // Reset to initial height
    }

    try {
      let response;
      // (3) Core interview loop
      if (isInitialStepDone) {
        response = await axios.post('http://localhost:5000/wait_task', { userInput });

        const botMessageText = typeof response.data.message === 'string' 
        ? response.data.message 
        : "Sorry, I didn't understand that.";

        setLoading(false); // Hide typing animation
        
        const botMessage = new Message({
          id: Date.now(), // Unique ID for bot message
          message: botMessageText,
          senderName: 'Bot',
        });

        setMessages((prevMessages) => [...prevMessages, botMessage]);
      // (1) Initial name & lang loop
      } else {
        response = await axios.post('http://localhost:5000/send_name_language', { userInput });
        const initialLoopStatus = await axios.get('http://localhost:5000/is_initial_step_done');
        setIsInitialStepDone(initialLoopStatus.data.message);
        console.log('isInitialStepDone:', initialLoopStatus);
        console.log('CHECK HERE isInitialStepDone:', isInitialStepDone);

        // (2) Get thank you msg & interview question if name & lang are valid
        if (initialLoopStatus.data.message) {
          const start_question_msg = await axios.get('http://localhost:5000/get_question');

          // Send interview question to the chat
          const botMessageText1 = typeof start_question_msg.data.message[0] === 'string' 
            ? start_question_msg.data.message[0]
            : "Sorry, I didn't understand that.";

          const botMessageText2 = typeof start_question_msg.data.message[1] === 'string' 
            ? start_question_msg.data.message[1]
            : "Sorry, I didn't understand that.";

          setLoading(false); // Hide typing animation
          
          const botMessage1 = new Message({
            id: Date.now(), // Unique ID for bot message
            message: botMessageText1,
            senderName: 'Bot',
          });

          const botMessage2 = new Message({
            id: Date.now() + 1, // Unique ID for bot message
            message: botMessageText2,
            senderName: 'Bot',
          });

          setMessages((prevMessages) => [...prevMessages, botMessage1, botMessage2]);
        // Retry the initial loop
        } else {
          const botMessageText = typeof response.data.message === 'string' 
            ? response.data.message 
            : "Sorry, I didn't understand that.";

          setLoading(false); // Hide typing animation
          
          const botMessage = new Message({
            id: Date.now(), // Unique ID for bot message
            message: botMessageText,
            senderName: 'Bot',
          });

          setMessages((prevMessages) => [...prevMessages, botMessage]);
        }
      }

      // const botMessageText = typeof response.data.message === 'string' 
      //   ? response.data.message 
      //   : "Sorry, I didn't understand that.";

      // setLoading(false); // Hide typing animation
      
      // const botMessage = new Message({
      //   id: Date.now(), // Unique ID for bot message
      //   message: botMessageText,
      //   senderName: 'Bot',
      // });

      // setMessages((prevMessages) => [...prevMessages, botMessage]);
    } catch (error) {
      console.error('Error fetching bot response:', error);
      setLoading(false); // Hide typing animation even on error
    }
  };

  // Handle Enter key press
  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      event.preventDefault(); // Prevent default behavior of adding a new line
      handleSubmit();
    }
  };

  // Scroll to bottom whenever messages change
  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]); // Dependency on messages array to trigger scrolling when a new message is added

  return (
    <div className="chat-container">
      <h1>Interview Chat</h1>
      <div ref={chatBoxRef} className="chat-box">
        {messages.map((msg, index) => (
          <div key={index} className={`chat-bubble ${msg.senderName === 'User' ? 'user-message' : 'bot-message'}`}>
            {msg.message}
          </div>
        ))}
        {loading && <div className="chat-bubble bot-message typing-indicator">...</div>}
      </div>
      <div className="input-box">
        <textarea
          ref={textareaRef}
          value={userInput}
          onChange={handleInputChange}
          onKeyDown={handleKeyPress}
          placeholder="Type your response..."
          rows={1}
          style={{
            minHeight: '40px', // Fixed initial height
            height: '40px', // Starting height
            resize: 'none',
            overflow: 'hidden',
          }} // Explicitly set the height back to initial value after sending
        />
        <button onClick={handleSubmit}>Send</button>
      </div>
    </div>
  );
}
