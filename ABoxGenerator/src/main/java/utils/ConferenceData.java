/** For each confersity instance, College instances (Both Women and Co-Ed) and Research Groups are generated. 
 * And Basic hasName, hasCode data property assertion axioms are generated
* In order to modify the min-max range,that is, to modify the density of each node, user can make changes in the config.properties file */

package utils;

import genact.temporal.data.generator.DataGenerator;
import org.apache.jena.query.ARQ;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.jena.rdf.model.RDFNode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import org.yaml.snakeyaml.Yaml;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.vocabulary.RDF;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.jena.query.DatasetFactory;
import java.io.OutputStream;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.*;
import java.text.SimpleDateFormat;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.RDFDataMgr;

/*
randomly picked a paper to map the last authors in the author list as the keynote, trackchairs, organizers
*/
//for each conference, we can have a start time that's within a range. For the dataset we provide we chose 6 months, users
//can change it
public class ConferenceData {
	DataGenerator gen;
	int confIndex;
	String confId, confName, confInstance, confURL;
	String profile;
	int acceptedPaperCount;
	int otherPeopleInvolved;
	int seed;
	Random random = new Random();
	Dataset dataset = DatasetFactory.create();
	int graphCounter = 1;
	HashSet<String> attendees = new HashSet<>();
	HashSet<String> nonAttendees = new HashSet<>();
	HashSet<String> organizers = new HashSet<>();
	HashSet<String> chairs = new HashSet<>();
	List<String[]> confTweets = new ArrayList<>();
	Map<String, List<String[]>> userTweets = new HashMap<>();
	File streamFile;
	Map<String, Map<String, Object>> papers;
	PrintWriter writer;
	String[] categories = { "Conference Announcement", "Call for Papers", "Submission Reminder", "Notification",
			"Registration Reminder", "Before Conference", "During Conference", "After Conference" };
	long currentTimeMillis;
	String OWL2StreamBench_URL = "https://kracr.iiitd.edu.in/OWL2StreamBench#";
	String OWL2Bench_URL = "https://kracr.iiitd.edu.in/OWL2Bench#";
	Model tweetModel = ModelFactory.createDefaultModel();
	String directoryPath;
	File streamsDirectory;
	File tweetFile_n3;
	File tweetFile_rdf;
	Property rdfSubject = RDF.subject;
	Property rdfPredicate = RDF.predicate;
	Property rdfObject = RDF.object;
	Property hasDateTimestamp = tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
	RDFWriter tweetModelWriter;
	long randomOffsetMillis;
	long conferenceStartMillis;
	private Map<String, Model> namedGraphs;
	int conferenceDuration_min_months;
	int conferenceDuration_max_months;
	int conferenceDuration_months;
	long startTimestampMillis;

	public void ConferenceStreams(DataGenerator gen, int confIndex, String confInstance,
			Map<String, Map<String, Object>> papers, long startTimestampMillis, String directoryPath, int confCycle) {

		this.gen = gen;
		this.papers = papers;
		this.startTimestampMillis = startTimestampMillis;
		this.profile = gen.profile;
		this.confIndex = confIndex;
		this.confName = confInstance;// how to make sure that the years are increasing
		this.confInstance = confInstance; // year will be a variable
		this.confId = confInstance;
		this.seed = confIndex + 10000 * confCycle;
		this.random.setSeed((long) seed);

		this.confURL = "https://www." + confInstance + ".com";
		this.acceptedPaperCount = this.random.nextInt(gen.acceptedPaperCount_max - gen.acceptedPaperCount_min + 1)
				+ gen.acceptedPaperCount_min;
		this.otherPeopleInvolved = this.random.nextInt(gen.otherPeopleInvolved_max - gen.otherPeopleInvolved_min + 1)
				+ gen.otherPeopleInvolved_min;
		this.streamsDirectory = new File(gen.directoryPath + "/Streams/");
		if (!streamsDirectory.exists()) {
			streamsDirectory.mkdirs();
		}
		// this.tweetFile_n3 = new File(this.streamsDirectory + "/"+this.confName +
		// ".n3");
		this.tweetFile_rdf = new File(this.streamsDirectory + "/" + this.confName + ".n3");

		System.out.println("file name check" + this.tweetFile_rdf);
		Properties prop = new Properties();
		InputStream input = null;
//		this.confTweets=confTweets;
//		/*
//		 * Code for mapping papers and authors with the conference name
//		 */
//		// also include some triples about Non-Academic Organizations, non-author

		Map<String, Map<String, Object>> papersAccepted = getRandomPapers(papers, this.acceptedPaperCount);
		Map<String, Map<String, Object>> extraUsers = getRandomPapers(papers, this.otherPeopleInvolved);
		this.conferenceDuration_months = this.random
				.nextInt(gen.conferenceDuration_max_months - gen.conferenceDuration_min_months + 1)
				+ gen.conferenceDuration_min_months;
		long monthsInMillis = this.conferenceDuration_months * 30L * 24L * 60L * 60L * 1000L;
		this.randomOffsetMillis = (long) (this.random.nextDouble() * monthsInMillis);
		this.conferenceStartMillis = this.startTimestampMillis + randomOffsetMillis;
		System.out.println(this.conferenceStartMillis);
		/*
		 * Tweets about a certain conference are always posted in a certain order It
		 * starts with announcement tweets. Conference announcement first by
		 * generalChair, followed by track announcements by track chairs
		 */

		loadBeforeConferenceTemplates("C:/GitHub/owl2streambench/Mappings/BeforeConferenceTemplates");
		loadDuringConferenceTemplates("C:/GitHub/owl2streambench/Mappings/DuringConferenceTemplates");
		loadAfterConferenceTemplates("C:/GitHub/owl2streambench/Mappings/AfterConferenceTemplates");

//		for (int i = 0; i < categories.length; i++) {
//			String category = categories[i];
//
//			// Generate tweets with the specified frequency for each category
//			int tweetFrequency = getTweetFrequency(category);
//
//			for (int j = 0; j < tweetFrequency; j++) {
//				// String formattedTimestamp = formatTimestamp(currentTimeMillis);
//				generateTweet(category, currentTimeMillis); // (pass timestamp also)
//
//				// Increment time with a random variation within the specified time range
//				long timeIncrementMillis = getTimeIncrementMillis(category);
//				currentTimeMillis += timeIncrementMillis;
//			}
//		}
//		// saveNamedGraphsToFile(namedGraphs,this.OWL2StreamBench_URL);
//		System.out.println("checking if file exists  ");
//
//		if (!this.tweetFile_rdf.exists()) {
//			try {
//				// Create a new file
//				this.tweetFile_rdf.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
////		try {
////			RDFDataMgr.write(new FileOutputStream(tweetFile_rdf), tweetModel, Lang.RDFXML);
////		} catch (FileNotFoundException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		// Save the model to a file in RDF format
//		this.tweetModelWriter = tweetModel.getWriter("N3");
//		try {
//			tweetModelWriter.write(tweetModel, new FileOutputStream(tweetFile_rdf), null);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	public void loadBeforeConferenceTemplates(String directoryPath) {
		List<TemplateEntry> sequenceTemplates = loadTemplates(directoryPath + "/sequence.yaml");
		//List<TemplateEntry> randomTemplates = loadTemplates(directoryPath + "/random.yaml");
		// code to generate the rdf triples based on the mapping file
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		// Execute processing for sequenceTemplates in one thread
		executorService.submit(() -> processTemplates(sequenceTemplates));
		// Execute processing for randomTemplates in another thread
	//	executorService.submit(() -> processTemplates(randomTemplates));
		// Shutdown the executor service when processing is complete
		executorService.shutdown();
	}

	private void processTemplates(List<TemplateEntry> templates) {
		for (TemplateEntry template : templates) {
			// Your processing logic here
			System.out.println(" Template - Frequency: " + template.getFrequency());
			//System.out.println(" Template - Min Duration: " + template.getMinDuration());
			//System.out.println(" Template - Max Duration: " + template.getMaxDuration());
			 for (String placeholder : template.getPlaceholders()) {
				 System.out.println(" : " +placeholder);
		            readMappingFile(placeholder);
		        }
			System.out.println("---------------");
		}
	}
	private void readMappingFile(String placeholder) {
	    try (FileInputStream input = new FileInputStream("C:/GitHub/owl2streambench/Mappings/mapping.yaml")) {
	        Yaml yaml = new Yaml();
	        Map<String, List<Map<String, List<Map<String, String>>>>> mappingData = yaml.load(input);

	        if (mappingData != null) {
	            // Find the entry corresponding to the placeholder
	            List<Map<String, List<Map<String, String>>>> placeholderMappings = mappingData.get(placeholder);

	            if (placeholderMappings != null) {
	                // Iterate over the mappings for the given placeholder
	                for (Map<String, List<Map<String, String>>> mapping : placeholderMappings) {
	                    for (Map.Entry<String, List<Map<String, String>>> entry : mapping.entrySet()) {
	                        String type = entry.getKey();
	                        List<Map<String, String>> triplesList = entry.getValue();

	                        System.out.println("type: " + type);

	                        // Iterate over the RDF triples for the current placeholder
	                        for (Map<String, String> triple : triplesList) {
	                            for (Map.Entry<String, String> tripleEntry : triple.entrySet()) {
	                                String predicate = tripleEntry.getKey();
	                                String object = tripleEntry.getValue();

	                                System.out.println("  " + predicate + ": " + object);
	                            }
	                        }
	                        System.out.println("---------------");
	                    }
	                }
	            } else {
	                System.out.println("No mapping found for placeholder: " + placeholder);
	            }
	        } else {
	            System.out.println("Mapping data is null");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}


//	 private void readMappingFile(String placeholder) {
//	        try (FileInputStream input = new FileInputStream("C:/GitHub/owl2streambench/Mappings/mapping.yaml")) {
//	            Yaml yaml = new Yaml();
//	            Map<String, List<Map<String, String>>> mappingData = yaml.load(input);
//
//	            if (mappingData != null) {
//	                for (Map.Entry<String, List<Map<String, String>>> entry : mappingData.entrySet()) {
//	                    String type = entry.getKey();
//	                    List<Map<String, String>> triplesList = entry.getValue();
//
//	                    System.out.println("type: " + type);
//
//	                    // Iterate over the RDF triples for the current placeholder
//	                    for (Map<String, String> triple : triplesList) {
//	                        for (Map.Entry<String, String> tripleEntry : triple.entrySet()) {
//	                            String predicate = tripleEntry.getKey();
//	                            String object = tripleEntry.getValue();
//
//	                            System.out.println("  " + predicate + ": " + object);
//	                        }
//	                    }
//	                    System.out.println("---------------");
//	                }
//	            }
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//	    }
	public void loadDuringConferenceTemplates(String directoryPath) {
		List<TemplateEntry> sequenceTemplates = loadTemplates(directoryPath + "/sequence.yaml");
		List<TemplateEntry> randomTemplates = loadTemplates(directoryPath + "/random.yaml");
		// System.out.println(sequenceTemplates);
		// System.out.println(randomTemplates);
	}

	public void loadAfterConferenceTemplates(String directoryPath) {
		// List<TemplateEntry> sequenceTemplates = loadTemplates(directoryPath +
		// "/sequence.yaml");
		List<TemplateEntry> randomTemplates = loadTemplates(directoryPath + "/random.yaml");
		// System.out.println(sequenceTemplates);
		// System.out.println(randomTemplates);
	}

	private List<TemplateEntry> loadTemplates(String filePath) {
		List<TemplateEntry> templates = new ArrayList<>();
		try (FileInputStream input = new FileInputStream(new File(filePath))) {

			Yaml yaml = new Yaml();
			Map<String, List<Map<String, Object>>> templateData = yaml.load(input);

			if (templateData != null) {
				List<Map<String, Object>> entries = templateData.get("templates");

				if (entries != null) {
					for (Map<String, Object> entry : entries) {
						// System.out.println(entry.get("body"));
						TemplateEntry templateEntry = extractTemplate(entry);
						templates.add(templateEntry);

					}
				}

			} else {
				System.out.println("Failed to load YAML data. Check the YAML file structure.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return templates;
	}

	private TemplateEntry extractTemplate(Map<String, Object> entry) {
		// String templateName = (String) entry.get("template_name");

		double frequency = (double) entry.get("frequency");
		// System.out.println(frequency);
		// double minDuration = (double) entry.get("min_duration");
		// double maxDuration = (double) entry.get("max_duration");
		String body = (String) entry.get("body");
		// System.out.println(frequency);
		// Extract placeholders from the body
		List<String> placeholders = extractPlaceholders(body);
		// System.out.println(placeholders);
		return new TemplateEntry(frequency, placeholders);
	}

	private List<String> extractPlaceholders(String body) {
		List<String> placeholders = new ArrayList<>();
		// Regular expression to find text within square brackets
		Pattern pattern = Pattern.compile("\\[(.*?)\\]");
		Matcher matcher = pattern.matcher(body);

		while (matcher.find()) {
			// Add the text within square brackets as a placeholder
			placeholders.add(matcher.group(1));
		}

		return placeholders;
	}

	public class TemplateEntry {
		// private String templateName;
		private double frequency;
		private double minDuration;
		private double maxDuration;
		private List<String> placeholders;

		public TemplateEntry(double frequency, List<String> placeholders) {
			// this.templateName = templateName;
			this.frequency = frequency;
			this.minDuration = minDuration;
			this.maxDuration = maxDuration;
			this.placeholders = placeholders;
		}

//	        public String getTemplateName() {
//	            return templateName;
//	        }

		public double getFrequency() {
			return frequency;
		}

		public double getMinDuration() {
			return minDuration;
		}

		public double getMaxDuration() {
			return maxDuration;
		}

		public List<String> getPlaceholders() {
			return placeholders;
		}
	}

	public int getTweetFrequency(String category) {
		// Define tweet frequencies for each category
		switch (category) {
		case "Conference Announcement":
			return 1; // Once at the start
		case "Call for Papers":
			return 7; // Approximately 1 tweet per day for a week
		case "Submission Reminder":
			return 2; // Approximately 1 tweet every 2-3 weeks
		case "Notification":
			return 5; // High frequency for a few days after a long gap
		case "Registration Reminder":
			return 12; // Approximately 1 tweet every 2-3 weeks
		case "Before Conference":
			return 4; // Approximately 1 tweet per week for 2 weeks
		case "During Conference":
			return 20; // High frequency during conference days
		case "After Conference":
			return 5; // High frequency for a few days after the conference
		default:
			return 1; // check this
		}
	}

	private void generateTweet(String category, long currentTimeMillis) {
		// You can define your tweet templates here for each category
		// and fill them with relevant information.
		// Example: "We're excited to announce the 10th edition of XYZ Conference..."
		String formattedTimestamp = formatTimestamp(currentTimeMillis);
		Map<String, Map<String, Object>> papersAccepted = getRandomPapers(this.papers, this.acceptedPaperCount);
		Map<String, Map<String, Object>> extraUsers = getRandomPapers(this.papers, this.otherPeopleInvolved);
		if (category == "Conference Announcement") {
			conferenceAnnouncement(this.gen, extraUsers, formattedTimestamp);
		} else if (category == "Call for Papers") {
			callForPapers(this.gen, extraUsers, formattedTimestamp);
		} else if (category == "Submission Reminder") {
			submissionReminders(this.gen, papersAccepted, formattedTimestamp); // deadlines approaching
		} else if (category == "Notification") {
			reviewNotificationsAndAcceptance(this.gen, papersAccepted, extraUsers, formattedTimestamp);
		} else if (category == "Registration Reminder") {
			registrationReminders(this.gen, papersAccepted, formattedTimestamp);
		} else if (category == "Before Conference") {
			beforeConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees, formattedTimestamp);
		} else if (category == "During Conference") {
			duringConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees, formattedTimestamp);
		} else if (category == "After Conference") {
			afterConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees, formattedTimestamp);
		}
	}

	private static long getTimeIncrementMillis(String category) {
		// Define time ranges and random variations for each category in milliseconds
		switch (category) {
		case "Conference Announcement":
			return 0L;
		case "Call for Papers":
			return getRandomTimeIncrement(7L * 24L * 60L * 60L * 1000L, 1, 3); // 1 week with random variation
		case "Submission Reminder":
			return getRandomTimeIncrement(21L * 24L * 60L * 60L * 1000L, 2, 3); // 3 weeks with random variation
		case "Notification":
			return getRandomTimeIncrement(45L * 24L * 60L * 60L * 1000L, 1, 3); // 1.5 months with random variation
		case "Registration Reminder":
			return getRandomTimeIncrement(105L * 24L * 60L * 60L * 1000L, 2, 3); // 3.5 months with random variation
		case "Before Conference":
			return getRandomTimeIncrement(14L * 24L * 60L * 60L * 1000L, 1, 3); // 2 weeks with random variation
		case "During Conference":
			return getRandomTimeIncrement(5L * 24L * 60L * 60L * 1000L, 1, 2); // 5 days with random variation
		case "After Conference":
			return getRandomTimeIncrement(45L * 24L * 60L * 60L * 1000L, 1, 3); // 1.5 months with random variation
		default:
			return 0L;
		}
	}

	private static long getRandomTimeIncrement(long baseTimeIncrement, int minMultiplier, int maxMultiplier) {
		// Generate a random time increment within the specified range
		int randomMultiplier = minMultiplier + new Random().nextInt(maxMultiplier - minMultiplier + 1);
		return baseTimeIncrement * randomMultiplier;
	}

	public void objectPropertyAssertion(Resource Tweet, String subject, String predicate, String object) {

		Property information = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasInformation");
		Resource Information = this.tweetModel.createResource(OWL2StreamBench_URL + "information" + UUID.randomUUID());
		tweetModel.add(this.tweetModel.createStatement(Tweet, information, Information));

		Resource Subject = this.tweetModel.createResource(subject);
		Property Predicate = this.tweetModel.createProperty(predicate);
		Resource Object = this.tweetModel.createResource(object);
		tweetModel.add(this.tweetModel.createStatement(Information, rdfSubject, Subject));
		tweetModel.add(this.tweetModel.createStatement(Information, rdfPredicate, Predicate));
		tweetModel.add(this.tweetModel.createStatement(Information, rdfObject, Object));

	}

	public void dataPropertyAssertion(Resource Tweet, String subject, String predicate, String object) {
		Property information = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasInformation");
		Resource Information = this.tweetModel.createResource(OWL2StreamBench_URL + "information" + UUID.randomUUID());
		tweetModel.add(this.tweetModel.createStatement(Tweet, information, Information));

		Resource Subject = this.tweetModel.createResource(subject);
		Property Predicate = this.tweetModel.createProperty(predicate);
		Literal Object = this.tweetModel.createTypedLiteral(object);
		tweetModel.add(this.tweetModel.createStatement(Information, rdfSubject, Subject));
		tweetModel.add(this.tweetModel.createStatement(Information, rdfPredicate, Predicate));
		tweetModel.add(this.tweetModel.createStatement(Information, rdfObject, Object));
	}

	// Helper method to get a random element from a set
	private static <T> T getRandomElement(Set<T> set, Random random) {
		// Random random = new Random();
		int index = random.nextInt(set.size());
		int i = 0;
		for (T element : set) {
			if (i == index) {
				return element;
			}
			i++;
		}
		throw new IllegalArgumentException("Set is empty or index is out of bounds");
	}

	public Map<String, Map<String, Object>> getRandomPapers(Map<String, Map<String, Object>> papersAccepted, int n) {
		Map<String, Map<String, Object>> randomPapers = new HashMap<>();

		List<String> paperIds = new ArrayList<>(papersAccepted.keySet());
		int totalPapers = paperIds.size();

		// Shuffle the paperIds list to randomize the selection
		Collections.shuffle(paperIds);

		// Select 'n' random papers and add them to the new map
		for (int i = 0; i < n && i < totalPapers; i++) {
			String paperId = paperIds.get(i);
			randomPapers.put(paperId, papersAccepted.get(paperId));
		}

		return randomPapers;
	}

//	public void sleep() {
//		try {
//			Thread.sleep(this.random.nextInt(1000));
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	public void conferenceAnnouncement(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			String formattedTimestamp) {
		/*
		 * (Announcement Template) We're excited to announce the [n-th] edition of
		 * [@conf]! The deadlines for submissions, registrations, and other important
		 * dates have been announced. Visit our website [@ConferenceWebsiteURL]for more
		 * details and stay tuned for updates.
		 */

		// Add class assertion to the static dataset file

		String instance = OWL2StreamBench_URL + confInstance;
		String concept = OWL2StreamBench_URL + "Conference";
		gen.classAssertion(concept, instance);

		Set<String> keys = papersAccepted.keySet();
		String randomKey = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
		Map<String, Object> paperInfo = papersAccepted.get(randomKey);
		List<String> authorNames = (List<String>) paperInfo.get("authors");
		String lastAuthor = authorNames.get(authorNames.size() - 1);
		// Triples related to the tweet posted by the authors
		String userId = lastAuthor;
//		twitter user posts some tweet. each tweet has information
		Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
		Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
		String tweet = "tweet" + UUID.randomUUID();
		Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
		gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
		tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

		Literal DateTimestamp = this.tweetModel.createTypedLiteral("2000-03-24T16:13:14");
		Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
		Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
		tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
		tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

		this.dataPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance, OWL2StreamBench_URL + "hasConferenceName",
				OWL2StreamBench_URL + confName);
		this.dataPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance, OWL2StreamBench_URL + "hasId",
				OWL2StreamBench_URL + confId);
		this.dataPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance, OWL2StreamBench_URL + "hasWebsiteURL",
				"www.kracr.iiitd.edu.in." + confId);
		this.objectPropertyAssertion(Tweet, "www.kracr.iiitd.edu.in." + confInstance,
				"https://kracr.iiitd.edu.in/OWL2StreamBench#hasGeneralChair", OWL2StreamBench_URL + lastAuthor);
		this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
				OWL2StreamBench_URL + "hasGeneralChairRoleAt", "www.kracr.iiitd.edu.in." + confInstance);
		this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "hasRole",
				OWL2StreamBench_URL + "generalChair");
		int random = this.random.nextInt(gen.TOKEN_EventMode.length);
		String eventMode = gen.TOKEN_EventMode[random];
		this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance, OWL2StreamBench_URL + "hasMode",
				eventMode);
		if (eventMode.matches("offline") || eventMode.matches("hybrid")) {
			this.dataPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance, OWL2StreamBench_URL + "hasLocation",
					"location");
		}
		organizers.add(lastAuthor);

//		String graphUri = getNextGraphURI();
//		if (namedGraphs == null) {
//			namedGraphs = new HashMap<>();
//		}
//		Model namedGraph = namedGraphs.computeIfAbsent(graphUri, k -> ModelFactory.createDefaultModel());
//
//		addTripleToGraph2(namedGraph, Tweet, hasDateTimestamp, DateTimestamp);
//		addTripleToGraph(namedGraph, Tweet, isAbout, UserId);
//		// addTripleToGraph(namedGraph, Tweet, rdfObject, Object, null);
	}

	public void callForPapers(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			String formattedTimestamp) {
//		We extend a warm invitation to researchers and scholars to participate in [Conference] by submitting your papers to 
//		the [track]. Share your pioneering research and valuable insights with the academic community. Don't miss this 
//		opportunity to make a meaningful contribution to the success of [Conference]! The submission deadline is 
//		[Submission Deadline] [Individual Track Chairs]
		// there could be two chairs for each track. also include this info @mention
		// maybe
		int randomNum = gen.TOKEN_ConferenceEventTrack.length;
		HashSet<Integer> set = new HashSet<>();
		// random track announcements
		while (set.size() < randomNum) {
			int random = this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
			set.add(random);
		}
		// pick random last authors from the extraUsers as different trackc chairs
		for (int value : set) {
			int length = papersAccepted.size();
			String track = gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			Set<String> keys = papersAccepted.keySet();
			String randomKey = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = papersAccepted.get(randomKey);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);

			// Triples related to the tweet posted by the authors
			String userId = lastAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

//			twitter user posts some tweet. each tweet has information
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
					OWL2StreamBench_URL + "has" + track + "ChairRoleAt", OWL2StreamBench_URL + confInstance);
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "hasRole",
					OWL2StreamBench_URL + track + "Chair");
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance,
					OWL2StreamBench_URL + "hasPaperTrack", OWL2StreamBench_URL + track);
			organizers.add(lastAuthor);
		}
	}

	public void submissionReminders(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			String formattedTimestamp) {

//		 Just a reminder, only [insert number] days left to submit your papers for [Conference2023] in the [Track] category. 
//		Don't miss out on this opportunity to showcase your research. Please ensure your papers are submitted by [insert date]. [Individual Track Chairs]
		int randomNum = gen.TOKEN_ConferenceEventTrack.length;
		HashSet<Integer> set = new HashSet<>();
		// random track announcements
		while (set.size() < randomNum) {
			int random = this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
			set.add(random);
		}
		// pick random last authors from the extraUsers as different trackc chairs
		for (int value : set) {
			int length = papersAccepted.size();
			String track = gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			Set<String> keys = papersAccepted.keySet();
			String randomKey = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = papersAccepted.get(randomKey);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);

			// Triples related to the tweet posted by the authors
			String userId = lastAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
					OWL2StreamBench_URL + "has" + track + "ChairRoleAt", OWL2StreamBench_URL + confInstance);
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "hasRole",
					OWL2StreamBench_URL + track + "Chair");
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance,
					OWL2StreamBench_URL + "hasPaperTrack", OWL2StreamBench_URL + track);
			organizers.add(lastAuthor);
		}
	}

	public void reviewNotificationsAndAcceptance(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, String formattedTimestamp) {
		// pick some random first authors from the papermap 2 as doctoral consortium
		// students
		for (int i = 0; i < extraUsers.size() * 0.3; i++) {
			Set<String> keys = extraUsers.keySet();
			String paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(paperId);
			String firstAuthor, paperTitle;

			if (extraUsers.containsKey(paperId)) {
				List<String> authorsList = (List<String>) extraUsers.get(paperId).get("authors");
				firstAuthor = authorsList.get(0);
				paperTitle = (String) extraUsers.get(paperId).get("title");
				attendees.add(firstAuthor);

				// Triples related to the tweet posted by the authors
				String userId = firstAuthor;
				Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
				Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
				String tweet = "tweet" + UUID.randomUUID();
				Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
				gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
				tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

				Literal DateTimestamp = this.tweetModel
						.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
				Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
				Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
				tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
				tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2StreamBench_URL + "attends",
						OWL2StreamBench_URL + confInstance);
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2StreamBench_URL + "presents",
						OWL2StreamBench_URL + paperId);
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor,
						OWL2StreamBench_URL + "hasAcceptedPaper", OWL2StreamBench_URL + paperId);
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
						OWL2StreamBench_URL + "hasPaperTrack", OWL2StreamBench_URL + "doctoralConsortiumTrack");
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
						OWL2StreamBench_URL + "isPresentedAt", OWL2StreamBench_URL + confInstance);
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasTitle",
						OWL2StreamBench_URL + paperTitle);
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
						OWL2StreamBench_URL + "hasPaperDomain",
						OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);
				String instance = OWL2StreamBench_URL + firstAuthor;
				String concept = OWL2StreamBench_URL + "Student";
				gen.classAssertion(concept, instance);

			}

		}

//		 We're thrilled to announce that our paper titled [PaperTitle] has been accepted in the [PaperTrack] at [Conference]! 
//		 Heartfelt congratulations to our co-authors [Coauthor1, Coauthor2, …]. We look forward to sharing our research with you at the conference! [Author]
		// coauthors also included
		// pick random papers from paperMap 1 for the acceptance notifications (not all
		// the authors would post)
		String paperId2, postingAuthor;
		String paperTitle;
		List<String> authorsList;
		HashSet<String> postingAuthors = new HashSet<>();
		for (int i = 0; i < papersAccepted.size() * 0.7; i++) {
			Set<String> keys = extraUsers.keySet();
			String paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(paperId);
			if (!postingAuthors.contains(paperId)) {
				postingAuthors.add(paperId);
			}
		}
		for (String paperId : postingAuthors) {
			authorsList = (List<String>) extraUsers.get(paperId).get("authors");
			postingAuthor = authorsList.get(this.random.nextInt(authorsList.size()));
			paperTitle = (String) extraUsers.get(paperId).get("title");

			// Triples related to the tweet posted by the authors
			String userId = postingAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + postingAuthor,
					OWL2StreamBench_URL + "hasAcceptedPaper", OWL2StreamBench_URL + paperId);
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "isAcceptedAt",
					OWL2StreamBench_URL + confInstance);
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasAuthor",
					OWL2StreamBench_URL + postingAuthor);
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasTitle",
					OWL2StreamBench_URL + paperTitle);
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasPaperDomain",
					OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);

			// now for some of these paperIds, generate co-authors

			if (this.random.nextInt(1) == 1) {
				// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
				for (String author : authorsList) {
					// do something with the author
					if (author.matches(postingAuthor)) {
					} else
//						gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
//								gen.getNamedIndividual(author));
						this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
								OWL2StreamBench_URL + "hasAuthor", OWL2StreamBench_URL + author);
//					gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"),
//							gen.getNamedIndividual(postingAuthor), gen.getNamedIndividual(author));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + postingAuthor,
							OWL2StreamBench_URL + "hasCoAuthor", OWL2StreamBench_URL + author);
				}

			}
		}

	}

	public void registrationReminders(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			String formattedTimestamp) {
//		Attention all attendees of [Conference]! Early bird registration is now open! Register before [insert date] to 
//		enjoy savings of [insert discount amount]. Secure your spot today and take advantage of this special offer. 
//		Regular registration will continue until [insert date], but why wait? Register now and be part of this exciting event! [Conference Chair]

	}

	public void beforeConferenceEvent(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, HashSet<String> attendees, String formattedTimestamp) {

		/*
		 * Thank you to the 〈SponsorName〉 for supporting my attendance at 〈Conf 6〉.
		 * Looking forward to representing 〈InstituteName〉 at the conference. Glad to
		 * share the news that I got the student grant to attend ⟨Conf 1⟩. Can’t wait to
		 * learn from all the amazing talks and meet so many incredible researchers at
		 * ⟨Conf Location⟩. Thank you to the 〈Organizers〉 for making this opportunity
		 * possible.
		 */
		Set<String> keys = papersAccepted.keySet();
		String paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
		Map<String, Object> paperInfo = papersAccepted.get(paperId);
		String firstAuthor, paperTitle;
		String organization;
		// int length=extraUsers.size();
		// randomly pick first authors
		if (papersAccepted.containsKey(paperId)) {
			List<String> authorsList = (List<String>) papersAccepted.get(paperId).get("authors");
			firstAuthor = authorsList.get(0);
			paperTitle = (String) papersAccepted.get(paperId).get("title");
			// Triples related to the tweet posted by the authors
			String userId = firstAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

			if (this.random.nextInt(1) == 1) {
				organization = "AcadOrg" + this.random.nextInt(100);
			} else
				organization = "NonAcadOrg" + this.random.nextInt(100);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(organization));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2Bench_URL + "hasAffiliation",
					OWL2Bench_URL + organization);
//			gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2StreamBench_URL + "attends",
					OWL2StreamBench_URL + confInstance);
			String instance = OWL2StreamBench_URL + firstAuthor;
			String concept = OWL2StreamBench_URL + "Student";
			gen.classAssertion(concept, instance);
			attendees.add(firstAuthor);

			/*
			 * Excited to be a volunteer at 〈Conf 1〉 this year! Looking forward to meeting
			 * all the attendees and helping out in any way I can.
			 */
			if (this.random.nextInt(1) == 1) {
//				gen.objectPropertyAssertion(gen.getObjectProperty("volunteersFor"), gen.getNamedIndividual(firstAuthor),
//						gen.getNamedIndividual(confInstance));

				// Triples related to the tweet posted by the authors
				userId = firstAuthor;
				UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
				Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
				tweet = "tweet" + UUID.randomUUID();
				Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
				gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
				tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

				DateTimestamp = this.tweetModel
						.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);

				tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
				tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor,
						OWL2StreamBench_URL + "volunteersFor", OWL2StreamBench_URL + confInstance);
				attendees.add(firstAuthor);
			}
		}

		/*
		 * We’re thrilled to announce our keynote speakers for 〈Conf 1〉: 〈Speaker1〉,
		 * 〈Speaker2〉, and 〈Speaker3〉! Be sure to catch their talks during the
		 * conference.
		 */

		int randomNum = this.random.nextInt(3) + 1;
		for (int value = 0; value < randomNum; value++) {
			int length = extraUsers.size();
			keys = extraUsers.keySet();
			paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			paperInfo = extraUsers.get(paperId);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			// Triples related to the tweet posted by the authors
			String userId = lastAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

//			gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(lastAuthor),
//					gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "attends",
					OWL2StreamBench_URL + confInstance);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasRole"), gen.getNamedIndividual(lastAuthor),
//					gen.getNamedIndividual("keynoteSpeakerRole"));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "hasRole",
					OWL2StreamBench_URL + "keynoteSpeakerRole");
//			gen.objectPropertyAssertion(gen.getObjectProperty("givesTalk"), gen.getNamedIndividual(lastAuthor),
//					gen.getNamedIndividual(confInstance+"keynoteTalk"+value));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "givesTalk",
					OWL2StreamBench_URL + confInstance + "keynoteTalk" + value);
//			gen.objectPropertyAssertion(gen.getObjectProperty("givenAt"), gen.getNamedIndividual(confInstance+"keynoteTalk"+value),
//					gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance + "keynoteTalk" + value,
					OWL2StreamBench_URL + "givenAt", OWL2StreamBench_URL + confInstance);
//			gen.dataPropertyAssertion(gen.getDataProperty("givesTalkOn"), gen.getNamedIndividual(lastAuthor),
//					gen.getLiteral(confInstance + "_keynoteTalk_" + value));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "givesTalkOn",
					OWL2StreamBench_URL + confInstance + "_keynoteTalk_" + value);
			// gen.classAssertion(gen.getClass("KeynoteTalks"),
			// gen.getNamedIndividual(confInstance+"keynoteTalk"+value));
			// gen.classAssertion(gen.getClass("KeynoteSpeakerRole"),
			// gen.getNamedIndividual("keynoteSpeakerRole"));
			String instance = OWL2StreamBench_URL + "keynoteSpeakerRole";
			String concept = OWL2StreamBench_URL + "KeynoteSpeakerRole";
			gen.classAssertion(concept, instance);
			attendees.add(lastAuthor);
//			gen.dataPropertyAssertion(gen.getDataProperty("hasDomain"), gen.getNamedIndividual(confInstance+"keynoteTalk"+value),
//					gen.getLiteral(gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance + "_keynoteTalk_" + value,
					OWL2StreamBench_URL + "hasDomain",
					OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);
		}

		if (this.random.nextInt(1) == 0) {
			randomNum = this.random.nextInt(2) + 1;
			for (int value = 0; value < randomNum; value++) {
				int length = extraUsers.size();
				keys = extraUsers.keySet();
				paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
				paperInfo = extraUsers.get(paperId);
				List<String> authorNames = (List<String>) paperInfo.get("authors");
				String lastAuthor = authorNames.get(authorNames.size() - 1);

				// Triples related to the tweet posted by the authors
				String userId = lastAuthor;
				Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
				Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
				String tweet = "tweet" + UUID.randomUUID();
				Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
				gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
				tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

				Literal DateTimestamp = this.tweetModel
						.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
				Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
				Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
				tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
				tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

//				gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(lastAuthor),
//						gen.getNamedIndividual(confInstance));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "attends",
						OWL2StreamBench_URL + confInstance);
//				gen.objectPropertyAssertion(gen.getObjectProperty("hasRole"), gen.getNamedIndividual(lastAuthor),
//						gen.getNamedIndividual("invitedTalkSpeakerRole"));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "hasRole",
						OWL2StreamBench_URL + "invitedTalkSpeakerRole");
//				gen.objectPropertyAssertion(gen.getObjectProperty("givesTalk"), gen.getNamedIndividual(lastAuthor),
//						gen.getNamedIndividual(confInstance+"invitedTalk"+value));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2StreamBench_URL + "givesTalk",
						OWL2StreamBench_URL + confInstance + "invitedTalk" + value);
//				gen.objectPropertyAssertion(gen.getObjectProperty("givenAt"), gen.getNamedIndividual(confInstance+"invitedTalk"+value),
//						gen.getNamedIndividual(confInstance));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance + "invitedTalk" + value,
						OWL2StreamBench_URL + "givenAt", OWL2StreamBench_URL + confInstance);
//				gen.dataPropertyAssertion(gen.getDataProperty("givesTalkOn"), gen.getNamedIndividual(lastAuthor),
//						gen.getLiteral(confInstance + "_invitedTalk_" + value));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
						OWL2StreamBench_URL + "givesTalkOn",
						OWL2StreamBench_URL + confInstance + "_invitedTalk_" + value);
				// gen.classAssertion(gen.getClass("InvitedTalks"),
				// gen.getNamedIndividual(confInstance+"invitedTalk"+value));
				String instance = OWL2StreamBench_URL + "invitedTalkSpeakerRole";
				String concept = OWL2StreamBench_URL + "InvitedTalkSpeakerRole";
				gen.classAssertion(concept, instance);
//				gen.classAssertion(gen.getClass("InvitedTalkSpeakerRole"),
//						gen.getNamedIndividual("invitedTalkSpeakerRole"));
//				gen.dataPropertyAssertion(gen.getDataProperty("hasDomain"), gen.getNamedIndividual(confInstance+"invitedTalk"+value),
//						gen.getLiteral(gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + confInstance + "invitedTalk" + value,
						OWL2StreamBench_URL + "hasDomain",
						OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);
				attendees.add(lastAuthor);
			}
		}

		/*
		 * Our research group 〈@Affiliation〉 is presenting <n> papers titled
		 * 〈@Paper1, @Paper2, …〉 at 〈Conf 4〉 this year. Looking forward to hearing about
		 * other great works as well.
		 */
		String researchGroup, college;
		randomNum = this.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < randomNum * 0.4; value++) {
			int length = papersAccepted.size();
			// String track=gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			keys = papersAccepted.keySet();
			researchGroup = "researchGroup" + this.random.nextInt(gen.researchGroupCount);
			int count = 0;
			while (this.random.nextInt(5) != 0 || count < 6) {
				paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
				Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
				List<String> authorNames = (List<String>) paperInfo2.get("authors");
				String lastAuthor = authorNames.get(authorNames.size() - 1);
				// Triples related to the tweet posted by the authors
				String userId = lastAuthor;
				Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
				Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
				String tweet = "tweet" + UUID.randomUUID();
				Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
				gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
				tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

				Literal DateTimestamp = this.tweetModel
						.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
				Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
				Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
				tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
				tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

				randomNum = this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length) + 1;
				HashSet<Integer> set = new HashSet<>();
				// random track announcements
				while (set.size() < randomNum) {
					int random = this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
					set.add(random);
				}
				// pick random last authors from the extraUsers as different trackc chairs
				for (int t : set) {
					// int length=extraUsers.size();
					String track = gen.TOKEN_ConferenceEventTrack[t];
					// researchGroup = "researchGroup" +
					// this.random.nextInt(gen.researchGroupCount);
//					gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),
//							gen.getNamedIndividual(lastAuthor), gen.getNamedIndividual(researchGroup));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
							OWL2Bench_URL + "hasAffiliation", OWL2Bench_URL + researchGroup);

//					gen.objectPropertyAssertion(gen.getObjectProperty("hasAcceptedPaper"),
//							gen.getNamedIndividual(lastAuthor), gen.getNamedIndividual(paperId));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
							OWL2StreamBench_URL + "hasAcceptedPaper", OWL2StreamBench_URL + paperId);
//
//					gen.objectPropertyAssertion(gen.getObjectProperty("isAcceptedAt"), gen.getNamedIndividual(paperId),
//							gen.getNamedIndividual(confInstance));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
							OWL2StreamBench_URL + "isAcceptedAt", OWL2StreamBench_URL + confInstance);

//					gen.objectPropertyAssertion(gen.getObjectProperty("hasPaperTrack"), gen.getNamedIndividual(paperId),
//							gen.getNamedIndividual(track));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
							OWL2StreamBench_URL + "hasPaperTrack", OWL2StreamBench_URL + track);
//
//					gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
//							gen.getLiteral(gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
							OWL2StreamBench_URL + "hasPaperDomain",
							OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);

				}
				count++;
			}
		}
		/*
		 * My student 〈@Person〉 will be presenting our work titled 〈@PaperTitle〉 at
		 * 〈@Conference〉 〈@Coauthor1, @Coauthor2, …〉.
		 * 
		 */
		randomNum = this.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < randomNum * 0.3; value++) {
			int length = papersAccepted.size();
			keys = papersAccepted.keySet();
			paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			firstAuthor = authorNames.get(0);

			// Triples related to the tweet posted by the authors
			String userId = lastAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

			researchGroup = "researchGroup" + this.random.nextInt(gen.researchGroupCount);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(lastAuthor),
//					gen.getNamedIndividual(researchGroup));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor, OWL2Bench_URL + "hasAffiliation",
					OWL2Bench_URL + researchGroup);

//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(researchGroup));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2Bench_URL + "hasAffiliation",
					OWL2Bench_URL + researchGroup);

//			gen.objectPropertyAssertion(gen.getObjectProperty("presents"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(paperId));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2StreamBench_URL + "presents",
					OWL2StreamBench_URL + paperId);

//			gen.objectPropertyAssertion(gen.getObjectProperty("isPresentedAt"), gen.getNamedIndividual(paperId),
//					gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "isPresentedAt",
					OWL2StreamBench_URL + confInstance);

			String instance = OWL2StreamBench_URL + firstAuthor;
			String concept = OWL2StreamBench_URL + "Student";
			gen.classAssertion(concept, instance);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAdvisor"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(lastAuthor));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2StreamBench_URL + "hasAdvisor",
					OWL2StreamBench_URL + lastAuthor);

//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
//					gen.getNamedIndividual(firstAuthor));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasAuthor",
					OWL2StreamBench_URL + firstAuthor);

//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
//					gen.getNamedIndividual(lastAuthor));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
					"https://kracr.iiitd.edu.in/OWL2StreamBench#hasAuthor", OWL2StreamBench_URL + lastAuthor);

//			gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
//					gen.getLiteral(gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasPaperDomain",
					OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);

			attendees.add(firstAuthor);

			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author
				if (author.matches(lastAuthor)) {
				} else
//					gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
//							gen.getNamedIndividual(author));
					this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId,
							OWL2StreamBench_URL + "hasAuthor", OWL2StreamBench_URL + author);
//				gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"), gen.getNamedIndividual(lastAuthor),
//						gen.getNamedIndividual(author));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + lastAuthor,
						OWL2StreamBench_URL + "hasCoAuthor", OWL2StreamBench_URL + author);

			}

		}
	}

	public void duringConferenceEvent(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, HashSet<String> attendees, String formattedTimestamp) {

		/*
		 * [@Attendee] Having a great time networking and meeting new people at
		 * [@Conference]. So many interesting conversations! or [@Attendee] Just got
		 * back from a poster session at [@Conference]. So many innovative ideas on
		 * display!
		 */
		/*
		 * [@Attendee] Just attended an amazing keynote speech at [@Conference] by
		 * [@KeynoteSpeaker]. Learned so much about 〈Topic〉!
		 */
		Set<String> keys = papersAccepted.keySet();
		// int randomNum = this.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < keys.size() - keys.size() / 5; value++) {

			String paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			// String firstAuthor = authorNames.get(0);
			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author
				if (author.matches(lastAuthor)) {
				} else {
					if (this.random.nextInt(2) == 1) {

						// Triples related to the tweet posted by the authors
						String userId = author;
						Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
						Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
						String tweet = "tweet" + UUID.randomUUID();
						Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
						gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
						tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

						Literal DateTimestamp = this.tweetModel
								.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
						Property hasDateTimestamp = this.tweetModel
								.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
						Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
						tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
						tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));
						String researchGroup = "researchGroup" + this.random.nextInt(gen.researchGroupCount);
//						gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),
//								gen.getNamedIndividual(author), gen.getNamedIndividual(researchGroup));
						this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + author,
								OWL2Bench_URL + "hasAffiliation", OWL2Bench_URL + researchGroup);
//						gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(author),
//								gen.getNamedIndividual(confInstance));
						this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + author,
								OWL2StreamBench_URL + "attends", OWL2StreamBench_URL + confInstance);
						attendees.add(author);
					}
				}

			}
		}

		// randomNum = this.random.nextInt(extraUsers.size()) + 1;
		keys = extraUsers.keySet();

		for (int value = 0; value < keys.size() - keys.size() / 5; value++) {

			String paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			// System.out.println(":.?"+extraUsers.size());
			Map<String, Object> paperInfo2 = extraUsers.get(paperId);

			List<String> authorNames = (List<String>) paperInfo2.get("authors");

			String lastAuthor = authorNames.get(authorNames.size() - 1);
			// String firstAuthor = authorNames.get(0);
			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors")

			for (String author : authorNames) {
				// do something with the author
				if (author.matches(lastAuthor)) {
				} else {

					// Triples related to the tweet posted by the authors
					String userId = author;
					Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
					Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
					String tweet = "tweet" + UUID.randomUUID();
					Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
					gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
					tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

					Literal DateTimestamp = this.tweetModel
							.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
					Property hasDateTimestamp = this.tweetModel
							.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
					Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
					tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
					tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));
					if (this.random.nextInt(2) == 1) {
//						gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(author),
//								gen.getNamedIndividual(confInstance));
						this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + author,
								OWL2StreamBench_URL + "attends", OWL2StreamBench_URL + confInstance);
						attendees.add(author);
						if (this.random.nextInt(3) == 1) {
							String researchGroup = "researchGroup" + this.random.nextInt(gen.researchGroupCount);
//							gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"),
//									gen.getNamedIndividual(author), gen.getNamedIndividual(researchGroup));
							this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + author,
									OWL2Bench_URL + "hasAffiliation", OWL2Bench_URL + researchGroup);
						}
					}

				}
			}
		}

		/*
		 * I 〈@Person〉 will be presenting our work titled 〈@PaperTitle〉 at 〈@Conference〉
		 * 〈@Coauthor1, @Coauthor2, …〉.
		 * 
		 */
		keys = papersAccepted.keySet();
		// randomNum = this.random.nextInt(papersAccepted.size()) + 1;
		for (int value = 0; value < keys.size() - keys.size() / 5; value++) {
			int length = papersAccepted.size();

			String paperId = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo2 = papersAccepted.get(paperId);
			List<String> authorNames = (List<String>) paperInfo2.get("authors");
			String lastAuthor = authorNames.get(authorNames.size() - 1);
			String firstAuthor = authorNames.get(0);

			// Triples related to the tweet posted by the authors
			String userId = firstAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

//			gen.objectPropertyAssertion(gen.getObjectProperty("presents"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(paperId));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2StreamBench_URL + "presents",
					OWL2StreamBench_URL + paperId);
			attendees.add(firstAuthor);
//			gen.objectPropertyAssertion(gen.getObjectProperty("isPresentedAt"), gen.getNamedIndividual(paperId),
//					gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "isPresentedAt",
					OWL2StreamBench_URL + confInstance);
			String researchGroup = "researchGroup" + this.random.nextInt(gen.researchGroupCount);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAffiliation"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(researchGroup));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor, OWL2Bench_URL + "hasAffiliation",
					OWL2Bench_URL + researchGroup);
//			gen.dataPropertyAssertion(gen.getDataProperty("hasPaperDomain"), gen.getNamedIndividual(paperId),
//					gen.getLiteral(gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasPaperDomain",
					OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);
			// authorsList = (List<String>) extraUsers.get(paperId2).get("authors");
			for (String author : authorNames) {
				// do something with the author
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + paperId, OWL2StreamBench_URL + "hasAuthor",
						OWL2StreamBench_URL + author);
//				gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(paperId),
//						gen.getNamedIndividual(author));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor,
						OWL2StreamBench_URL + "hasCoAuthor", OWL2StreamBench_URL + author);
//				gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"), gen.getNamedIndividual(firstAuthor),
//						gen.getNamedIndividual(author));
			}

			/*
			 * We're excited to start the [@Day] day [@Conference]! Here's a quick rundown
			 * of what's happening today: [Insert schedule of events]
			 */

			// gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"),gen.getNamedIndividual(confInstance),gen.getLiteral("onGoing"));

		}

	}

	public void afterConferenceEvent(DataGenerator gen, Map<String, Map<String, Object>> papersAccepted,
			Map<String, Map<String, Object>> extraUsers, HashSet<String> attendees, String formattedTimestamp) {

		/*
		 * Had a great time at 〈Conf 1〉! The keynote speeches, paper presentations, and
		 * networking events were all top-notch. Thanks to everyone who made it a
		 * memorable experience!
		 * 
		 */
		/*
		 * Congratulations to the organizing committee <O1, O2…>of 〈Conf 1〉 for putting
		 * together a successful event. Kudos to all the volunteers and sponsors who
		 * made it possible! See you all next year at 〈Future ConfLocation 〉
		 * 
		 */
		// organizers.add("David");

		// Retrieve random values from the attendees and organizers sets

		for (int value = 0; value < this.organizers.size() * 0.2; value++) {
			String randomOrganizer = getRandomElement(this.organizers, this.random);

			// Triples related to the tweet posted by the authors
			String userId = randomOrganizer;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));
//				gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(randomOrganizer ),	gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomOrganizer, OWL2StreamBench_URL + "attends",
					OWL2StreamBench_URL + confInstance);
		}
		for (int value = 0; value < this.attendees.size() * 0.7; value++) {
			String randomAttendee = getRandomElement(this.attendees, this.random);

			String userId = randomAttendee;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));
//			gen.objectPropertyAssertion(gen.getObjectProperty("attends"), gen.getNamedIndividual(randomAttendee),	gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomAttendee, OWL2StreamBench_URL + "attends",
					OWL2StreamBench_URL + confInstance);
		}

		/*
		 * Thrilled to announce that we got the best 〈PaperTrack-n〉 award at 〈Conf-n〉
		 * for our paper 〈PaperTitle-n〉. 〈Institute and Author Tags〉.
		 * 
		 */
		int randomNum = this.random.nextInt(papersAccepted.size()) + 1;
		randomNum = this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length) + 1;
		HashSet<Integer> set = new HashSet<>();
		// random track announcements
		while (set.size() < randomNum) {
			int random = this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length);
			set.add(random);
		}
		// pick random last authors from the extraUsers as different trackc chairs
		for (int value : set) {
			int length = extraUsers.size();
			String track = gen.TOKEN_ConferenceEventTrack[value];
			// randomly picking a paper to map the last author in the author list as the
			// track chair
			Set<String> keys = extraUsers.keySet();
			String randomKey = keys.toArray(new String[keys.size()])[this.random.nextInt(keys.size())];
			Map<String, Object> paperInfo = extraUsers.get(randomKey);
			List<String> authorNames = (List<String>) paperInfo.get("authors");
			String firstAuthor = authorNames.get(0);
			String paperTitle = (String) paperInfo.get("title");

			String userId = firstAuthor;
			Resource UserId = this.tweetModel.createResource(OWL2StreamBench_URL + userId);
			Property Posts = this.tweetModel.createProperty(OWL2StreamBench_URL + "posts");
			String tweet = "tweet" + UUID.randomUUID();
			Resource Tweet = this.tweetModel.createResource(OWL2StreamBench_URL + tweet);
			gen.classAssertion(OWL2StreamBench_URL + "Tweet", OWL2StreamBench_URL + tweet);
			tweetModel.add(this.tweetModel.createStatement(UserId, Posts, Tweet));

			Literal DateTimestamp = this.tweetModel
					.createTypedLiteral("http://www.w3.org/2001/XMLSchema#" + formattedTimestamp);
			Property hasDateTimestamp = this.tweetModel.createProperty(OWL2StreamBench_URL + "hasDateTimestamp");
			Property isAbout = this.tweetModel.createProperty(OWL2StreamBench_URL + "isAbout");
			tweetModel.add(this.tweetModel.createStatement(Tweet, isAbout, confInstance));
			tweetModel.add(this.tweetModel.createStatement(Tweet, hasDateTimestamp, DateTimestamp));

			for (String author : authorNames) {
				// do something with the author
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomKey, OWL2StreamBench_URL + "hasAuthor",
						OWL2StreamBench_URL + author);
//				gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(randomKey),
//						gen.getNamedIndividual(author));
				this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor,
						OWL2StreamBench_URL + "hasCoAuthor", OWL2StreamBench_URL + author);
//				gen.objectPropertyAssertion(gen.getObjectProperty("hasCoAuthor"), gen.getNamedIndividual(firstAuthor),
//						gen.getNamedIndividual(author));
			}
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + firstAuthor,
					"https://kracr.iiitd.edu.in/OWL2StreamBench#hasAcceptedPaper", OWL2StreamBench_URL + randomKey);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAcceptedPaper"), gen.getNamedIndividual(firstAuthor),
//					gen.getNamedIndividual(randomKey));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomKey,
					"https://kracr.iiitd.edu.in/OWL2StreamBench#isAcceptedAt", OWL2StreamBench_URL + confInstance);
//			gen.objectPropertyAssertion(gen.getObjectProperty("isAcceptedAt"), gen.getNamedIndividual(randomKey),
//					gen.getNamedIndividual(confInstance));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomKey,
					"https://kracr.iiitd.edu.in/OWL2StreamBench#hasPaperTrack", OWL2StreamBench_URL + track);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasPaperTrack"), gen.getNamedIndividual(randomKey),
//					gen.getNamedIndividual(track));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomKey,
					"https://kracr.iiitd.edu.in/OWL2StreamBench#hasAuthor", OWL2StreamBench_URL + firstAuthor);
//			gen.objectPropertyAssertion(gen.getObjectProperty("hasAuthor"), gen.getNamedIndividual(randomKey),
//					gen.getNamedIndividual(firstAuthor));
			this.objectPropertyAssertion(Tweet, OWL2StreamBench_URL + randomKey,
					"https://kracr.iiitd.edu.in/OWL2StreamBench#hasPaperDomain",
					OWL2StreamBench_URL + gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);
		}
	}

	/*
	 * Thank you to all the attendees, speakers, and sponsors who made [@Conference]
	 * a huge success! We hope you had a great time and learned a lot. See you at
	 * the next conference!
	 */
//		gen.dataPropertyAssertion(gen.getDataProperty("hasStatus"), gen.getNamedIndividual(confInstance),
//				gen.getLiteral("eventOver"));
//

	private static String formatTimestamp(long timestampMillis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date(timestampMillis));
	}

}

/*
 * conferenceAnnouncement(this.gen, extraUsers); trackAnnouncements(this.gen,
 * extraUsers); this.confTweets.add(new String[] { OWL2StreamBench_URL +
 * "Conference", OWL2StreamBench_URL + "hasStatus", OWL2StreamBench_URL +
 * "submissionReminders" }); sleep(); reminders(this.gen); // deadlines
 * approaching this.confTweets.add(new String[] { OWL2StreamBench_URL +
 * "Conference", OWL2StreamBench_URL + "hasStatus", OWL2StreamBench_URL +
 * "reviewNotifications" }); sleep(); reviewNotificationsAndAcceptance(this.gen,
 * papersAccepted, extraUsers); sleep(); this.confTweets.add(new String[] {
 * OWL2StreamBench_URL + "Conference", OWL2StreamBench_URL + "hasStatus",
 * OWL2StreamBench_URL + "registrationReminders" });
 * studentGrantsAndVolunteers(this.gen, papersAccepted); sleep();
 * this.confTweets.add(new String[] { OWL2StreamBench_URL + "Conference",
 * OWL2StreamBench_URL + "hasStatus", OWL2StreamBench_URL + "beforeConference"
 * }); beforeConferenceEvent(this.gen, papersAccepted, extraUsers,
 * this.attendees);
 * 
 * this.confTweets.add(new String[] { OWL2StreamBench_URL + "Conference",
 * OWL2StreamBench_URL + "hasStatus", OWL2StreamBench_URL + "duringConference"
 * }); duringConferenceEvent(this.gen, papersAccepted, extraUsers,
 * this.attendees); this.confTweets.add(new String[] { OWL2StreamBench_URL +
 * "Conference", OWL2StreamBench_URL + "hasStatus", OWL2StreamBench_URL +
 * "afterConference" });
 * 
 * afterConferenceEvent(this.gen, papersAccepted, extraUsers, this.attendees);
 * this.confTweets.add(new String[] { OWL2StreamBench_URL + "Conference",
 * OWL2StreamBench_URL + "hasStatus", OWL2StreamBench_URL + "over" });
 * 
 * // Create a new PrintWriter with the file path // PrintWriter writer = new
 * PrintWriter(streamFile); // for (String[] row : this.tweets) { // for (String
 * value : row) { // System.out.print(value + " "); // } //
 * System.out.println(?); // Print a new line after each row // } for (String[]
 * row : confTweets) { for (String value : row) { System.out.print(value + " ");
 * } System.out.println("?nn?"); // Print a new line after each row }
 * 
 */
