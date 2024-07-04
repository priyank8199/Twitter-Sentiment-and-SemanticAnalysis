import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class NewsExtract {
	public static void main(String args[]) throws IOException, URISyntaxException {
		// MongoDB Configurations
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://root:root@cluster0.jvs2l.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase database = mongoClient.getDatabase("ReuterDb");
		MongoCollection<org.bson.Document> collection = database.getCollection("News");

		//Access File from resources folder
		URL url = NewsExtract.class.getClassLoader().getResource("reut2-009.sgm");
		newsInsert(url, collection);
		url = NewsExtract.class.getClassLoader().getResource("reut2-014.sgm");
		newsInsert(url, collection);

	}

	//Function to save news articles stored in SGM files into MongoDB Collection
	public static void newsInsert(URL url, MongoCollection<org.bson.Document> collection)
			throws IOException, URISyntaxException {
		File f = new File(url.toURI());
		Reader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		StringBuilder file_content = new StringBuilder();
		String line = br.readLine();
		do {
			file_content.append(line).append("\n");
			line = br.readLine();
		} while (line != null);
		List<String> reuter_tags = Arrays.asList(file_content.toString().split("</REUTERS>"));
		for (String reuter_tag : reuter_tags) {
			String title = reuter_tag.contains("<TITLE>") ? reuter_tag.split("<TITLE>")[1].split("</TITLE>")[0] : null;
			String body = reuter_tag.contains("<BODY>") ? reuter_tag.split("<BODY>")[1].split("</BODY>")[0] : null;
			Document document = new Document();
			document.append("title", title);
			document.append("body", body);
			collection.insertOne(document);
		}
	}
}
