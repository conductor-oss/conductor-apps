import random
from multiprocessing import Value

from conductor.client.worker.worker_task import worker_task

balance = Value('d', 300.0)


def update_balance(balance, amount):
    with balance.get_lock():  # Synchronize access
        balance.value = amount


# Define a function to read the balance
def read_balance(balance):
    with balance.get_lock():  # Synchronize access
        return balance.value


@worker_task("get_stock_price")
def get_stock_price(ticker: str) -> float:
    return random.randrange(100, 105, 1)


@worker_task("buy_stock")
def buy_stock(ticker: str, quantity: int, price: float) -> str:
    amount = price * quantity
    global balance
    current_balance = read_balance(balance)
    if amount > current_balance:
        return "Insufficient funds to buy the stock"
    current_balance = current_balance - amount
    update_balance(balance=balance, amount=current_balance)
    print(f'bought {ticker} and we now have {current_balance}')
    return "OK"


@worker_task("sell_stock")
def sell_stock(ticker: str, quantity: int, price: float) -> str:
    amount = price * quantity
    global balance
    current_balance = read_balance(balance)
    current_balance = current_balance + amount
    update_balance(balance=balance, amount=current_balance)
    print(f'sold {ticker} and we now have {current_balance}')
    return "OK"


@worker_task("check_account_balance")
def check_account_balance() -> float:
    global balance
    return read_balance(balance=balance)


@worker_task("transfer_money")
def transfer_money(amount: float) -> float:
    global balance
    balance = balance + amount
    return balance
