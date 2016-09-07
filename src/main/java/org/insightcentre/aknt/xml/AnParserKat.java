/*
 * Copyright 2014 Ashok Hariharan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.insightcentre.aknt.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.akomantoso.api.AnDocType;
import org.akomantoso.api.AnTypeGetterHelper;
import org.akomantoso.api.AnValidator;
import org.akomantoso.api.AnValidatorError;
import org.akomantoso.api.AnVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Encapsulates a parser for Akoma Ntoso documents.
 * You need to indicate the schema version, and it unmarshals the document for 
 * that schema and discovers the docType and returns a object handle to it.
 * 
 * See Issue 4 : https://github.com/kohsah/akomantoso-lib/issues/4 for the 
 * rationale for this.
 * 
 * @author Ashok Hariharan
 */
public class AnParserKat {

	private static Logger logger = LoggerFactory.getLogger(AnParserKat.class);

	public AnParserKat(){}

	/**
	 * Unmarshalls an Akoma ntoso document - and returns an object of the identified type 
	 * in the document. If the document type cannot be idenitifed, returns null. 
	 * @param version an AnVersion object identifying the version of the schema to be used
	 * @param iStream the Akoma Ntoso xml document as an InputStream
	 * @return AnDocType - this can be OpenStructure, HierarchialStructure etc.This returns the object
	 * returned by getAct() , getBill() etc. whichever may be the appropriate doc type. Encapsulated with other
	 * information related to the object
	 * @throws JAXBException
	 * @throws SAXException 
	 */
	public static AnDocType parse(AnVersion version, InputStream iStream) throws SAXException, JAXBException{
		AnValidatorError vErr = AnValidator.validate(version, iStream);
		if (vErr != null){
			JAXBElement anType = unmarshal(version, iStream);
			return parse(version, anType);
		} else {
			logger.error("Error while validating XML document (InputStream) ", vErr);
		}
		return null;
	}

	/**
	 * Unmarshalls an Akoma ntoso document - and returns an object of the identified type 
	 * in the document. If the document type cannot be idenitifed, returns null. 
	 * @param version an AnVersion object identifying the version of the schema to be used
	 * @param iStream the Akoma Ntoso xml document as a Reader object (e.g. FileReader)
	 * @return AnDocType - this can be OpenStructure, HierarchialStructure etc.This returns the object
	 * returned by getAct() , getBill() etc. whichever may be the appropriate doc type. Encapsulated with other
	 * information related to the object
	 * @throws JAXBException
	 * @throws SAXException 
	 */    
	public static AnDocType parse(AnVersion version, Reader reader) throws SAXException, JAXBException{
		AnValidatorError vErr = AnValidator.validate(version, reader);
		if (vErr != null){
			JAXBElement anType = unmarshal(version, reader);
			return parse(version, anType);
		} else {
			logger.error("Error while validating XML document (reader)", vErr);
		}
		return null;
	}

	/**
	 * Unmarshalls an Akoma ntoso document - and returns an object of the identified type 
	 * in the document. If the document type cannot be idenitifed, returns null. 
	 * @param version an AnVersion object identifying the version of the schema to be used
	 * @param uri the Akoma Ntoso xml document as a URI (the URL class has a blocking problem)
	 * @return AnDocType - this can be OpenStructure, HierarchialStructure etc.This returns the object
	 * returned by getAct() , getBill() etc. whichever may be the appropriate doc type. Encapsulated with other
	 * information related to the object
	 * @throws JAXBException
	 * @throws SAXException 
	 */
	public static AnDocType parse(AnVersion version, URI uri) throws SAXException, JAXBException {
		AnValidatorError vErr = AnValidator.validate(version, uri);
		if (vErr != null){
			JAXBElement anType = unmarshal(version, uri);
			return parse(version, anType);
		} else {
			logger.error("Error while validating XML document (URI)", vErr);
		}
		return null;
	}

	/**
	 * Unmarshalls an Akoma ntoso document - and returns an object of the identified type 
	 * in the document. If the document type cannot be idenitifed, returns null. 
	 * @param version an AnVersion object identifying the version of the schema to be used
	 * @param fanXml the Akoma Ntoso xml document as a file. Note: no existence checks are done
	 * @return AnDocType - this can be OpenStructure, HierarchialStructure etc.This returns the object
	 * returned by getAct() , getBill() etc. whichever may be the appropriate doc type. Encapsulated with other
	 * information related to the object
	 * @throws JAXBException
	 * @throws SAXException 
	 */
	public static AnDocType parse(AnVersion version, File fanXml) throws SAXException, JAXBException {
		//AnValidatorError vErr = AnValidator.validate(version, fanXml);
		//if (vErr != null){
		JAXBElement anType = unmarshal(version, fanXml);
		return parse(version, anType);
		//} else {
		//    logger.error("Error while validating XML document (File)", vErr);
		//}
		//return null;
	}

	/**
	 * Internal parse() api called by the other APIs.
	 * @param version
	 * @param anType
	 * @return 
	 */ 
	private static AnDocType parse(AnVersion version, JAXBElement anType) {
		AnDocType objDocType = null;
		Object[] resultObj = AnTypeGetterHelper.getDocType(anType.getValue());
		if (resultObj != null) {
			String sType = (String)resultObj[0];
			Object methodResult = resultObj[1];
			objDocType = new AnDocType(methodResult, 
					sType, 
					methodResult.getClass().getName()
					);
		} 
		return objDocType;
	}

	/**
	 * 
	 * @param version
	 * @param fanXml
	 * @return
	 * @throws JAXBException 
	 */
	private static JAXBElement unmarshal(AnVersion version, File fanXml) throws JAXBException{
		Source source = new StreamSource(fanXml);
		return unmarshal(version, source);
	}

	/**
	 * 
	 * @param version
	 * @param reader
	 * @return
	 * @throws JAXBException 
	 */
	private static JAXBElement unmarshal(AnVersion version, Reader reader) throws JAXBException{
		Source source = new StreamSource(reader);
		return unmarshal(version, source);
	}

	/**
	 * Unmarshalls a stream and returns it as a root JAXBElement 
	 * @param version
	 * @param iStream
	 * @return
	 * @throws JAXBException 
	 */
	private static JAXBElement unmarshal(AnVersion version, InputStream iStream) throws JAXBException{
		Source source = new StreamSource(iStream);
		return unmarshal(version, source);
	}

	/**
	 * Unmarshalls a file uri and returns it as a root JAXBElement 
	 * @param version
	 * @param iStream
	 * @return
	 * @throws JAXBException 
	 */
	private static JAXBElement unmarshal(AnVersion version, URI uri) throws JAXBException{
		Source source = new StreamSource(uri.toASCIIString());
		return unmarshal(version, source);
	}

	/**
	 * Unmarshalls a Source and returns it as a root JAXBElement 
	 * @param version
	 * @param source
	 * @return
	 * @throws JAXBException 
	 */
	private static JAXBElement unmarshal(AnVersion version, Source source) throws JAXBException{
		Unmarshaller unmarshaller = version.getContext().createUnmarshaller();
		JAXBElement anType = (JAXBElement) unmarshaller.unmarshal(source);
		return anType;
	}

}
