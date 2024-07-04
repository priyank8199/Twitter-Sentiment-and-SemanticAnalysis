import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TweetsExtraction {
	public static void main(String[] args) throws TwitterException, IOException {

		// Setup Twitter Developer API
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true);
		cb.setDebugEnabled(true).setOAuthConsumerKey("X0vtGf1CDyr9u3l6xp0edq46a")
				.setOAuthConsumerSecret("UTR4WfykWzByLzev9lHjTUHN6RiMD4oeuDsF3S00WGJOe6SJDr")
				.setOAuthAccessToken("2531855173-xXLAPPPqc7VMgijoITbHy1z5dHJ388Td9UI93Xd")
				.setOAuthAccessTokenSecret("55HfVYr0dKowhuT4zj2tprZ7qP5Dio38BMjGuEY4DTrdO");

		// MongoDB Configurations
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://root:root@cluster0.jvs2l.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase database = mongoClient.getDatabase("RawDb");
		MongoDatabase cleaned_database = mongoClient.getDatabase("ProcessedDb");

		// Twitter instance to extract tweets
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		ArrayList<String> keywords = new ArrayList<String>(
				Arrays.asList("weather", "hockey", "Canada", "Temperature", "Education"));

		// Keywords wise for loop to extract tweets
		for (String keyword : keywords) {
			// Collection of specific keyword
			MongoCollection<org.bson.Document> collection = database.getCollection(keyword);
			MongoCollection<org.bson.Document> cleaned_collection = cleaned_database.getCollection(keyword);

			// Query parameter to extract tweets of specific keyword
			Query query = new Query(keyword);
			query.setCount(100);
			int numberOfTweets = 450;
			long lastID = Long.MAX_VALUE;
			ArrayList<Status> tweets = new ArrayList<Status>();
			System.out.println("Keyword Started : " + keyword);

			while (tweets.size() <= numberOfTweets) {
				QueryResult result = twitter.search(query);
				tweets.addAll(result.getTweets());
				for (Status tweet : result.getTweets()) {
					String json = TwitterObjectFactory.getRawJSON(tweet);
					collection.insertOne(org.bson.Document.parse(json));
					String cleaned_Tweet = json.replaceAll(
							"http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\\\(\\\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))", "");
					cleaned_collection.insertOne(org.bson.Document.parse(cleaned_Tweet));

					if (tweet.getId() < lastID) {
						lastID = tweet.getId();
						query.setMaxId(lastID - 1);
					}
				}
				System.out.println("100 Tweets added");
			}
			System.out.println("Keyword Ended : " + keyword);
		}
	}
}
