package de.unibonn.iai.eis.linda.querybuilder.classes;

import com.owlike.genson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.CommonHelper;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClassProperty {
	public String uri;

	public String type;
	public String label;
	public Integer count;
	public RDFClassPropertyRange range;
	public Boolean multiplePropertiesForSameNode; // This variable determines if
													// there are multiple
													// properties like this. Its
													// important as this
													// determines how to
													// Normalize in RDF
													// conversions

	public RDFClassProperty(String uri, String type, String label) {
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.multiplePropertiesForSameNode = false;
	}

	public RDFClassProperty(String uri, String type, String label,
			RDFClassPropertyRange range) {
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.range = range;
		this.multiplePropertiesForSameNode = false;
	}

	public RDFClassProperty(String uri, String type, String label,
			Integer count, Boolean multiplePropertiesForSameNode,
			RDFClassPropertyRange range) {
		this.uri = uri;
		this.count = count;
		this.type = type;
		this.label = label;
		this.range = range;
		this.multiplePropertiesForSameNode = multiplePropertiesForSameNode;
	}

	public void handleStatisticalInformation(String dataset, String classUri) {
		generateCountOfProperty(classUri, dataset);
		generateRange(dataset);
	}

	// This method generates the count of the property
	public void generateCountOfProperty(String classUri, String dataset) {
		String countQuery = SPARQLHandler.getPrefixes();
		countQuery += " SELECT  (count(DISTINCT ?d) AS ?totalcount)  where {?c rdf:type <"
				+ classUri + ">. ?c <" + this.uri + "> ?d} ";
		ResultSet countResultSet = SPARQLHandler.executeQuery(dataset,
				countQuery);
		this.count = 0;
		if (countResultSet.hasNext()) {
			QuerySolution row = countResultSet.next();
			this.count = SPARQLHandler.getIntegerValueOfLiteral(row
					.get("totalcount"));
		}
		this.multiplePropertiesForSameNode = hasMultiplePropertiesForSameNode(
				dataset, classUri);
		System.out.println("generated count for " + this.uri + " ("
				+ this.count
				+ "), has multiple properties for the same node .. "
				+ this.multiplePropertiesForSameNode.toString());

	}

	// This method finds out if the property/predicate has multiple objects for
	// the same subject
	public Boolean hasMultiplePropertiesForSameNode(String dataset,
			String classUri) {
		Boolean result = false;
		String q = SPARQLHandler.getPrefixes();
		q += "SELECT DISTINCT ?c ?d ?e  where {?c rdf:type <" + classUri
				+ ">. ?c <" + this.uri + "> ?d. ?c <" + this.uri
				+ "> ?e. FILTER(?e != ?d)}  LIMIT 1";
		ResultSet checkResultSet = SPARQLHandler.executeQuery(dataset, q);
		while (checkResultSet.hasNext()) {
			checkResultSet.next();
			result = true;
		}
		return result;
	}

	// this method generates the range for the property
	public void generateRange(String dataset) {
		ResultSet rangeResultSet = SPARQLHandler.executeQuery(dataset,
				getRangeSPARQLQuery());
		if (rangeResultSet.hasNext()) {
			QuerySolution row = rangeResultSet.next();
			RDFNode rangeUri = row.get("range");
			this.range = new RDFClassPropertyRange(rangeUri.toString());
			this.range.generateRangeLabel(dataset);
			// this section specifies the type of the property based on its
			// range
			if (SPARQLHandler.isDataTypeUri(this.range.uri)) {
				this.type = "data";
			} else {
				this.type = "object";
			}
		}
	}
	@JsonIgnore
	public String getPropertyUnderscoreVariableName(){
		String restOfTheUri = this.uri.replace(SPARQLHandler.getBaseUrl(this.uri),"");
		return CommonHelper.getUnderscoreStringFromCamelCase(restOfTheUri.substring(1,restOfTheUri.length()));
	}
	
	@JsonIgnore
	private String getRangeSPARQLQuery() {
		String query = SPARQLHandler.getPrefixes();
		query += "select distinct ?range where {<" + this.uri
				+ "> rdfs:range ?range}";
		return query;
	}
	
	@JsonIgnore
	public Boolean hasValidRange() {
		Boolean result = true;
		if (this.type.equals("object")) {
			if (this.range == null)
				result = false;
			else if (this.range.label == null || this.range.label.equals(""))
				result = false;
		}
		return result;
	}

	/*
	 * START RDB methods
	 */
	@JsonIgnore
	public String getTableAttributeType() {
		if (this.type.equalsIgnoreCase("object"))
			return "int";
		else {
			if (this.range.label.equalsIgnoreCase("integer") || this.range.label.equalsIgnoreCase("nonnegativeinteger"))
				return "int";
			else
				return "text";
		}
	}
	@JsonIgnore
	public String getTableAttributeName() {
		if (this.type.equalsIgnoreCase("object"))
			return CommonHelper.getVariableName(this.label, "") + "_id";
		else
			return CommonHelper.getVariableName(this.label, "");
	}

	@JsonIgnore
	public String getRangeTableName(){
		String result = "";
		if(this.type.equals("object")){
			result = CommonHelper.getVariableName(range.label, "thing")+"s";
		}
		return result;
	}
	@JsonIgnore
	public String getTableName(RDFClass rdfClass) {
		return rdfClass.getVariableName()
				+ CommonHelper.getVariableName(this.label, "thing", false)
				+ "s";
	}

	/*
	 * END RDB methods
	 */
	
	
	/*
	 * START CSV methods
	 * 
	 * */
	
	//this method returns the header attribute name
	@JsonIgnore
	public String getCSVHeaderAttributeName() {
		return this.label.trim().toLowerCase().replace(" ", "_");
	}
	/*
	 * END CSV methods
	 * 
	 * */
	
	
	@JsonIgnore
	public String toString() {
		return "uri : " + this.uri + ", type : " + this.type + ", label : "
				+ this.label + ", range : {" + this.range.toString()
				+ "}, count : " + this.count.toString()
				+ ", has multiple properties for the same node : "
				+ this.multiplePropertiesForSameNode.toString();
	}

}
