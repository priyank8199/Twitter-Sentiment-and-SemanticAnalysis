package Problem3;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Formatter;
import java.text.DecimalFormat;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class SemanticAnalysis {
	public static void main(String args[]) throws JSONException {
		// MongoDB Configurations
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://root:root@cluster0.jvs2l.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase cleaned_database = mongoClient.getDatabase("ReuterDb");
		MongoCollection news_collection = cleaned_database.getCollection("News");

		DecimalFormat df = new DecimalFormat("#.#########");

		// to count total documents
		int n = (int) news_collection.countDocuments();
		System.out.println("TF-IDF (term frequency-inverse document frequency)");
		System.out.println("Total Documents : " + n);

		// Formatting the table
		Formatter format = new Formatter();
		format.format("%85s\n", "-".repeat(85));
		format.format("|%-15s|%-30s|%-20s|%-15s|\n", "Search Query", "Documents Containing Term(df)",
				"Total Documents(N)", "Log10(N/df)");
		format.format("%85s\n", "-".repeat(85));

		// count term frequency-inverse document frequency (TF-IDF)
		FindIterable<Document> documents = news_collection.find();
		MongoCursor<Document> cursor = documents.iterator();

		// Variable to count keywords frequency in different news articles
		int canada_frequency = 0;
		int vancouver_frequency = 0;
		int toronto_frequency = 0;

		while (cursor.hasNext()) {
			Document document = cursor.next();
			JSONObject object = new JSONObject(document);
			// Get news body text from single document
			String news_body = object.getString("body").trim();
			if (news_body.contains("Canada") || news_body.contains("canada")) {
				canada_frequency++;
			} else if (news_body.contains("Vancouver") || news_body.contains("vancouver")) {
				vancouver_frequency++;
			} else if (news_body.contains("Toronto") || news_body.contains("toronto")) {
				toronto_frequency++;
			}
		}

		format.format("|%-15s|%-30s|%-20s|%-15s|\n", "Canada", canada_frequency, n,
				df.format(Math.log10(n / canada_frequency)));
		format.format("|%-15s|%-30s|%-20s|%-15s|\n", "Halifax", vancouver_frequency, n,
				df.format(Math.log10(n / vancouver_frequency)));
		format.format("|%-15s|%-30s|%-20s|%-15s|\n", "Toronto", canada_frequency, n,
				df.format(Math.log10(n / toronto_frequency)));
		format.format("%85s\n", "-".repeat(85));
		System.out.println(format);

		// Count highest frequency of canada in all documents
		Formatter format2 = new Formatter();
		format2.format("%73s\n", "-".repeat(73));
		format2.format("|%-35s|%-35s|\n", "Term", "Canada");
		format2.format("%73s\n", "-".repeat(73));
		format2.format("|%-35s|%-17s|%-17s|\n", "Canada Appeared in documents", "Total Words(m)", "Frequencey(f)");
		format2.format("%73s\n", "-".repeat(73));

		// Counting canada frequency in each docuemnt
		documents = news_collection.find();
		cursor = documents.iterator();

		// Counter for articles
		int article_count = 1;
		// variable to find max frequency
		float max = 0;
		float highest_ratio = 0;
		JSONObject highest_words_document = null;
		while (cursor.hasNext()) {
			Document document = cursor.next();
			JSONObject object = new JSONObject(document);
			// Get news body text from single document
			String news_body = object.getString("body").trim();
			String[] words = news_body.split(" ");
			int total_words = words.length;
			int frequency = 0;
			for (String word : words) {
				if (word.equals("Canada") || word.equals("canada"))
					frequency++;
			}

			float temp = (float) frequency / (float) total_words;
			if (temp > max) {
				highest_words_document = new JSONObject(document);
				highest_ratio = temp;
			}

			if (frequency > 0)
				format2.format("|%-35s|%-17s|%-17s|\n", "Article #" + article_count, total_words, frequency);
			article_count++;
		}
		
		format2.format("%73s\n", "-".repeat(73));
		System.out.println(format2);
		System.out.println("Document with highest frequency of Canada term");
		System.out.println("Frequency/Total words = " + highest_ratio);
		System.out.println("Title : " + highest_words_document.getString("title"));
		System.out.println("Body : " + highest_words_document.getString("body"));
	}
}
