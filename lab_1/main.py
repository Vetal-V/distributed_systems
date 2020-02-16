from datetime import datetime

print("HELLo World!")
name = input("Please, enter your name: ")
print(f"Hello, {name}!")

length = len(name)
fact = 1
for i in range(1, length+1):
    fact = fact * i
print(f"Your name has {length} letters. {length}! = {fact}")

birth_date = input("Please, enter your birth date in format (DD.MM.YYYY): ")
try:
    birth_date = datetime.strptime(birth_date, '%d.%m.%Y')
except:
    print("Error in birth date. Please, try again.")
    exit(1)

now_date = datetime.today()
delta = str(now_date - birth_date)
delta_days = int(delta.split()[0])
delta_years = int(delta_days // 365.2425)

if delta_years < 0:
    print("Error in birth date (were you born in the future?). Please, try again.")
    exit(1)

now_date = now_date.strftime("%d.%m.%Y")
print(f"Today is {now_date}, you are {delta_years} year{'s' if delta_years > 1 else ''} ({delta_days} day{'s' if delta_days > 1 else ''}) old.")