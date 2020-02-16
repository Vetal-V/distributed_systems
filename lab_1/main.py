print("HELLo World!")
name = input("Please, enter your name: ")
print(f"Hello, {name}!")

length = len(name)
fact = 1
for i in range(1, length+1):
    fact = fact * i
print(f"Your name has, {length} letters. {length}! = {fact}")