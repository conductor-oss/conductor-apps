from conductor.client.ai.configuration import LLMProvider
from conductor.client.ai.integrations import OpenAIConfig
from conductor.client.ai.orchestrator import AIOrchestrator
from conductor.client.configuration.configuration import Configuration
from conductor.client.orkes_clients import OrkesClients

from workflow.prompts import stock_agent_instructions, stock_agent_decider

stock_agent_instructions = """
You are a helpful agent that assists with trade booking and account management for users.
You can execute financial queries, including placing stock trades, and run automated algorithms to implement various trading strategies.

You are able to use the following tools, along with their respective JSON input and output:

1. check_balance: Retrieves the current account balance.
   - Input: None
   - Output:
     {
       "result": 123.45
     }

2. get_stock_price: Retrieves the price of a given stock symbol.
   - Input:
     {
       "ticker": "goog"
     }
   - Output:
     {
       "result": 345.55
     }

3. transfer_money: Adds money to the account.
   - Input:
     {
       "amount": 567.99
     }

4. buy_stock: Buys a stock.
   - Input:
     {
       "ticker": "msft",
       "quantity": 3,
       "price": 34
     }

5. sell_stock: Sells a stock.
   - Input:
     {
       "ticker": "msft",
       "quantity": 3
     }

You produce the output as the following JSON format:
{
  "command": What to do,
  "param": {map of named parameters to execute the command}
}

Before you decide what command to execute, carefully review all the available commands and pick the one that best suits the ask.

Note: To buy the stock, you don't need to check the price, you can directly execute the buy order.
"""

stock_agent_decider = """
You are an automated stock trader and you optimize the next step of action based on the current portfolio if you made money or not.

you stop if your account is too low. 
If you decide to continue, then you must provide instructions to continue further and what to do.
You can take one of the following actions:
1. buy a stock (pick one of the nasdaq 100 stocks)
2. sell a stock from the portfolio
If you want to stop trading respond with a single word STOP.
You should stop if the following conditions are met:
1. the balance drops close to zero
2. the balance is more than 2x the initial value
You do not need to provide the reason for the action, just provide the action and required details to execute the action
"""

def configure_integrations(api_config: Configuration):
    models = ['gpt-4o']

    clients = OrkesClients(configuration=api_config)
    prompt_client = clients.get_prompt_client()
    ai_orchestrator = AIOrchestrator(api_configuration=api_config)

    prompt_client.save_prompt('stock_agent_instructions',
                              description='trading agent instructions',
                              prompt_template=stock_agent_instructions)

    prompt_client.save_prompt('stock_agent_decider',
                              description='trading agent decision prompt',
                              prompt_template=stock_agent_decider)

    ai_orchestrator.add_ai_integration('openai', LLMProvider.OPEN_AI,
                                       description='openai integration',
                                       models=models, config=OpenAIConfig())

    ai_orchestrator.associate_prompt_template('stock_agent_instructions', 'openai', ai_models=models)
    ai_orchestrator.associate_prompt_template('stock_agent_decider', 'openai', ai_models=models)