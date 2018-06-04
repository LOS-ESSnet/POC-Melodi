package fr.insee.melodi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class creates a Jena model corresponding to the POP5 data set, writes RDF triples in it and finally outputs the corresponding Turtle file.
 *  
 * @author Franck Cotton
 */
public class EsaneModelMaker {

	/** Jena model for the Esane data set */
	private static Model esaneModel = null;

	/** Logger Log4J */
	private static Logger logger = LogManager.getLogger(EsaneModelMaker.class);

	private static DateFormat yearFormat = new SimpleDateFormat("yyyy");

	public static Map<String, Resource> confidentialityStatusMappings = new HashMap<String, Resource>();
	static {
		// Mappings between the values in the input file (code_valeur) and the target code list (SDMX CL_CONF_STATUS)
		confidentialityStatusMappings.put("", ResourceFactory.createResource(Configuration.sdmxCodeURI("confStatus", "F")));
		confidentialityStatusMappings.put("SE", ResourceFactory.createResource(Configuration.sdmxCodeURI("confStatus", "C")));
		confidentialityStatusMappings.put("ND", ResourceFactory.createResource(Configuration.sdmxCodeURI("confStatus", "N")));
	}

	public static Map<String, Property> sdmxAttributes = new HashMap<String, Property>();
	static {
		sdmxAttributes.put("confStatus", ResourceFactory.createProperty("http://purl.org/linked-data/sdmx/2009/attribute#confStatus"));
		sdmxAttributes.put("unitMult", ResourceFactory.createProperty("http://purl.org/linked-data/sdmx/2009/attribute#unitMult"));
	}
	
	public static Map<String, Property> sdmxDimensions = new HashMap<String, Property>();
	static {
		sdmxDimensions.put("timePeriod", ResourceFactory.createProperty("http://purl.org/linked-data/sdmx/2009/dimension#timePeriod"));
		// TODO SDMX dimension freq is defined in the DSD but not used?
		sdmxDimensions.put("freq", ResourceFactory.createProperty("http://purl.org/linked-data/sdmx/2009/dimension#freq"));
	}

	public static Map<String, Property> inseeDimensions = new HashMap<String, Property>();
	static {
		inseeDimensions.put("nafRev2", ResourceFactory.createProperty("http://id.insee.fr/meta/dimension/nafRev2"));
		inseeDimensions.put("trancheEffectif", ResourceFactory.createProperty("http://id.insee.fr/meta/dimension/trancheEffectif"));		
	}

	public static void main(String... args) {
		makeDataSetModel();
		try {
			RDFDataMgr.write(new FileOutputStream("src/main/resources/turtle/esane-dataset.ttl"), esaneModel, Lang.TURTLE);
		} catch (FileNotFoundException e) {
			logger.error("Error writing Turtle file - " + e.getMessage());
		}
	}

	public static void makeDataSetModel() {

		// Read the DSD and create the list of measures
		OntModel dsdModel = makeDSDModel();
		Map<String, Property> inseeMeasures = new HashMap<String, Property>();
		ResIterator measureIterator = dsdModel.listResourcesWithProperty(RDF.type, ResourceFactory.createResource("http://purl.org/linked-data/cube#MeasureProperty"));
		while (measureIterator.hasNext()) {
			Property measureProperty = ResourceFactory.createProperty(measureIterator.next().getURI());
			inseeMeasures.put(measureProperty.getLocalName().toUpperCase(), measureProperty);
		}

		esaneModel = ModelFactory.createDefaultModel();
		esaneModel.setNsPrefix("xsd", XSD.NS);
		esaneModel.setNsPrefix("qb", DataCube.NS);
		esaneModel.setNsPrefix("sdmx-attribute", "http://purl.org/linked-data/sdmx/2009/attribute#");
		esaneModel.setNsPrefix("sdmx-code", "http://purl.org/linked-data/sdmx/2009/code#");
		esaneModel.setNsPrefix("sdmx-dimension", "http://purl.org/linked-data/sdmx/2009/dimension#");
		esaneModel.setNsPrefix("idata", "http://data.insee.fr/entreprises/esane/");

		Resource dataSetResource = esaneModel.createResource("http://data.insee.fr/entreprises/esane/2013", DataCube.DataSet);
		dataSetResource.addProperty(DataCube.structure, esaneModel.createResource("http://id.insee.fr/meta/dsd/esane/0.1")); // TODO Extract from DSD
		dataSetResource.addProperty(RDFS.label, esaneModel.createLiteral("Esane 2013", "fr"));
		dataSetResource.addProperty(sdmxAttributes.get("unitMult"), esaneModel.createTypedLiteral(3));

		logger.debug("About to start reading data file " + Configuration.ESANE_DATA_FILE);

		try {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter('#').withFirstRecordAsHeader().parse(new FileReader(Configuration.ESANE_DATA_FILE));
			for (CSVRecord record : records) {
				if (!selectRecord(record)) continue;

				// Create resource for observation and attach it to the data set
				String recordId = record.get("numenregistrement");
				String observationURI = dataSetResource.getURI() + "/observation/" + recordId;
				Resource observationResource = esaneModel.createResource(observationURI, DataCube.Observation);
				observationResource.addProperty(DataCube.dataSet, dataSetResource);
				// Add time period (should be constant and equal to 2013 for the ESANE data set)
				try {
					String yearValue = record.get("annee");
					yearFormat.parse(yearValue); // Make sure we have a valid year
					observationResource.addProperty(sdmxDimensions.get("timePeriod"), esaneModel.createTypedLiteral(yearValue, XSDDatatype.XSDdate));
				} catch (ParseException e) {
					logger.error("Unparseable year value: " + record.get("annee") + " for record " + recordId);
				}
				// Add the NAF rév.2 code corresponding to the economic activity dimension
				String nafCodeNoPoint = record.get("NAF");
				String nafItemURI = Configuration.nafr2ItemURI(nafCodeNoPoint);
				observationResource.addProperty(inseeDimensions.get("nafRev2"), esaneModel.createResource(nafItemURI));
				// Add confidentiality status as an attribute
				String codeValue = record.get("code_valeur");
				if (confidentialityStatusMappings.containsKey(codeValue)) {
					observationResource.addProperty(sdmxAttributes.get("confStatus"), confidentialityStatusMappings.get(codeValue));
				} else {
					logger.debug("Unexpected value for confidentatiality status: " + codeValue);
				}
				// Add measureType dimension and measure value
				String conceptMeasured = record.get("concept_mesure");
				if (!inseeMeasures.containsKey(conceptMeasured)) {
					logger.error("Unknown measure: " + conceptMeasured + " for record " + recordId);
				} else {
					float measureValue = 0;
					try {
						measureValue = Float.parseFloat(record.get("valeur"));
						Property measureResource = inseeMeasures.get(conceptMeasured);
						observationResource.addProperty(DataCube.measureType, measureResource);
						observationResource.addProperty(measureResource, esaneModel.createTypedLiteral(measureValue));
					} catch (Exception e) {
						logger.error("Invalid float value: " + record.get("valeur") + " for record " + recordId);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Error while reading data file - " + e.getMessage());
		}

		logger.info("End of program");
	}

	public static OntModel makeDSDModel() {

		// The DSD is split between different files: one for the base DSD and two for the measures
		OntModel esaneDSDModel = ModelFactory.createOntologyModel();
		logger.info("Reading the Esane DSD (without measures)");
		esaneDSDModel.read("src/main/resources/turtle/esane-dsd-base.ttl");
		logger.info("Reading the Esane DSD measure definitions");
		esaneDSDModel.read("src/main/resources/turtle/esane-dsd-measure-definitions.ttl");
		logger.info("Reading the Esane DSD measure specifications");
		esaneDSDModel.read("src/main/resources/turtle/esane-dsd-measure-specifications.ttl");

		// TODO Replace default concept in measures when a hand-chosen concept is indicated
		return esaneDSDModel;
	}

	private static boolean selectRecord(CSVRecord record) {

		// For now we ignore records that refer to NAF rév.2 groupings (prof_naf = "NO")
		if ("N0".equals(record.get("prof_naf"))) return false;
		// If the value is missing (valeur = "") we ignore the record
		if (record.get("valeur").length() == 0) return false;

		return true;
		
	}
}
