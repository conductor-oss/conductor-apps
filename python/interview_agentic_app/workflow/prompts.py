from conductor.client.ai.configuration import LLMProvider
from conductor.client.ai.integrations import OpenAIConfig
from conductor.client.ai.orchestrator import AIOrchestrator
from conductor.client.configuration.configuration import Configuration
from conductor.client.orkes_clients import OrkesClients

from workflow.prompts import stock_agent_instructions, stock_agent_decider

interview_question_generator = """
Given an interviewee's name ${name} and a programming language / framework ${language}, generate a technical software engineering question relevant to that language. The question should assess problem-solving skills, algorithms, data structures, or language-specific best practices. Ensure the question is clear, unambiguous, and suited for a coding interview. Your response:
"""

interview_response_evaluator = """
Evaluate the interviewee's ${name} response to the given technical question in ${language}.

QUESTION: ${question}
RESPONSE: ${response}
FOLLOW-UP: ${follow_up}

1. SIMPLIFY: Output this if the response is incorrect or too complex for the interviewee to grasp. This means the question might need to be reworded in a simpler form to make it more approachable.
Example Scenario:
- The answer was completely wrong or irrelevant.
- The question was phrased in a complex or confusing way.
2. HINT: Output this if the response is partially correct, but it’s clear that the interviewee needs some additional guidance or nudging to improve their answer. This suggests that they’re on the right track, but could use a little push in the right direction.
Example Scenario:
- The response contains some correct elements but is missing key points or has some mistakes.
- The interviewee may have started solving the problem, but needs a hint to finish it properly.
3. DONE: Output this if the response is fully correct, complete, and demonstrates a clear understanding of the question. No further action or simplification is needed.
Example Scenario:
- The answer is fully correct with no gaps or errors.
- The interviewee shows a clear understanding of the problem and provides a solution that satisfies the question.

Return only "SIMPLIFY", "HINT", or "DONE" as the output. No additional text.
"""

interview_hint_generator = """
Generate a hint based on the interviewee's ${name} response to the technical question in ${language}. Provide a clue to help them refine their answer. If a previous follow-up exists, use it as context for the hint.

ORIGINAL QUESTION: ${question}
RESPONSE: ${response}
PREVIOUS FOLLOW-UP (if applicable): ${follow_up}

HINT Guidelines:  
Use a hint when the response is partially correct but needs improvement.  
Example cases:  
- The response has some correct elements but is missing key points or contains errors.  
- The interviewee has the right idea but needs guidance to complete the solution.
"""

interview_simplification_generator = """
Generate a simplified version of the technical question for the interviewee ${name} in ${language}. Reword the original question or the previous follow-up (if applicable) to make it more approachable.

ORIGINAL QUESTION: ${question}
RESPONSE: ${response}
PREVIOUS FOLLOW-UP (if applicable): ${follow_up}

SIMPLIFY Guidelines:
Simplify the question if the response was incorrect or the original wording was too complex.
Example cases:
- The answer was completely wrong or off-topic.
- The question was too complicated or unclear.
"""

interview_thank_you_email_generator = """
Write a personalized thank-you email to an interviewee ${name} who recently took the time to interview with Orkes.

The email should:
- Express genuine gratitude for their time, insights, and thoughtful responses during the interview.
- Let them know that our hiring team is currently reviewing feedback and that we will get back to them shortly.
- Personalize the content based on prior messages from the interview.
- Maintain a professional yet warm and appreciative tone.

Important Formatting Instructions:
- Generate the email using proper HTML formatting, ensuring all content is enclosed within a <body> tag.
- Use <p> tags to separate each paragraph. Do not add any extra spaces or \n characters between paragraphs in the HTML.
- Do not include a subject line.
- Sign off as 'John Doe' with the title 'Senior Talent Recruiter'.

Example Output Format:
<body>  
  <p>Dear ${name},</p>  
  <p>I hope this message finds you well. I wanted to take a moment to express my genuine gratitude for the time you took to interview with us at Orkes. Your insights and thoughtful responses during our conversation were truly appreciated.</p>  
  <p>Our hiring team is currently reviewing feedback from your interview, and we will get back to you shortly with an update. Thank you once again for your interest in joining our team and for sharing your expertise with us.</p>  
  <p>Best regards,</p>  
  <p>John Doe<br>Senior Talent Recruiter</p>  
</body>

Here are the previous messages from the interview for context (ignore \n characters for output):
${messages}
"""


def configure_integrations(api_config: Configuration):
    models = ['gpt-4o-mini']

    clients = OrkesClients(configuration=api_config)
    prompt_client = clients.get_prompt_client()
    ai_orchestrator = AIOrchestrator(api_configuration=api_config)

    prompt_client.save_prompt('interview_question_generator',
                              description='Generate a technical question for the interviewee.',
                              prompt_template=interview_response_evaluator)

    prompt_client.save_prompt('interview_response_evaluator',
                              description="Evaluate an interviewee's response to a technical question by outputting either SIMPLIFY, HINT, or DONE.",
                              prompt_template=interview_response_evaluator)
    
    prompt_client.save_prompt('interview_hint_generator',
                              description="Generate a hint based on the original question.",
                              prompt_template=interview_hint_generator)
    
    prompt_client.save_prompt('interview_simplification_generator',
                              description="Generate a simplified question based on the original question.",
                              prompt_template=interview_simplification_generator)
    
    prompt_client.save_prompt('interview_thank_you_email_generator',
                              description="Generate a personalized thank you email to the interviewee.",
                              prompt_template=interview_thank_you_email_generator)

    ai_orchestrator.add_ai_integration('openai-orkes-karl', LLMProvider.OPEN_AI,
                                       description="Karl's Orkes' OpenAi Integration",
                                       models=models, config=OpenAIConfig())

    ai_orchestrator.associate_prompt_template('interview_question_generator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_response_evaluator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_hint_generator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_simplification_generator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_thank_you_email_generator', 'openai-orkes-karl', ai_models=models)