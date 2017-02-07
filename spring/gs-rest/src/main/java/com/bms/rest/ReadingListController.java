package com.bms.rest;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bms.domain.Book;
import com.bms.domain.BookRepository;

/**
 * @RestController is equivalent to:
 * @Controller - a @Component which is a web controller
 * @ResponseBody - indicates that method return values should be bound to the web response body (can also be added at the method level)
 * 
 * @author razing
 *
 */
@RestController
@RequestMapping("/books")
public class ReadingListController {
	
	private BookRepository bookRepository;
	
	@Autowired
	public ReadingListController(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}
	
	@RequestMapping(value="/{reader}", method=RequestMethod.GET)
	public List<Book> readersBooks(@PathVariable("reader") String reader, Model model) {
		List<Book> bookList = bookRepository.findByReader(reader);
		if(bookList != null) {
			model.addAttribute("books", bookList);
		}
		System.out.println(bookList);
		return bookList;
	}
	
	@RequestMapping(value="/bogus/{reader}", method=RequestMethod.GET) 
	public ResponseEntity<Book> getFirstBook(@PathVariable("reader") String reader) {
		Random rnd = new Random();
		Book book = new Book();
		book.setAuthor("mymir");
		book.setDescription("this is a book");
		book.setId(rnd.nextLong());
		book.setIsbn("asdf");
		book.setReader(reader);
		book.setTitle("This is still a Book");
		System.out.println(book);
		
		return new ResponseEntity<Book>(book, HttpStatus.OK);
	}
	
	/**
	 * @RequestBody binds the request body to the given object, with spring providing the parsing/transformation
	 * @param reader
	 * @param book
	 * @return
	 */
	@RequestMapping(value="/{reader}", method=RequestMethod.POST)
	public Book addToBookList(@PathVariable("reader") String reader, @RequestBody Book book) {
		book.setReader(reader);
		bookRepository.save(book);
		System.out.println(book);
		return book;
	}

}
