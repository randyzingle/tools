package com.bms.mvc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bms.domain.Book;
import com.bms.domain.BookRepository;
import com.bms.properties.AmazonProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/readers")
//load all properties (via setter injection) from application.properties that start with amazon.
@ConfigurationProperties(prefix="amazon") 
public class ReadingListController {
	
	private BookRepository bookRepository;
	private AmazonProperties amazonProperties;
	
	private String amazonID;
	
	// Spring boot will create a class that implements our BookRepository interface 
	// and we can inject it from the application context
	@Autowired
	public ReadingListController(BookRepository bookRepository, AmazonProperties amazonProperties) {
		this.bookRepository = bookRepository;
		this.amazonProperties = amazonProperties;
		System.out.println("******************");
		System.out.printf("amazon.amazonID=%s%namazon.url=%s%namazon.password=%s%n",
				amazonProperties.getAmazonID(), amazonProperties.getUrl(), amazonProperties.getPassword());
		System.out.println("******************");
	}
	
	// application properties has an amazon.amazonID property. It will get loaded and injected here
	// at application startup since this is a bean that will be loaded into the application context
	public void setAmazonID(String amazonID) {
		this.amazonID = amazonID;
	}
	
	@RequestMapping(value="/{reader}", method=RequestMethod.GET)
	public String readersBooks(@PathVariable("reader") String reader, Model model) {
		List<Book> readingList = bookRepository.findByReader(reader);
		if (readingList != null) {
			model.addAttribute("books", readingList);
			model.addAttribute("amazonID", amazonID);
		}
		return "readingList";
	}
	
	@RequestMapping(value="/{reader}", method=RequestMethod.POST)
	public String addToReadingList(@PathVariable("reader") String reader, Book book) {
		book.setReader(reader);
		bookRepository.save(book);
		System.out.println(book);
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println(mapper.writeValueAsString(book));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "redirect:/readers/{reader}";
	}

}
