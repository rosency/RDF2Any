package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.converters.impl.configuration.BodyChunk;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

public class ConfiguredConverter extends MainConverter implements Converter {

	public String header;
	public String body;
	public String footer;
	private Map<String, String> variableDictionary;
	private Map<String, String> intermediateVariableDictionary;
	private Map<String, String> intermediateVariableValue;
	private List<BodyChunk> bodyChunks;

	public ConfiguredConverter(String variableDictionary, String header,
			String body, String footer) {
		this.header = header;
		this.body = body;
		this.footer = footer;
		this.variableDictionary = new HashMap<String, String>();
		this.intermediateVariableDictionary = new HashMap<String, String>();
		this.intermediateVariableValue = new HashMap<String, String>();
		if (variableDictionary != null
				&& !variableDictionary.equalsIgnoreCase("")) {
			for (String item : variableDictionary.split(",")) {
				this.variableDictionary.put(item.split(":")[0],
						item.split(":")[1]);
			}
		}
		this.bodyChunks = BodyChunk.getBodyChunksFromString(this.body);
	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
			RDFClass forClass) throws IOException {
		// printing the header to the file
		output.write((this.header + "\n").getBytes(Charset.forName("UTF-8")));
		//printing the body to the file
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			RDFNode object = row.get("concept");
			if (object != null) {
				RDFNode objectName = row.get("label");
				RDFObject rdfObject = null;
				if (objectName != null) {
					rdfObject = new RDFObject(forClass, object.toString(),
							SPARQLHandler.getLabelName(objectName));
				} else {
					rdfObject = new RDFObject(forClass, object.toString());
				}
				rdfObject.generateProperties();
				for (BodyChunk c : this.bodyChunks) {
					writeBodyChunk(output, c, rdfObject);
				}
			}
		}
		// printing the footer to the file
		output.write((this.footer).getBytes(Charset.forName("UTF-8")));

	}

	private void writeBodyChunk(OutputStream output, BodyChunk bodyChunk, RDFObject rdfObject)
			throws IOException {
		if (bodyChunk.type.equals("text")) {
			output.write(bodyChunk.value.getBytes(Charset.forName("UTF-8")));
		} else if (bodyChunk.type.equals("variable")) {
			String outputString = "";
			if(bodyChunk.equals("NAME"))
				outputString = rdfObject.name;
			else if(bodyChunk.equals("URI"))
				outputString = rdfObject.uri;
			else if (variableDictionary.containsKey(bodyChunk.type)) {
				//
			}
			output.write(outputString.getBytes(Charset.forName("UTF-8")));
		}
	}

}