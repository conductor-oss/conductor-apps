from conductor.client.ai.configuration import LLMProvider
from conductor.client.ai.integrations import OpenAIConfig
from conductor.client.ai.orchestrator import AIOrchestrator
from conductor.client.configuration.configuration import Configuration
from conductor.client.orkes_clients import OrkesClients
import os

name_question_evaluator = """
Given the following user input, extract the user's name and preferred programming language or frontend framework. The recognized languages and frameworks include:  

Programming Languages:
JavaScript, Python, Java, C, C++, C#, Ruby, PHP, Swift, Kotlin, Go, Rust, TypeScript, Dart, Shell, SQL, R, Perl, Scala, Haskell, Lua.  

Frontend Frameworks:
React, Angular, Vue, Svelte, SolidJS, Next.js, Nuxt.js.  

Then, output a JSON object in the format:  
{"is_initial_step_done": true, "name": "user's name", "language": "preferred programming language or framework"}

Ensure the name's first letters are capitalized.  

If either the name or the programming language/framework is missing or unrecognized, return:  
{"is_initial_step_done": false, "name": null, "language": null}

**Important:** Do not wrap the output in a ```json ... ``` block. The output should be a plain JSON object.

---

Example Cases:  
✅ Valid Input:
Input: "Hi, I'm Alex and I want to conduct this interview in JavaScript."
Output: {"is_initial_step_done": true, "name": "Alex", "language": "JavaScript"}

Input: "Hi, I'm donavan smith and I want to use Java."
Output: {"is_initial_step_done": true, "name": "Donavan Smith", "language": "Java"}

Input: "name=jane, language=c."
Output: {"is_initial_step_done": true, "name": "Jane", "language": "C"}

Input: "Name is Gloria Peng and language React."
Output: {"is_initial_step_done": true, "name": "Gloria Peng", "language": "React"}

Input: "James, Vue"
Output: {"is_initial_step_done": true, "name": "James", "language": "Vue"}

❌ Invalid Input (missing name, missing language, invalid language):
Input: "I enjoy coding."
Output: {"is_initial_step_done": false, "name": null, "language": null}

Input: "My name is george and I enjoy coding."
Output: {"is_initial_step_done": false, "name": "George", "language": null}

Input: "I want to code in C++."
Output: {"is_initial_step_done": false, "name": null, "language": "C++"}

Input: "I'm Bob and I want to code in Chinese."
Output: {"is_initial_step_done": false, "name": "Bob", "language": null}

Input: "I'm Amy and I want to use Snake."
Output: {"is_initial_step_done": false, "name": "Amy", "language": null}

---

Now, process this input: "${response}"
"""

interview_question_generator = """
Given an interviewee's name ${name} and a programming language / framework ${language}, generate a technical software engineering question relevant to that language. The question should assess problem-solving skills, algorithms, data structures, or language-specific best practices. Ensure the question is clear, unambiguous, and suited for a coding interview. Your response:
"""

interview_response_evaluator = """
Evaluate the interviewee's ${name} response to the given technical question in ${language}.

Output must be strictly one of the following: 'SIMPLIFY', 'HINT', or 'DONE'. Do not generate explanations, reasoning, or additional text.

1. SIMPLIFY: Output this if the response is incorrect or too complex for the interviewee to grasp. This means the question might need to be reworded in a simpler form to make it more approachable.
Example Scenario:
- The answer was completely wrong or irrelevant.
- The question was phrased in a complex or confusing way.

2. HINT: Output this if the response is partially correct, but it’s clear that the interviewee needs some additional guidance or nudging to improve their answer. This suggests that they’re on the right track, but could use a little push in the right direction.
Example Scenario:
- The response contains some correct elements but is missing key points or has some mistakes.
- The interviewee may have started solving the problem, but needs a hint to finish it properly.
- The interviewee **only suggests an approach (e.g., "Could I use a dictionary for this?") but does not provide an actual solution.**

3. DONE: Output this if the code response is fully correct, complete, and demonstrates a clear understanding of the question. No further action or simplification is needed.
Example Scenario:
- The answer is fully correct with no gaps or errors.
- The interviewee shows a clear understanding of the problem and provides a solution that satisfies the question.

Strictly return only 'SIMPLIFY', 'HINT', or 'DONE'. Do not generate explanations, reasoning, or additional text.

RESPONSE: ${response}
"""

interview_hint_generator = """
Generate a hint based on the interviewee's ${name} response to the technical question in ${language}. Provide a clue to help them refine their answer without explicitly revealing the solution. If a previous follow-up exists, use it as context for the hint.

ORIGINAL QUESTION: ${question}
RESPONSE: ${response}
PREVIOUS FOLLOW-UP (if applicable): ${follow_up}

HINT Guidelines:  
- Use a hint when the response is partially correct but needs improvement.
- The hint should nudge the interviewee toward the correct approach without directly 
- Example cases: the response has some correct elements but is missing key points or contains errors, the interviewee has the right idea but needs guidance to complete the solution.

Formatting Instructions:
- Explicitly insert `\n` where appropriate to improve readability.  
- Format the hint in **Markdown**.  
- For lists, explicitly include `\n` characters to separate items, e.g.:  
  ```
  \n1...
  \n2...
  ```
- **Do not explicitly state the correct solution**—instead, provide guiding principles, best practices, or alternative considerations. 

Use this as an example output for formatting. You must include explicit `\n` characters:
**Question:**\n\nKarl, imagine you are tasked with implementing a function in Python that takes a list of integers and returns a new list containing only the unique elements from the original list, while maintaining the order of their first appearance. \n\nPlease write a function called `get_unique_elements` that accomplishes this. \n\n**Function Signature:**\n```python\ndef get_unique_elements(nums: List[int]) -> List[int]:\n```\n\n**Example:**\n```python\ninput_list = [4, 5, 4, 6, 7, 5, 8]\noutput_list = get_unique_elements(input_list)\nprint(output_list)  # Output should be [4, 5, 6, 7, 8]\n```\n\n**Constraints:**\n- The input list can contain both positive and negative integers.\n- The input list can be empty.\n- You should aim for a solution that has a time complexity of O(n), where n is the number of elements in the input list.\n\nPlease explain your approach and thought process as you implement the function.

Here is a second example with proper formatting using explicit `\n` characters:
**Hint:**\n\nTo implement the `unique_elements` function, consider using a set to keep track of the elements you've already seen. This will help you efficiently check for uniqueness while preserving the order of their first occurrence. Here’s a structured approach to guide you:\n\n1. **Initialize an empty list** to store the unique elements.\n\n2. **Create an empty set** to track the elements that have already been added to the list.\n\n3. **Iterate through the input list**:\n   - For each element, check if it is in the set.\n   - If it is not in the set, append it to the unique elements list and add it to the set.\n\n4. **Return the unique elements list** at the end.\n\n**Time Complexity:**\nAim for O(n) where n is the number of elements in the input list, since you will be iterating through the list once.\n\n**Space Complexity:**\nThe space complexity will be O(u), where u is the number of unique elements, due to the storage in the set and the output list.\n\nThis approach will help you efficiently handle larger lists as well. Good luck!

***You must be adding explicit `\n` characters into the text output to provide appropriate markdown formatting.
"""

interview_simplification_generator = """
Generate a simplified version of the technical question for the interviewee ${name} in ${language}. Reword the original question or the previous follow-up (if applicable) to make it more approachable while maintaining the core challenge.

ORIGINAL QUESTION: ${question}
RESPONSE: ${response}
PREVIOUS FOLLOW-UP (if applicable): ${follow_up}

SIMPLIFY Guidelines:
- Simplify the question if the response was incorrect or the original wording was too complex.
- Do not change the intent of the question—just make it easier to understand.
- Example cases: the answer was completely wrong or off-topic, the question was too complicated or unclear.

Formatting Instructions:
- Explicitly insert `\n` where appropriate to improve readability.  
- Format the simplified question in **Markdown**.  
- Use line breaks (`\n`) to improve clarity and separate key parts of the question.  
- For lists, explicitly add `\n` characters to separate items, e.g.:  
  ```
  \n1...
  \n2...
  ```
- **Do not include the full solution**—only simplify the question for better understanding.  

Use this as an example output for formatting. You must include explicit `\n` characters:
**Question:**\n\nKarl, imagine you are tasked with implementing a function in Python that takes a list of integers and returns a new list containing only the unique elements from the original list, while maintaining the order of their first appearance. \n\nPlease write a function called `get_unique_elements` that accomplishes this. \n\n**Function Signature:**\n```python\ndef get_unique_elements(nums: List[int]) -> List[int]:\n```\n\n**Example:**\n```python\ninput_list = [4, 5, 4, 6, 7, 5, 8]\noutput_list = get_unique_elements(input_list)\nprint(output_list)  # Output should be [4, 5, 6, 7, 8]\n```\n\n**Constraints:**\n- The input list can contain both positive and negative integers.\n- The input list can be empty.\n- You should aim for a solution that has a time complexity of O(n), where n is the number of elements in the input list.\n\nPlease explain your approach and thought process as you implement the function.

Here is a second example with proper formatting using explicit `\n` characters:
**Hint:**\n\nTo implement the `unique_elements` function, consider using a set to keep track of the elements you've already seen. This will help you efficiently check for uniqueness while preserving the order of their first occurrence. Here’s a structured approach to guide you:\n\n1. **Initialize an empty list** to store the unique elements.\n\n2. **Create an empty set** to track the elements that have already been added to the list.\n\n3. **Iterate through the input list**:\n   - For each element, check if it is in the set.\n   - If it is not in the set, append it to the unique elements list and add it to the set.\n\n4. **Return the unique elements list** at the end.\n\n**Time Complexity:**\nAim for O(n) where n is the number of elements in the input list, since you will be iterating through the list once.\n\n**Space Complexity:**\nThe space complexity will be O(u), where u is the number of unique elements, due to the storage in the set and the output list.\n\nThis approach will help you efficiently handle larger lists as well. Good luck!

***You must be adding explicit `\n` characters into the text output to provide appropriate markdown formatting.
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

    prompt_client.save_prompt('name_question_evaluator',
                              description="Evaluate an interviewee's response to providing their own name & programming language. Output three values: is_initial_step_done, name, language in JSON format.",
                              prompt_template=name_question_evaluator)

    prompt_client.save_prompt('interview_question_generator',
                              description='Generate a technical question for the interviewee.',
                              prompt_template=interview_question_generator)

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

    openai_api_key = os.getenv('OPENAI_API_KEY')
    openai_config = OpenAIConfig(api_key=openai_api_key)
    ai_orchestrator.add_ai_integration('openai-orkes-karl', LLMProvider.OPEN_AI,
                                       description="Karl's Orkes' OpenAi Integration",
                                       models=models, config=openai_config)

    ai_orchestrator.associate_prompt_template('name_question_evaluator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_question_generator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_response_evaluator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_hint_generator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_simplification_generator', 'openai-orkes-karl', ai_models=models)
    ai_orchestrator.associate_prompt_template('interview_thank_you_email_generator', 'openai-orkes-karl', ai_models=models)