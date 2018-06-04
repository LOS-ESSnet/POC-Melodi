package fr.insee.melodi;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class containing the configuration parameters for the creation of RDF data corresponding to the Esane dataset.
 * 
 * @author Franck Cotton
 */
public class Configuration {


	////////////////
	// File names //
	////////////////

	public static final String ESANE_DATA_FILE = "src/main/resources/data/esane-2013.txt";

	///////////////////////
	// URI and namepaces //
	///////////////////////

	public static final String META_BASE_URI = "http://rdf.insee.fr/meta/";

	/**
	 * Returns the URI of a code entry in an SDMX code list.
	 * 
	 * @param concept The name of the SDMX concept (e.g. confStatus)
	 * @param code The code notation ("C", "D", etc.)
	 * @return The URI of the code entry (e.g. http://purl.org/linked-data/sdmx/2009/code#confStatus-C).
	 */
	public static final String sdmxCodeURI(String concept, String code) {
		return "http://purl.org/linked-data/sdmx/2009/code#" + concept + "-" + code;
	}

	public static String nafr2ItemURI(String itemCode) {

		String baseURI = "http://id.insee.fr/codes/nafr2/";
		switch (itemCode.length()) {
			case 5: return baseURI + "sousClasse/" + itemCode.substring(0, 2) + "." + itemCode.substring(2);
			case 4: return baseURI + "classe/" + itemCode.substring(0, 2) + "." + itemCode.substring(2);
			case 3: return baseURI + "groupe/" + itemCode.substring(0, 2) + "." + itemCode.substring(2);
			case 2: return baseURI + "division/" + itemCode;
			case 1: return baseURI + "section/" + itemCode;
			default: return null;
		}
	}

	public static final Set<String> validEmploymentRangeCodes = new TreeSet<String>(Arrays.asList("009","119", "224", "250"));
	public static String employmentRangeURI(String rangeCode) {

		if (!validEmploymentRangeCodes.contains(rangeCode)) return null;
		return "http://id.insee.fr/codes/tranchesEffectifs/" + rangeCode;
	}
}
