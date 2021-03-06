package com.codingbash;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import com.codingbash.model.PostTweetLimiter;
import com.codingbash.model.TweetDataPayload;

@Configuration
public class MemeConfiguration {

	@Bean
	@Lazy
	@Profile("development")
	public Twitter twitterDevelopment(@Value("${spring.social.twitter.appId}") String consumerKey,
			@Value("${spring.social.twitter.appSecret}") String consumerSecret,
			@Value("${spring.social.twitter.access-token}") String accessToken,
			@Value("${spring.social.twitter.access-token-secret}") String accessTokenSecret) {
		return new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
	}

	@Bean
	@Lazy
	@Autowired
	@Profile("production")
	public Twitter twitterProduction(Environment env) {
		String consumerKey = env.getProperty("spring.social.twitter.appId");
		String consumerSecret = env.getProperty("spring.social.twitter.appSecret");
		String accessToken = env.getProperty("spring.social.twitter.access-token");
		String accessTokenSecret = env.getProperty("spring.social.twitter.access-token-secret");
		return new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
	}

	@Bean
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("MemeAsyncThread-");
		executor.initialize();
		return executor;
	}

	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(4);
		return scheduler;
	}

	@Bean
	@Lazy
	public Queue<TweetDataPayload> postTweetQueue() {
		return new ConcurrentLinkedQueue<TweetDataPayload>();
	}

	@Bean
	public PostTweetLimiter limiter() {
		PostTweetLimiter limiter = new PostTweetLimiter();
		limiter.refresh();
		return limiter;
	}

	@Bean
	@Qualifier("homeTweets")
	public List<Tweet> homeList() {
		return new CopyOnWriteArrayList<Tweet>();
	}

	@Bean
	@Qualifier("memeArchive")
	public List<Tweet> memeArchive() {
		return new CopyOnWriteArrayList<Tweet>();
	}
	
	@Bean
	@Qualifier("lastTweetId")
	public Long lastTweetId(){
		return null;
	}
}
