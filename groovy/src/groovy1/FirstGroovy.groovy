package groovy1

import com.sun.org.apache.xpath.internal.functions.FuncStartsWith

class FirstGroovy {

	static main(args) {
		def mylist = [1,2,"baldur", "finnr", true]
		mylist.each({println it}) 
		// or more simply lose the round brackets and just pass the closure
		mylist.reverseEach {println it}
		
		println sum(55,32)
		println sum(55,66,77)
		
		for (i in 0..9) {
			println ("Hello $i")
		}
		
		// from java.lang.Math
		println Math.PI
		println Math.E
		def a = -1.34
		println Math.abs(a)
		
		def s = "This is a complex GString based sentence."
		def slist = s.split(" ")
		slist.each {println it}
		
		def dogs = ["Baldur": "Male Black Lab", "Mymir": "Female Black Lab", "Butters": "Male Yellow Lab"]
		println dogs["Mymir"]
		
		def nmap = [:] // create a new empty map
		nmap["Baldur"] = "This is the first key-value pair in the nmap, it's assigned to Baldur"
		nmap["Mymir"] = "This is the second entry and Myms has it"
		nmap["Butters"] = "This is Butters, he is a good dog"
		nmap["Finnr"] = "Finnr is not a dog but is small enough to be one"
		println nmap["Baldur"]
		println nmap.Mymir // another way to pass the key to the map
		
		nmap.each {key, value -> print key + ": " println value}
		
		def boydogs = nmap.findAll({key -> key.getKey().toString().startsWith("B")})
		println boydogs
		
		def data = new URL("https://www.sas.com").getText()
		//print data // would print out the sas front page
	}

	static sum(a,b,c=0) { // c is optional and has a default value of 0
		a+b+c // groovy returns the last item in a method
	}
}
