import sys

# Ran1 = sys.argv[1]
# Ran2 = sys.argv[2]

#Ran1 = 3
#Ran2 = 2

data = sys.argv[1]
#data = "2,3"

dataList = data.split(',')
dataFloats = 0
dataFloats = map(int, dataList)
#print(dataList)
#print(dataFloats)

num1 = dataFloats[0]
num2 = dataFloats[1]
#print('num1 received as:' num1)
#print('num2 received as:' num2)
sum = num1 + num2
result = str(sum)

rresult = result

#Result = Ran1 + Ran2
#print(Result)

print(rresult)
