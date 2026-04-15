/** For each confersity instance, College instances (Both Women and Co-Ed) and Research Groups are generated. 
 * And Basic hasName, hasCode data property assertion axioms are generated
* In order to modify the min-max range,that is, to modify the density of each node, user can make changes in the config.properties file */

package utils;

import genact.temporal.data.generator.DataGenerator;
import org.apache.jena.query.ARQ;import java.util.UUID;
import org.apache.jena.rdf.model.*;
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
import org.apache.jena.query.*;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.RDFDataMgr;
import java.util.concurrent.CountDownLatch;
/*
randomly picked a paper to map the last authors in the author list as the keynote, trackchairs, organizers
*/
//for each conference, we can have a start time that's within a range. For the dataset we provide we chose 6 months, users
//can change it
public class ConferenceStreams2 {
	DataGenerator gen;
	int confIndex, confCycle;
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
	int year;
	Map<String, Map<String, Object>> papers;
	PrintWriter writer;
	String[] categories = { "Conference Announcement", "Call for Papers", "Submission Reminder", "Notification",
			"Registration Reminder", "Before Conference", "During Conference", "After Conference" };
	long currentTimeMillis;
	String genACT_URL = "https://kracr.iiitd.edu.in/genACT#";
	String OWL2Bench_URL = "https://kracr.iiitd.edu.in/OWL2Bench#";

	String directoryPath;
	File streamsDirectory;
	File tweetFile_n3;
	File tweetFile_rdf;
	Property rdfSubject = RDF.subject;
	Property rdfPredicate = RDF.predicate;
	Property rdfObject = RDF.object;

	RDFWriter tweetModelWriter;
	long randomOffsetMillis;
	long conferenceStartMillis;
	int conferenceDuration_min_months;
	int conferenceDuration_max_months;
	int conferenceDuration_months;
	long startTimestampMillis;
	long monthsInMillis;
	List<String> researchGroups = new ArrayList<>();
	List<String> colleges = new ArrayList<>();
	List<String> academicOrganizations = new ArrayList<>();
	List<String> nonAcademicOrganizations = new ArrayList<>();
	List<String> peopleDirectlyInvolvedList = new ArrayList<>();
	List<String> otherPeopleInvolvedList = new ArrayList<>();
	List<String> papersList = new ArrayList<>();
	List<String> cityList = new ArrayList<>();
	CountDownLatch latch;
	private static final double RANDOM_TEMPLATE_PROBABILITY = 0.5;
	public ConferenceStreams2(DataGenerator gen, int confIndex, String directoryPath) {
		// find the year from the initial time, this will be the year for the
		// conference, and in the next cycles +18
		// Initialize with the number of calls

		this.gen = gen;
		this.confCycle = gen.confCycle;
		this.papersList = gen.papersList;
		this.cityList = gen.cityList;
		this.otherPeopleInvolvedList = gen.otherPeopleInvolvedList;
		this.peopleDirectlyInvolvedList = gen.peopleDirectlyInvolvedList;
		this.researchGroups = gen.researchGroups;
		this.colleges = gen.colleges;
		this.academicOrganizations = gen.academicOrganizations;
		this.nonAcademicOrganizations = gen.nonAcademicOrganizations;
		this.startTimestampMillis = gen.startTimestampMillis;
		this.year = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date(startTimestampMillis)));
		this.confIndex = confIndex;
		
		int i=1;
		// how to make sure that the years are increasing
		while (i <= this.confCycle) {
			//System.out.println(" loop starts here" + i);
			//this.latch = new CountDownLatch(3);
			this.confInstance = "conf" + this.confIndex + "_" + this.year; // year will be a variable
			System.out.println(this.confInstance + " year" + this.year);
			this.confName = this.confInstance;
			this.confId = this.confInstance;
			this.seed = confIndex + 10000 * this.confCycle;
			this.random.setSeed((long) this.seed);
			this.confURL = "https://www." + this.confInstance + ".com";
			this.acceptedPaperCount = this.random.nextInt(gen.acceptedPaperCount_max - gen.acceptedPaperCount_min + 1)
					+ gen.acceptedPaperCount_min;
//		this.otherPeopleInvolved = this.random.nextInt(gen.otherPeopleInvolved_max - gen.otherPeopleInvolved_min + 1)
//				+ gen.otherPeopleInvolved_min;
			this.streamsDirectory = new File(gen.directoryPath + "/Streams/");
			if (!streamsDirectory.exists()) {
				streamsDirectory.mkdirs();
			}

			this.conferenceDuration_months = this.random
					.nextInt(gen.conferenceDuration_max_months - gen.conferenceDuration_min_months + 1)
					+ gen.conferenceDuration_min_months;
			this.monthsInMillis = this.conferenceDuration_months * 30L * 24L * 60L * 60L * 1000L;
			this.randomOffsetMillis = (long) (this.random.nextDouble() * this.monthsInMillis);
			this.conferenceStartMillis = this.startTimestampMillis + this.randomOffsetMillis;
			
			String mappingFilePath = "C:\\GitHub\\temporal-data-generator\\Mappings\\mapping.yaml";
	        String templatesFilePath = "C:\\GitHub\\temporal-data-generator\\Mappings\\templates.yaml";
	        
	        // Parse mapping and templates YAML files
	        Map<String, List<Triple>> triples = parseMappingFile(mappingFilePath);
	        List<TemplateEntry> templates = parseTemplatesFile(templatesFilePath);

	        // Create an instance of TemplateProcessor
	        TemplateProcessor templateProcessor = new TemplateProcessor(templates, triples);

			// Set the start time for new conference instance
			// end time of previous conference plus random 3-4 months ga
			this.conferenceStartMillis = this.monthsInMillis + (long) (this.random.nextDouble() * monthsInMillis)
					+ 3L * 30L * 24L * 60L * 60L * 1000L;
			this.year += 1;
			i++;
			//System.out.println(" loop ended here??" + i);
		}
	}

		 public static Map<String, List<Triple>> parseMappingFile(String filePath) {
		        Yaml yaml = new Yaml();
		        try {
		            String yamlContent = Files.readString(Paths.get(filePath));
		            Map<String, List<Map<String, String>>> parsedYaml = yaml.load(yamlContent);

		            Map<String, List<Triple>> triples = new HashMap<>();
		            for (String key : parsedYaml.keySet()) {
		                List<Map<String, String>> tripleMaps = parsedYaml.get(key);
		                List<Triple> tripleList = new ArrayList<>();
		                for (Map<String, String> tripleMap : tripleMaps) {
		                    String subject = tripleMap.get("subject");
		                    String predicate = tripleMap.get("predicate");
		                    String object = tripleMap.get("object");
		                    tripleList.add(new Triple(subject, predicate, object));
		                }
		                triples.put(key, tripleList);
		            }
		            return triples;
		        } catch (IOException e) {
		            e.printStackTrace();
		            return null;
		        }
		    }
    

    private static List<TemplateEntry> parseTemplatesFile(String filePath) {
        Yaml yaml = new Yaml();
        try {
            String yamlContent = Files.readString(Paths.get(filePath));
            return yaml.load(yamlContent);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
//	  public void processTemplates(List<TemplateEntry> templates, List<Triple> triples) {
//	        for (TemplateEntry template : templates) {
//	            if (template.getOrder().equals("Sequential")) {
//	                processSequentialTemplate(template, triples);
//	            } else {
//	                processRandomTemplate(template, triples);
//	            }
//	        }
//	    }
//
//	    private void processSequentialTemplate(TemplateEntry template, List<Triple> triples) {
//	        for (String placeholder : template.getPlaceholders()) {
//	            Triple mappedTriple = findMappedTriple(placeholder, triples);
//	            if (mappedTriple != null) {
//	                // Instantiate the mapped triple in the template
//	                // For example: Replace placeholder in template with mappedTriple's subject, predicate, or object
//	            }
//	        }
//	        // Process the template with instantiated triples
//	    }
//
//	    private void processRandomTemplate(TemplateEntry template, List<Triple> triples) {
//	        // Randomly decide whether to process the template based on its frequency
//	        if (random.nextDouble() < template.getFrequency()) {
//	            for (String placeholder : template.getPlaceholders()) {
//	                Triple mappedTriple = findMappedTriple(placeholder, triples);
//	                if (mappedTriple != null) {
//	                    // Instantiate the mapped triple in the template
//	                    // For example: Replace placeholder in template with mappedTriple's subject, predicate, or object
//	                }
//	            }
//	            // Process the template with instantiated triples
//	        }
//	    }
//
//	    private Triple findMappedTriple(String placeholder, List<Triple> triples) {
//	        // Logic to find the corresponding triple for the placeholder in the mapping file
//	        // Iterate through the triples list and check if any triple matches the placeholder
//	        for (Triple triple : triples) {
//	            if (triple.getSubject().equals(placeholder)
//	                    || triple.getPredicate().equals(placeholder)
//	                    || triple.getObject().equals(placeholder)) {
//	                return triple;
//	            }
//	        }
//	        return null; // Placeholder not found in the mapping triples
//	    }
//	public long readMappingFile(TemplateEntry twitterTemplate, Long currentTimeMillis) {
//	    try (FileInputStream input = new FileInputStream(new File(gen.directoryPath + "/Mappings/mapping.yaml"))) {
//	        Yaml yaml = new Yaml();
//	        Map<String, Object> data = yaml.load(input);
//
//	        if (data != null) {
//	            List<Map<String, Object>> templates = (List<Map<String, Object>>) data.get("templates");
//
//	            if (templates != null) {
//	                String tweet = genACT_URL + "tweet" + UUID.randomUUID();
//	                gen.classAssertion(tweet, genACT_URL + "Tweet");
//
//	                for (String placeholder : twitterTemplate.getPlaceholders()) {
//	                    for (Map<String, Object> template : templates) {
//	                        String accountType = twitterTemplate.getAccountType();
//
//	                        List<Map<String, Object>> triples = (List<Map<String, Object>>) template.get(accountType);
//
//	                        if (triples != null) {
//	                            for (Map<String, Object> triple : triples) {
//	                                String subject = (String) triple.get("subject");
//	                                String predicate = (String) triple.get("predicate");
//	                                String object = (String) triple.get("object");
//	                                generateTriples(tweet, subject, predicate, object, currentTimeMillis);
//	                            }
//	                        }
//
//	                        triples = (List<Map<String, Object>>) template.get(placeholder);
//
//	                        if (triples != null) {
//	                            for (Map<String, Object> triple : triples) {
//	                                String subject = (String) triple.get("subject");
//	                                String predicate = (String) triple.get("predicate");
//	                                String object = (String) triple.get("object");
//	                                generateTriples(tweet, subject, predicate, object, currentTimeMillis);
//	                            }
//	                        }
//	                    }
//	                }
//	            } else {
//	                System.out.println("No templates found in the mapping file");
//	            }
//	        } else {
//	            System.out.println("Mapping data is null");
//	        }
//
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//
//	    currentTimeMillis = currentTimeMillis + (long) (this.random.nextDouble() * this.monthsInMillis);
//	    return currentTimeMillis;
//	}
//
//
//	public void generateTriples(String tweetid, String subject, String predicate, String object, long currentTimeMillis) {
//		
//		Model tweetModel = ModelFactory.createDefaultModel();
//		Property hasDateTimestamp = tweetModel.createProperty(genACT_URL + "hasDateTimestamp");
//		Property hasInformation = tweetModel.createProperty(genACT_URL + "hasInformation");
//		Resource Subject = tweetModel.createResource(genACT_URL +subject.substring(1));
//		Property Predicate = tweetModel.createProperty(genACT_URL +predicate);
//		Resource Object = tweetModel.createResource(genACT_URL +object.substring(1));
//
//		Literal Object2 = tweetModel.createTypedLiteral(genACT_URL +object);
//		// String randomCity = cityList.get(this.random.nextInt(cityList.size()));
//		/*
//		 * randomly picked a paper to map the last authors in the author list as the
//		 * keynote, trackchairs, organizers
//		 */
//		// for each conference, we can have a start time that's within a range. For the
//		// dataset we provide we chose 6 months, users
//		// can change it
//		Resource tweet = tweetModel.createResource(genACT_URL +tweetid);
//		// peopleDirectlyInvolvedList,otherPeopleInvolvedList,papersList,cityList
//		if (object.startsWith("?")) {
//			// for object property assertion
//			// first generate information_id
//			// every triple gets a different information_id
////			postinguser, speaker, 
////			tweet, information, conferenceinstance, otherusers, hashtag(TOKEN_Domain), 
////			username(peopleDirectlyInvolvedList),
////          affiliation, 
////			organization, conferenceName, conferenceid, conferenceMode, paperTrack,  location, paper, papertitle, 
////			paperdomain,speakerrole, talk, topic(TOKEN_Domain+"Topic"), organizer(TOKEN_ChairRole), 
////			organizerRole(TOKEN_ChairRole)
//			if (subject.contains("speaker")) {
//				Subject = tweetModel.createResource(genACT_URL +
//						gen.peopleDirectlyInvolvedList.get(this.random.nextInt(gen.peopleDirectlyInvolvedList.size())));
//			} else if(subject.contains("conferenceinstance"))
//			{
//				Subject = tweetModel.createResource(genACT_URL +this.confInstance);
//			}
//		
//			 else if(subject.contains("affiliation"))
//				{
//				 Subject = tweetModel.createResource(genACT_URL +
//							gen.researchGroups.get(this.random.nextInt(gen.researchGroups.size())));
//				}
//			 else if(subject.contains("conferenceName"))
//				{
//				 Subject = tweetModel.createResource(genACT_URL +this.confName);
//				}
//			 else if(subject.contains("conferenceid"))
//				{
//				 Subject = tweetModel.createResource(genACT_URL +this.confId);
//				}
//			 else if(subject.contains("conferenceMode"))
//				{
//				 Subject = tweetModel.createResource(genACT_URL +gen.TOKEN_EventMode[this.random.nextInt(gen.TOKEN_EventMode.length)]);
//				}
//			 else if(subject.contains("paperTrack"))
//				{
//				 Subject = tweetModel.createResource(genACT_URL +gen.TOKEN_ConferenceEventTrack[this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length)]); 
//				}
//			
//			
//			if (object.contains("speaker")) {
//				Object = tweetModel.createResource(genACT_URL +
//						gen.peopleDirectlyInvolvedList.get(this.random.nextInt(gen.peopleDirectlyInvolvedList.size())));
//			} else if(object.contains("conferenceinstance"))
//			{
//				Object = tweetModel.createResource(genACT_URL +this.confInstance);
//			}
//			
//			 else if(object.contains("affiliation"))
//				{
//				 Subject = tweetModel.createResource(genACT_URL +
//							gen.researchGroups.get(this.random.nextInt(gen.researchGroups.size())));
//				}
//			 else if(object.contains("conferenceName"))
//				{
//				 Object = tweetModel.createResource(genACT_URL +this.confName);
//				}
//			 else if(object.contains("conferenceid"))
//				{
//				 Object = tweetModel.createResource(genACT_URL +this.confId);
//				}
//			 else if(object.contains("conferenceMode"))
//				{
//				 Object = tweetModel.createResource(genACT_URL +gen.TOKEN_EventMode[this.random.nextInt(gen.TOKEN_EventMode.length)]);
//				}
//			 else if(object.contains("paperTrack"))
//				{
//				 Object = tweetModel.createResource(genACT_URL +gen.TOKEN_ConferenceEventTrack[this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length)]); 
//				}
//			Resource Information = tweetModel.createResource(genACT_URL + "information" + UUID.randomUUID());
//			tweetModel.add(tweetModel.createStatement(tweet, hasInformation, Information));
//			tweetModel.add(tweetModel.createStatement(Information, rdfSubject, Subject));
//			tweetModel.add(tweetModel.createStatement(Information, rdfPredicate, Predicate));
//			tweetModel.add(tweetModel.createStatement(Information, rdfObject, Object));
//			if (subject.contains("user") && subject.endsWith("s")) {
//				for (int i = 0; i < this.random.nextInt(5) + 3; i++) {
//
//					Subject = tweetModel.createResource(
//							peopleDirectlyInvolvedList.get(this.random.nextInt(peopleDirectlyInvolvedList.size())));
//					Predicate = tweetModel.createProperty(predicate);
//					Object = tweetModel.createResource(object.substring(1));
//					Information = tweetModel.createResource(genACT_URL + "information" + UUID.randomUUID());
//					tweetModel.add(tweetModel.createStatement(tweet, hasInformation, Information));
//					tweetModel.add(tweetModel.createStatement(Information, rdfSubject, Subject));
//					tweetModel.add(tweetModel.createStatement(Information, rdfPredicate, Predicate));
//					tweetModel.add(tweetModel.createStatement(Information, rdfObject, Object));
//				}
//			}
//			if (object.contains("user") && object.endsWith("s")) {
//				for (int i = 0; i < this.random.nextInt(5) + 3; i++) {
//					Subject = tweetModel.createResource(subject.substring(1));
//					Predicate = tweetModel.createProperty(predicate);
//					Object = tweetModel.createResource(
//							peopleDirectlyInvolvedList.get(this.random.nextInt(peopleDirectlyInvolvedList.size())));
//					Information = tweetModel.createResource(genACT_URL + "information" + UUID.randomUUID());
//					tweetModel.add(tweetModel.createStatement(tweet, hasInformation, Information));
//					tweetModel.add(tweetModel.createStatement(Information, rdfSubject, Subject));
//					tweetModel.add(tweetModel.createStatement(Information, rdfPredicate, Predicate));
//					tweetModel.add(tweetModel.createStatement(Information, rdfObject, Object));
//				}
//			}
//
//		} else if (object.startsWith("_")) {
//			
//			if (subject.contains("speaker")) {
//				Subject = tweetModel.createResource(
//						gen.peopleDirectlyInvolvedList.get(this.random.nextInt(gen.peopleDirectlyInvolvedList.size())));
//			} else if(subject.contains("conferenceinstance"))
//			{
//				Subject = tweetModel.createResource(this.confInstance);
//			}
//		
//			 else if(subject.contains("affiliation"))
//				{
//				 Subject = tweetModel.createResource(
//							gen.researchGroups.get(this.random.nextInt(gen.researchGroups.size())));
//				}
//			 else if(subject.contains("conferenceName"))
//				{
//				 Subject = tweetModel.createResource(this.confName);
//				}
//			 else if(subject.contains("conferenceid"))
//				{
//				 Subject = tweetModel.createResource(this.confId);
//				}
//			 else if(subject.contains("conferenceMode"))
//				{
//				 Subject = tweetModel.createResource(gen.TOKEN_EventMode[this.random.nextInt(gen.TOKEN_EventMode.length)]);
//				}
//			 else if(subject.contains("paperTrack"))
//				{
//				 Subject = tweetModel.createResource(gen.TOKEN_ConferenceEventTrack[this.random.nextInt(gen.TOKEN_ConferenceEventTrack.length)]); 
//				}
//			
//			// for data property assertion
//			// first generate information_id
////		/ every triple gets a different information_id
////			timestamp, hashtag, username, papertitle, paperdomain, location, url
//			 if(object.contains("hashtag"))
//				{
//				 Object2 =  tweetModel.createTypedLiteral(gen.TOKEN_Domain[this.random.nextInt(gen.TOKEN_Domain.length)]);
//				}
//			 if(object.contains("timestamp"))
//				{
//				 Object2 =  tweetModel.createTypedLiteral(object);
//				}
//			 if(object.contains("username"))
//				{
//				 Object2 =  tweetModel.createTypedLiteral(object);
//				}
//			 if(object.contains("papertitle"))
//				{
//				 Object2 =  tweetModel.createTypedLiteral(object);
//				}
//			 if(object.contains("paperdomain"))
//				{
//				 Object2 =  tweetModel.createTypedLiteral(object);
//				}
//			 if(object.contains("url"))
//				{
//				 Object2 =  tweetModel.createTypedLiteral(object);
//				}
////			Subject = tweetModel.createResource(subject);
////			Predicate = tweetModel.createProperty(predicate);
//			Resource Information = tweetModel.createResource(genACT_URL + "information" + UUID.randomUUID());
//			tweetModel.add(tweetModel.createStatement(tweet, hasInformation, Information));
//			tweetModel.add(tweetModel.createStatement(Information, rdfSubject, Subject));
//			tweetModel.add(tweetModel.createStatement(Information, rdfPredicate, Predicate));
//			tweetModel.add(tweetModel.createStatement(Information, rdfObject, Object2));
//		} else {
//			// class assertion
//			gen.classAssertion(subject, object);
//		}
//		
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
//		LocalDateTime currentDateTime = Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneOffset.UTC)
//				.toLocalDateTime();
//
//		String formattedDateTime = currentDateTime.format(formatter);
//		tweetModel.add(tweetModel.createStatement(tweet, hasDateTimestamp, formattedDateTime));
//		
//		this.tweetFile_rdf = new File(
//				gen.directoryPath + "/Streams/" + this.confInstance + "_" + formattedDateTime +UUID.randomUUID()+ "_tweet.ttl");
//		System.out.println(tweetFile_rdf);
////		if (!this.tweetFile_rdf.exists()) {
////			try {
////				// Create a new file
////				this.tweetFile_rdf.createNewFile();
////			} catch (IOException e) {
////				e.printStackTrace();
////			}
////		}
//		tweetModelWriter = tweetModel.getWriter("TTL");
//		try {
//			tweetModelWriter.write(tweetModel, new FileOutputStream(tweetFile_rdf), null);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	
//
//	private List<String> extractPlaceholders(String body) {
//		List<String> placeholders = new ArrayList<>();
//		// Regular expression to find text within square brackets
//		Pattern pattern = Pattern.compile("\\[(.*?)\\]");
//		Matcher matcher = pattern.matcher(body);
//
//		while (matcher.find()) {
//			// Add the text within square brackets as a placeholder
//			placeholders.add(matcher.group(1));
//		}
//
//		return placeholders;
//	}
//
//	public int getTweetFrequency(String category) {
//		// Define tweet frequencies for each category
//		switch (category) {
//		case "Conference Announcement":
//			return 1; // Once at the start
//		case "Call for Papers":
//			return 7; // Approximately 1 tweet per day for a week
//		case "Submission Reminder":
//			return 2; // Approximately 1 tweet every 2-3 weeks
//		case "Notification":
//			return 5; // High frequency for a few days after a long gap
//		case "Registration Reminder":
//			return 12; // Approximately 1 tweet every 2-3 weeks
//		case "Before Conference":
//			return 4; // Approximately 1 tweet per week for 2 weeks
//		case "During Conference":
//			return 20; // High frequency during conference days
//		case "After Conference":
//			return 5; // High frequency for a few days after the conference
//		default:
//			return 1; // check this
//		}
//	}
//
//	// Helper method to get a random element from a set
//	private static <T> T getRandomElement(Set<T> set, Random random) {
//		// Random random = new Random();
//		int index = random.nextInt(set.size());
//		int i = 0;
//		for (T element : set) {
//			if (i == index) {
//				return element;
//			}
//			i++;
//		}
//		throw new IllegalArgumentException("Set is empty or index is out of bounds");
//	}
//
//	public Map<String, Map<String, Object>> getRandomPapers(Map<String, Map<String, Object>> papersAccepted, int n) {
//		Map<String, Map<String, Object>> randomPapers = new HashMap<>();
//
//		List<String> paperIds = new ArrayList<>(papersAccepted.keySet());
//		int totalPapers = paperIds.size();
//
//		// Shuffle the paperIds list to randomize the selection
//		Collections.shuffle(paperIds);
//
//		// Select 'n' random papers and add them to the new map
//		for (int i = 0; i < n && i < totalPapers; i++) {
//			String paperId = paperIds.get(i);
//			randomPapers.put(paperId, papersAccepted.get(paperId));
//		}
//
//		return randomPapers;
//	}
//

}
