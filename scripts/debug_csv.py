with open("../data/foods.csv", "r", encoding="utf-8", errors="replace") as file:

    for i in range(5):
        line = file.readline()
        print(f"\nLINEA {i + 1}")
        print(line)