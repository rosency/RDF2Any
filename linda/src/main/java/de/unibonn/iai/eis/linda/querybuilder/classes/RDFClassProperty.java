package de.unibonn.iai.eis.linda.querybuilder.classes;

import com.hp.hpl.jena.query.ResultSet;


import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClassProperty {
	public String uri;

	public String type;
	public String label;
	public Integer count;
	public RDFClassPropertyRange range;
	public Boolean multiplePropertiesForSameNode; //This variable determines if there are multiple properties like this. Its important as this determines how to Normalize in RDF conversions
	
	public RDFClassProperty(String uri,  String type, String label){
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.multiplePropertiesForSameNode = false;
	}
	
	public RDFClassProperty(String uri,  String type, String label, RDFClassPropertyRange range){
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.range = range;
		this.multiplePropertiesForSameNode = false;
	}
	
	//This method generates the count of the property
	
	public void generateCountOfProperty(String classUri, String dataset){
		String countQuery = SPARQLHandler.getPrefixes();
		countQuery += " SELECT DISTINCT ?c  where {?c rdf:type <"+classUri+">. ?c <"+this.uri+"> ?d} ";
		ResultSet countResultSet = SPARQLHandler.executeQuery(dataset, countQuery);
		this.count = 0;
		while(countResultSet.hasNext()){
			countResultSet.next();
			this.count++;
		}
		this.multiplePropertiesForSameNode = hasMultiplePropertiesForSameNode(dataset, classUri);
		System.out.println("generated count for "+this.uri + " ("+this.count+"), has multiple properties for the same node .. "+this.multiplePropertiesForSameNode.toString());
		
	}
	
	public Boolean hasMultiplePropertiesForSameNode(String dataset, String classUri){
		Boolean result = false;
		String q = SPARQLHandler.getPrefixes();
		q += "SELECT DISTINCT ?c ?d ?e  where {?c rdf:type <"+classUri+">. ?c <"+this.uri+"> ?d. ?c <"+this.uri+"> ?e. FILTER(?e != ?d)}  LIMIT 1";
		ResultSet checkResultSet = SPARQLHandler.executeQuery(dataset, q);
		while(checkResultSet.hasNext()){
			checkResultSet.next();
			result = true;
		}
		return result;
	}
	
	public String toString(){
		return "uri : "+this.uri+", type : "+this.type+", label : "+this.label+", range : {"+this.range.toString()+"}, count : "+this.count.toString()+", has multiple properties for the same node : "+this.multiplePropertiesForSameNode.toString();
	}
}