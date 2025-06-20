package com.itt.newsaggregator;

import com.itt.newsaggregator.service.NewsAPIClientImpl;
import com.itt.newsaggregator.service.TheNewsAPIClientImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NewsaggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsaggregatorApplication.class, args);
//		ApplicationContext context = SpringApplication.run(NewsaggregatorApplication.class, args);
//		NewsAPIClientImpl fetcher = context.getBean(NewsAPIClientImpl.class);
//		fetcher.fetchNews();
	}

}
