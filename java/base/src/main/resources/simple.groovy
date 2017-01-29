// simple strings use single quotes

String s = 'hello'
def s1 = 'goodbye'
println s + ' ' + s1

// strings that can be interpolated, GStrings, use double quotes
// note variables don't need to be defined at all, or can be untyped with def
name = 'Baldur'
def greeting = 'Hello'
s = "${greeting} ${name}, how are you?"
println s

// we can put code in the interpolation
x = 8
s = "The number ${x} is even? ${x%2==0}"
println s

// if we don't want \ interpreted as an escape sequence use
def r = /(\d)+/
println r
println r.class

// regex pattern creation with ~
def p = ~/gr(.*)/
println p.class
s = 'groovy has some grs'
// pattern matching with the find operator ==~
if (s ==~ p)
    println "found a gr in: ${s}"

// closure = block of code that can be assigned to a reference
// or passed around just like any other variable (called lambda in Java 8)
def cl1 = {
    println "hello world from closure cl1"
}

// def as always is optional
cl2 = {
    println "hello from closure cl2"
}

// call the closures
cl1()
cl2()

// closure with parameters
def add = {
    a,b ->
    c = a + b
    println "${a} + ${b} = ${c}"
}
add(4,5)

// lists
def list = [1,2,3,4,5]
list.each {println it}
println list.class

def set = [1,2,3,4] as Set
println set
println set.class

// append to set or list
set << 6
println set
set << 6
println set // no change since it's a set

// create a map
def mymap = [:]
mymap.dog='baldur'
mymap.person='mymir'
println mymap
println mymap['dog']
println mymap.dog

// non-closure methods
def addj(a,b) {
    a+b // return statement optional
}
x = addj(4,5)
println x

// optional parameters
def divide(a, b=2) {
    a/b
}
x = divide(100) // will divide by 2
println x
x = divide(100,20)
println x