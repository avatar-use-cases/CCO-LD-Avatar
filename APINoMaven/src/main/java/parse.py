def parse():
    arr = []
    file = open('./test.rq');
    for line in file:
        arr.append(line)
    return arr
print(parse())