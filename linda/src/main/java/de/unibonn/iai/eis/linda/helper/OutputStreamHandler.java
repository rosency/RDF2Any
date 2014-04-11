package de.unibonn.iai.eis.linda.helper;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.converters.Converter;

/**
 * @author gsingharoy
 *
 *This class handles output Stream of a SPARQL query
 *
 **/
public class OutputStreamHandler {
	public static StreamingOutput getConverterStreamingOutput(final Converter converter, final String queryString){
		return new StreamingOutput(){

			public void write(OutputStream output) throws IOException,
			WebApplicationException {
				try{
					
					ResultSet results = SPARQLHandler.executeDBPediaQuery(queryString);
					converter.convert(output, results);
				}catch(Exception e){
					throw new WebApplicationException(e);
				}

			}

		};
	}
}