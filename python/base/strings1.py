# Simple variable assignment

cnt1 = 1
cnt2 = 2
cnt3 = cnt1 + cnt2
cnt4 = cnt3 ** 3
mytext1 = "Well hello there "
mytext2 = 'Single quotes are fine too!'

print(mytext1 + str(cnt4))

print(mytext1)
print(mytext2)
print(cnt1 > cnt2)
print(cnt2 > cnt1)
print(cnt2==cnt2)

# using 'r' to print raw text (don't interpret \ as escape)
print('C:\stupid\windows\drive\name')
print(r'C:\stupid\windows\drive\name')

# large string literal
best = """
It was the best of times
It was the worst of times
blah
blah
"""
print(best)

# strings are 0-based arrays
tsize = len(mytext2)
print(mytext2)
print(tsize)
print(mytext2[0]) # first character
print(mytext2[-1]) # - means count back from end so -1 is last character
print(mytext2[3:tsize]) # position 3 (included) to tsize (excluded) -> called slicing or substring
print(mytext2[3:]) # positon 3 to the end
print(mytext2[:5]) # start to 5(excluded)
