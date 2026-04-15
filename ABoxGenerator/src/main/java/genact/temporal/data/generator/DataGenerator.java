package genact.temporal.data.generator;

import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import com.opencsv.CSVReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Date;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import java.io.BufferedReader;
import java.util.*;

/**
 * DataGenerator is responsible for generating synthetic data for conferences
 * and related entities. It reads configuration from properties files, uses CSV
 * files for templates and mappings, and produces RDF data in N3 format.
 */
public class DataGenerator {

	// Configuration parameters
	public int acceptedPaperCount_min;
	public int acceptedPaperCount_max;
	public int acceptedPaperCount;
	public int peopleDirectlyInvolved_min;
	public int peopleDirectlyInvolved_max;
	public int peopleDirectlyInvolved;
	public int otherPeopleInvolved_min;
	public int otherPeopleInvolved_max;
	public int otherPeopleInvolved;
	public int usersInvolved_min;
	public int usersInvolved_max;
	public int usersInvolved;
	public int organizationCount_min;
	public int acadOrganizationCount_min;
	public int acadOrganizationCount_max;
	public int acadOrganizationCount;
	public int nonAcadOrganizationCount_min;
	public int nonAcadOrganizationCount_max;
	public int nonAcadOrganizationCount;
	public int researchGroupCount_min;
	public int researchGroupCount_max;
	public int researchGroupCount;
	public int collegeCount_min;
	public int collegeCount_max;
	public int collegeCount;
	public int conferenceDuration_min_months;
	public int conferenceDuration_max_months;
	public int conferenceDuration_months;
	public int nextConferenceCycleStartsIn_min;
	public int nextConferenceCycleStartsIn_max;
	public int nextConferenceCycleStartsIn;
	public int cityCount_min;
	public int cityCount_max;
	public int cityCount;
	public int early_announcement_peak_min;
	public int early_announcement_peak_max;
	public int notification_peak_min;
	public int notification_peak_max;
	public int during_conference_peak_min;
	public int during_conference_peak_max;
	public int after_conference_peak_min;
	public int after_conference_peak_max;
	public int during_conference_days_max;
	public int during_conference_days_min;
	public int after_conference_days_max;
	public int after_conference_days_min;
	public int random_tweets_min;
	public int random_tweets_max;
	public List<String> researchGroups = new ArrayList<>();
	public List<String> cities = new ArrayList<>();
	public Map<String, Map<String, Object>> papers;
	public File streamsDirectory;
	// Partition partition;
	public Map<String, Map<String, String>> userData;
	public Map<String, Map<String, Object>> paperData;
	// these are the instances that have been defined in the ontologies
	public String[] TOKEN_ConferenceEventTrack = new String[] { "applicationsTrack", "demoTrack", "doctoralConsortiumTrack",
			"posterTrack", "researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack" };
	public String[] TOKEN_EventMode = new String[] { "online", "offline", "hybrid" };
	public String[] TOKEN_ChairRole = new String[] { "generalChair", "localChair", "researchTrackChair", "resourcesTrackChair",
			"trackChair", "tutorialTrackChair", "workshopTrackChair" };
	public String[] TOKEN_Domain = new String[] { "ai", "ml", "nlp", "aiForSocialGood", "artificialIntelligence", "bigData",
			"blockchain", "cloudComputing", "computerVision", "dataScience", "deepLearning", "internetOfThings",
			"knowledgeGraph", "cardiology", "neurology", "oncology", "pediatrics", "linkedData", "machineLearning",
			"ontology", "naturalLanguageProcessing", "bioTech", "quantumComputing", "semanticWeb" };
	public String[] TOKEN_EventPhases = new String[] {};

	public int confNum; // user specifies the number of conferences required such as if user wants 4:
					// ESWC, ISWC, AAAI, WWW.
	public int confCycle; // user specifies the number of conference cycles needed such as ESWC21. ESWC22,
					// ESWC23...
	public File staticDirectory;
	public String profile;
	public String confInstance;
	public String directoryPath;
	public ConferenceStreams[] conferences;
	public Random random = new Random();
	public HashMap<Integer, String> map1 = new HashMap<>();
	public HashMap<Integer, String> map2 = new HashMap<>();
	public HashMap<Integer, String> map3 = new HashMap<>();
	public LocalDateTime dateTime;
	public long startTimestampMillis;
	public List<String> usersList;

	// Lists used by ConferenceStreams2 and ConferenceData
	public List<String> papersList = new ArrayList<>();
	public List<String> cityList = new ArrayList<>();
	public List<String> otherPeopleInvolvedList = new ArrayList<>();
	public List<String> peopleDirectlyInvolvedList = new ArrayList<>();
	public List<String> colleges = new ArrayList<>();
	public List<String> academicOrganizations = new ArrayList<>();
	public List<String> nonAcademicOrganizations = new ArrayList<>();

	public DataGenerator() {
	}

	public void classAssertion(String concept, String instance) {
		// Stub: class assertion for static RDF data
	}

	public void objectPropertyAssertion(String subject, String predicate, String object) {
		// Stub: object property assertion for static RDF data
	}

	public void dataPropertyAssertion(String subject, String predicate, String object) {
		// Stub: data property assertion for static RDF data
	}

	public static void main(String[] args) throws IOException {
		int confNum = 2;
		int seed = 1;
		int confCycle = 2;

		String currentDirectory = System.getProperty("user.dir");
		File currentDirFile = new File(currentDirectory);
		String directoryPath = currentDirFile.getParent();
		if (args.length == 4) {
			confNum = Integer.parseInt(args[0]);
			confCycle = Integer.parseInt(args[1]);
			directoryPath = args[2];
			seed = Integer.parseInt(args[3]);
		}
		if (args.length == 3) {
			confNum = Integer.parseInt(args[0]);
			confCycle = Integer.parseInt(args[1]);
			directoryPath = args[2];
		}

		else if (args.length == 2) {
			confNum = Integer.parseInt(args[0]);
			confCycle = Integer.parseInt(args[1]);
		} else {
			System.out.println(
					"Please give arguments in the following order: No. of conferences (int)*Mandatory, No. of conference Cycles (int)*Mandatory , DirectoryPath (optional), Seed (optional) ");
			System.out.println("For example: 2 5 C:/GitHub/GenACT 100");
		}

		File confDirectory = new File(directoryPath + "/Mappings/");
		if (!confDirectory.exists()) {
			System.out.println(directoryPath + "/Mappings/ directory is missing.");
		}
		// Universal Time (UTC).
		new DataGenerator().start(confNum, confCycle, directoryPath, seed);
	}

	// data generator starts with selecting the name of the authors, paper,
	// conferences, organizations, locations randomly
	// and storing them to a data structure and utilize them for generating the
	// instances in the later part of the code
	// tracks, authorids and affiliations are assigned randomly
	// currently we have data for 27 conferences starting from the year 2000 to 2022
	public void start(int confNum, int confCycle, String directoryPath, int seed) throws IOException {

		this.directoryPath = directoryPath;
		this.confNum = confNum;
		this.confCycle = confCycle;
		this.random.setSeed((long) seed);
		this.dateTime = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0);
		this.startTimestampMillis = dateTime.atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli();
		Properties prop1 = new Properties();
		InputStream input1 = null;
		Properties prop2 = new Properties();
		InputStream input2 = null;
		try {
			// load range of different parameters from properties files
			// property file to decide the entity counts
			input1 = new FileInputStream(directoryPath + "/ABoxGenerator/entitiesCount.properties");
			prop1.load(input1);
			// property file for the peaks
			input2 = new FileInputStream(directoryPath + "/ABoxGenerator/tweetsCount.properties");
			prop2.load(input2);
			// this.requiredABoxFormat=prop.getProperty("requiredABoxFormat");

			this.acceptedPaperCount_min = Integer.parseInt(prop1.getProperty("acceptedPaperCount_min"));
			this.acceptedPaperCount_max = Integer.parseInt(prop1.getProperty("acceptedPaperCount_max"));

			this.usersInvolved_min = Integer.parseInt(prop1.getProperty("usersInvolved_min"));
			this.usersInvolved_max = Integer.parseInt(prop1.getProperty("usersInvolved_max"));
			this.usersInvolved = this.random.nextInt(usersInvolved_max - usersInvolved_min + 1) + usersInvolved_min;
			this.peopleDirectlyInvolved_min = Integer.parseInt(prop1.getProperty("peopleDirectlyInvolved_min"));
			this.peopleDirectlyInvolved_max = Integer.parseInt(prop1.getProperty("peopleDirectlyInvolved_max"));
			this.otherPeopleInvolved_min = Integer.parseInt(prop1.getProperty("otherPeopleInvolved_min"));
			this.otherPeopleInvolved_max = Integer.parseInt(prop1.getProperty("otherPeopleInvolved_max"));
			this.researchGroupCount_min = Integer.parseInt(prop1.getProperty("researchGroupCount_min"));
			this.researchGroupCount_max = Integer.parseInt(prop1.getProperty("researchGroupCount_max"));
			this.nonAcadOrganizationCount_min = Integer.parseInt(prop1.getProperty("nonAcadOrganizationCount_min"));
			this.nonAcadOrganizationCount_max = Integer.parseInt(prop1.getProperty("nonAcadOrganizationCount_min"));
			this.conferenceDuration_min_months = Integer.parseInt(prop1.getProperty("conferenceDuration_min_months"));
			this.conferenceDuration_max_months = Integer.parseInt(prop1.getProperty("conferenceDuration_max_months"));
			this.cityCount_min = Integer.parseInt(prop1.getProperty("cityCount_min"));
			this.cityCount_max = Integer.parseInt(prop1.getProperty("cityCount_max"));
			this.early_announcement_peak_min = Integer.parseInt(prop2.getProperty("early_announcement_peak_min"));
			this.early_announcement_peak_max = Integer.parseInt(prop2.getProperty("early_announcement_peak_max"));
			this.notification_peak_min = Integer.parseInt(prop2.getProperty("notification_peak_min"));
			this.notification_peak_max = Integer.parseInt(prop2.getProperty("notification_peak_max"));
			this.during_conference_peak_min = Integer.parseInt(prop2.getProperty("during_conference_peak_min"));
			this.during_conference_peak_max = Integer.parseInt(prop2.getProperty("during_conference_peak_max"));
			this.after_conference_peak_min = Integer.parseInt(prop2.getProperty("after_conference_peak_min"));
			this.after_conference_peak_max = Integer.parseInt(prop2.getProperty("after_conference_peak_max"));
			this.during_conference_days_max = Integer.parseInt(prop2.getProperty("during_conference_days_max"));
			this.during_conference_days_min = Integer.parseInt(prop2.getProperty("during_conference_days_min"));
			this.after_conference_days_min = Integer.parseInt(prop2.getProperty("after_conference_days_min"));
			this.after_conference_days_max = Integer.parseInt(prop2.getProperty("after_conference_days_max"));
			this.random_tweets_min = Integer.parseInt(prop2.getProperty("random_tweets_min"));
			this.random_tweets_max = Integer.parseInt(prop2.getProperty("random_tweets_max"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input1 != null) {
				try {
					input1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (input2 != null) {
				try {
					input2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		int totalUserCount = this.usersInvolved_max * this.confNum * this.confCycle * 10;
		int totalPaperCount = this.acceptedPaperCount_max * this.confNum * this.confCycle * 10;
		// Load display names from authors.csv
		List<String> displayNames = loadDisplayNames(this.directoryPath + "/CSVFiles/authors.csv");
		List<String> paperTitles = loadDisplayNames(this.directoryPath + "/CSVFiles/papers.csv");
		this.researchGroups = readResearchGroupsFromCSV(this.directoryPath + "/CSVFiles/research_groups.csv");
		// Generate user data
		this.userData = generateUserData(totalUserCount, displayNames, this.researchGroups);
		this.paperData = generatePaperData(totalPaperCount, paperTitles, userData);
		this.cities = readCitiesFromCSV(this.directoryPath + "/CSVFiles/location.csv"); // from location.csv

//	        for (Map.Entry<String, Map<String, String>> entry : this.userData.entrySet()) {
//	            System.out.println("UserID: " + entry.getKey() + ", Data: " + entry.getValue());
//	        }

		this.generate(seed);
	}

	public Map<String, Map<String, Object>> generatePaperData(int totalPaperCount, List<String> paperTitles,
			Map<String, Map<String, String>> userData) {
		Map<String, Map<String, Object>> paperData = new HashMap<>();
		String[] TOKEN_Domain = new String[] { "ai", "ml", "nlp", "aiForSocialGood", "artificialIntelligence",
				"bigData", "blockchain", "cloudComputing", "computerVision", "dataScience", "deepLearning",
				"internetOfThings", "knowledgeGraph", "cardiology", "neurology", "oncology", "pediatrics", "linkedData",
				"machineLearning", "ontology", "naturalLanguageProcessing", "bioTech", "quantumComputing",
				"semanticWeb" };

		String[] conferenceTracks = { "applicationsTrack", "demoTrack", "doctoralConsortiumTrack", "posterTrack",
				"researchTrack", "resourcesTrack", "tutorialTrack", "workshopTrack" };

		List<String> students = new ArrayList<>();
		List<String> professors = new ArrayList<>();
		List<String> otherUsers = new ArrayList<>();

		for (Map.Entry<String, Map<String, String>> entry : userData.entrySet()) {
			String key = entry.getKey();
			String designation = entry.getValue().get("designation");

			if ("Student".equals(designation) || "PhDStudent".equals(designation)) {
				students.add(key);
			} else if ("Professor".equals(designation)) {
				professors.add(key);
			} else {
				otherUsers.add(key);
			}
		}

		for (int i = 0; i < totalPaperCount; i++) {
			String paperId = "paper" + (i + 1);
			String paperTitle = paperTitles.get(this.random.nextInt(paperTitles.size()));
			String conferenceTrack = conferenceTracks[this.random.nextInt(conferenceTracks.length)];

			List<String> authorList = new ArrayList<>();
			authorList.add(students.get(this.random.nextInt(students.size()))); // First author as a student

			int authorCount = 1 + this.random.nextInt(7); // Total authors between 1 and 8
			for (int j = 1; j < authorCount - 1; j++) {
				String author = otherUsers.get(this.random.nextInt(otherUsers.size()));
				if (!authorList.contains(author)) {
					authorList.add(author);
				}
			}

			authorList.add(professors.get(this.random.nextInt(professors.size()))); // Last author as a professor

			List<String> paperDomains = new ArrayList<>();
			int domainCount = 1 + this.random.nextInt(3); // Total domains between 1 and 3
			for (int k = 0; k < domainCount; k++) {
				String domain = TOKEN_Domain[this.random.nextInt(TOKEN_Domain.length)];
				if (!paperDomains.contains(domain)) {
					paperDomains.add(domain);
				}
			}

			Map<String, Object> paperMetaData = new HashMap<>();
			paperMetaData.put("PaperTitle", paperTitle);
			paperMetaData.put("ConferenceTrack", conferenceTrack);
			paperMetaData.put("AuthorList", authorList);
			paperMetaData.put("PaperDomains", paperDomains);

			paperData.put(paperId, paperMetaData);
		}

		return paperData;
	}

	public List<String> loadDisplayNames(String filePath) throws IOException {
		List<String> displayNames = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = br.readLine()) != null) {
			displayNames.add(line.split(",")[0]); // Assuming display name is in the first column
		}
		br.close();
		return displayNames;
	}

	public List<String> readPaperTitles(String filePath) throws IOException {
		List<String> paperTitles = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				// Assuming the paper title is in the first column of the CSV
				String[] values = line.split(",");
				if (values.length > 0) {
					paperTitles.add(values[0]);
				}
			}
		}
		return paperTitles;
	}

	public Map<String, Map<String, String>> generateUserData(int totalUserCount, List<String> displayNames,
			List<String> affiliations) {
		Map<String, Map<String, String>> userData = new HashMap<>();

		List<String> designations = Arrays.asList("Student", "PhDStudent", "Professor", "Researcher", "Faculty");

		int studentCount = (int) (totalUserCount * 0.7);
		int phdStudentCount = Math.min(20, (int) (totalUserCount * 0.1));
		int remainingCount = totalUserCount - studentCount - phdStudentCount;

		Map<String, Integer> designationCounts = new HashMap<>();
		designationCounts.put("Student", studentCount);
		designationCounts.put("PhDStudent", phdStudentCount);
		designationCounts.put("Professor", remainingCount / 3);
		designationCounts.put("Researcher", remainingCount / 3);
		designationCounts.put("Faculty", remainingCount - (2 * (remainingCount / 3)));

		for (int i = 0; i < totalUserCount; i++) {
			String userId = "user" + (i + 1);
			String userName = userId;
			String displayName = displayNames.get(this.random.nextInt(displayNames.size()));
			String affiliation = affiliations.get(this.random.nextInt(affiliations.size()));

			String designation = null;
			while (designation == null) {
				String potentialDesignation = designations.get(this.random.nextInt(designations.size()));

				if (designationCounts.get(potentialDesignation) > 0) {
					designation = potentialDesignation;
					designationCounts.put(potentialDesignation, designationCounts.get(potentialDesignation) - 1);
				}
			}

			Map<String, String> userMetaData = new HashMap<>();
			userMetaData.put("userName", userName);
			userMetaData.put("displayName", displayName);
			userMetaData.put("affiliation", affiliation);
			userMetaData.put("designation", designation);

			userData.put(userId, userMetaData);
		}

		return userData;
	}

	private void generate(int seed) {
		// code for creating organization mappings

		// use owlapi to find researchgroupds from the organization.owl file
		// Generate random names for research groups, colleges, academic organizations,
		// and non-academic organizations
		this.streamsDirectory = new File(this.directoryPath + "/EventData/");
		if (this.streamsDirectory.exists()) {
			deleteDirectory(this.streamsDirectory);
		}
		this.streamsDirectory.mkdirs();

		this.conferences = new ConferenceStreams[this.confNum];
		for (int i = 0; i < this.confNum; ++i) {
			// System.out.println(this.startTimestampMillis);
			// System.out.println("Started Conference Instance " + i);
			conferences[i] = new ConferenceStreams(DataGenerator.this, i);
		}

	}

	public static void deleteDirectory(File directory) {
		if (directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					deleteDirectory(file);
				}
			}
		}
		directory.delete();
	}

	private static List<String> readResearchGroupsFromCSV(String filename) {
		List<String> researchGroups = new ArrayList<>();
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			while ((line = br.readLine()) != null) {
				// Assuming each line contains a research group
				researchGroups.add(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return researchGroups;
	}

	private List<String> readCitiesFromCSV(String csvFilePath) {
		List<String> cities = new ArrayList<>();
		try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
			String[] line;
			csvReader.readNext(); // Skip header row
			while ((line = csvReader.readNext()) != null) {
				if (line.length > 0) {
					cities.add(line[0]);
				}
			}
		} catch (IOException | CsvValidationException e) {
			e.printStackTrace();
		}
		return cities;
	}

}
