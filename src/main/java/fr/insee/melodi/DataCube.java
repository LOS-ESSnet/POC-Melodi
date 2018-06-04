package fr.insee.melodi;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
 
/**
 * Constants for the W3C Data Cube Vocabulary.
 *
 * @see <a href="https://www.w3.org/TR/vocab-data-cube/">Data Catalog Vocabulary</a>
 */
public class DataCube {

	private static final OntModel model = ModelFactory.createOntologyModel();

	public static final String NS = "http://purl.org/linked-data/cube#";
	public static final Resource NAMESPACE = model.createResource(NS);

	/**
	 * Returns the URI for this schema
	 * @return URI
	 */
	public static String getURI() {
		return NS;
	}

	// Classes
	public static final OntClass Attachable = model.createClass(NS + "Attachable");
	public static final OntClass AttributeProperty = model.createClass(NS + "AttributeProperty");
	public static final OntClass CodedProperty = model.createClass(NS + "CodedProperty");
	public static final OntClass ComponentProperty = model.createClass(NS + "ComponentProperty");
	public static final OntClass ComponentSet = model.createClass(NS + "ComponentSet");
	public static final OntClass ComponentSpecification = model.createClass(NS + "ComponentSpecification");
	public static final OntClass DataSet = model.createClass(NS + "DataSet");
	public static final OntClass DataStructureDefinition = model.createClass(NS + "DataStructureDefinition");
	public static final OntClass DimensionProperty = model.createClass(NS + "DimensionProperty");
	public static final OntClass HierarchicalCodeList = model.createClass(NS + "HierarchicalCodeList");
	public static final OntClass MeasureProperty = model.createClass(NS + "MeasureProperty");
	public static final OntClass Observation = model.createClass(NS + "Observation");
	public static final OntClass Slice = model.createClass(NS + "Slice");
	public static final OntClass ObservationGroup = model.createClass(NS + "ObservationGroup");
	public static final OntClass SliceKey = model.createClass(NS + "SliceKey");

	// Datatype properties
	public static final DatatypeProperty accessURL = model.createDatatypeProperty(NS + "order");
	public static final DatatypeProperty componentRequired = model.createDatatypeProperty(NS + "componentRequired");

	// Object properties
	public static final ObjectProperty attribute = model.createObjectProperty(NS + "attribute");
	public static final ObjectProperty codeList = model.createObjectProperty(NS + "codeList");
	public static final ObjectProperty component = model.createObjectProperty(NS + "component");
	public static final ObjectProperty componentAttachment = model.createObjectProperty(NS + "componentAttachment");
	public static final ObjectProperty componentProperty = model.createObjectProperty(NS + "componentProperty");
	public static final ObjectProperty concept = model.createObjectProperty(NS + "concept");
	public static final ObjectProperty dataSet = model.createObjectProperty(NS + "dataSet");
	public static final ObjectProperty dimension = model.createObjectProperty(NS + "dimension");
	public static final ObjectProperty hierarchyRoot = model.createObjectProperty(NS + "hierarchyRoot");
	public static final ObjectProperty measure = model.createObjectProperty(NS + "measure");
	public static final ObjectProperty measureDimension = model.createObjectProperty(NS + "measureDimension");
	public static final ObjectProperty measureType = model.createObjectProperty(NS + "measureType");
	public static final ObjectProperty observation = model.createObjectProperty(NS + "observation");
	public static final ObjectProperty observationGroup = model.createObjectProperty(NS + "observationGroup");
	public static final ObjectProperty parentChildProperty = model.createObjectProperty(NS + "parentChildProperty");
	public static final ObjectProperty slice = model.createObjectProperty(NS + "slice");
	public static final ObjectProperty sliceKey = model.createObjectProperty(NS + "sliceKey");
	public static final ObjectProperty sliceStructure = model.createObjectProperty(NS + "sliceStructure");
	public static final ObjectProperty structure = model.createObjectProperty(NS + "structure");
}