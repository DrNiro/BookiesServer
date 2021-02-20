package dts;

import javax.annotation.PostConstruct;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

//	This is called after the constructor finished running.
	@PostConstruct
	public void afterInit() {
		System.err.println("server intialized...");
	}
}
