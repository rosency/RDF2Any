package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.helper.CSVHelper;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

/**
 * @author gsingharoy
 * 
 *         This converts ResultSet to CSV
 **/

public class CSVConverter extends MainConverter implements Converter {
	private String generateFileHeader() {
		String result = "";
		for (int i = 0; i < resultVars.size(); i++) {
			if(i>0)
				result += ",";
			result += resultVars.get(i);
		}
		result += "\n";
		return result;
	}

	private String generateFileHeader(RDFClass forClass) {
		String result = "uri,name";
		for (RDFClassProperty property : forClass.properties) {
			result += ",";
			result += property.getCSVHeaderAttributeName();
		}
		result += "\n";
		return result;
	}

	private String generateFileResultRow(QuerySolution row) throws Exception {
		String result = "";
		for (int i = 0; i < resultVars.size(); i++) {
			if (i > 0)
				result += ",";
			result += CSVHelper.getCSVReadyEntry(row.get(resultVars.get(i))
					.toString());
		}
		result += "\n";
		return result;
	}

	public void convert(OutputStream outputStream, ResultSet rdfResults)
			throws IOException {
		generateResultVars(rdfResults);
		outputStream.write(generateFileHeader().getBytes(
				Charset.forName("UTF-8")));
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			try {
				outputStream.write(generateFileResultRow(row).getBytes(
						Charset.forName("UTF-8")));
			} catch (Exception e) {
				System.out.println("Error : " + e.toString());
			}

		}

	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
			RDFClass forClass) throws IOException {
		output.write(generateFileHeader(forClass).getBytes(
				Charset.forName("UTF-8")));
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			try {
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
					String strRow = rdfObject.uri+","+rdfObject.name;
					for (RDFClassProperty property : forClass.properties) {
						strRow += ",";
						strRow += CSVHelper.getCSVReadyEntry(rdfObject
								.getCollectedPropertyValue(property.uri, ""));
					}
					output.write((strRow + "\n").getBytes(Charset
							.forName("UTF-8")));
				}
			} catch (Exception e) {
				System.out.println("Error : " + e.toString());
			}

		}

	}

}
