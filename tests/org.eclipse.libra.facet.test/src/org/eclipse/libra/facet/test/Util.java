package org.eclipse.libra.facet.test;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class Util {
		
	public static void changeWebContextRootFromSettings(IFile settingsFile, String newValue) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();				
			DocumentBuilder db = dbf.newDocumentBuilder();
			File xmlFile = new File(settingsFile.getLocationURI());
			Document xmlDocument = db.parse(xmlFile);	
			changeWebContextRootFromSettings(xmlDocument, newValue);
			saveXML(xmlDocument, xmlFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static Element getWebModuleElement(Document xmlDocument) {
		Element root = xmlDocument.getDocumentElement();
		NodeList children = root.getElementsByTagName("wb-module");
		Element webModuleElem = (Element)children.item(0);
		return webModuleElem;
	}
	
	private static Element getContextRootProperty(Element webModuleElement) {
		NodeList webModuleElementProperties = webModuleElement.getElementsByTagName("property");
		for (int i = 0; i < webModuleElementProperties.getLength(); i++) {
			Element property = (Element)webModuleElementProperties.item(i);
			NamedNodeMap attributes = property.getAttributes();
			Attr nameAttribute = (Attr)attributes.getNamedItem("name");
			if (nameAttribute.getNodeValue().equals("context-root")) {
				return property;
			}
		}
		return null;
	}

	private static Attr getValueAttribute(Element contextRootProperty) {
		NamedNodeMap attributes = contextRootProperty.getAttributes();
		Attr valueAttribute = (Attr)attributes.getNamedItem("value");
		if (valueAttribute != null) { 
			return valueAttribute;
		}
		return null;
	}	
	
	private static void changeWebContextRootFromSettings(Document xmlDocument, String newValue) {
		Element webModuleElement = getWebModuleElement(xmlDocument); 
		Element contextRootProperty = getContextRootProperty(webModuleElement);
		Attr valueAttribute = getValueAttribute(contextRootProperty);
		valueAttribute.setNodeValue(newValue);
	}
	
	private static void saveXML(Document xmlDocument, File xmlFile) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();			
			transformer.transform(new DOMSource(xmlDocument), new StreamResult(xmlFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
