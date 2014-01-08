/*
 * Copyright 2013 WSO2Mobile

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.wso2mobile.mam.packageExtractor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;

public class ZipFileReading {

	public static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	public String readAndroidManifestFile(String filePath) {
		String xml = "";
		try {
			ZipInputStream stream = new ZipInputStream(new FileInputStream(
					filePath));
			try {
				ZipEntry entry;
				while ((entry = stream.getNextEntry()) != null) {
					if (entry.getName().equals("AndroidManifest.xml")) {
						StringBuilder builder = new StringBuilder();
						xml = AndroidXMLParsing.decompressXML(IOUtils
								.toByteArray(stream));
					}
				}
			} finally {
				stream.close();
			}
			Document doc = loadXMLFromString(xml);
			doc.getDocumentElement().normalize();
			JSONObject obj = new JSONObject();
			obj.put("version",
					doc.getDocumentElement().getAttribute("versionName"));
			obj.put("package", doc.getDocumentElement().getAttribute("package"));
			xml = obj.toJSONString();
		} catch (Exception e) {
			xml = "Exception occured " + e;
		}
		return xml;
	}

	public String readiOSManifestFile(String filePath, String name) {
		String plist = "";
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			File file = new File(filePath);
			ZipInputStream stream = new ZipInputStream(
					new FileInputStream(file));
			try {
				ZipEntry entry;
				while ((entry = stream.getNextEntry()) != null) {
					if (entry.getName().equals(
							"Payload/" + name + ".app/Info.plist")) {
						InputStream is = stream;

						int nRead;
						byte[] data = new byte[16384];

						while ((nRead = is.read(data, 0, data.length)) != -1) {
							buffer.write(data, 0, nRead);
						}

						buffer.flush();

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				stream.close();
			}
			NSDictionary rootDict = (NSDictionary) BinaryPropertyListParser
					.parse(buffer.toByteArray());
			JSONObject obj = new JSONObject();
			obj.put("version", rootDict.objectForKey("CFBundleVersion").toString());
			obj.put("name", rootDict.objectForKey("CFBundleName").toString());
			obj.put("package",
					rootDict.objectForKey("CFBundleIdentifier").toString());
			plist = obj.toJSONString();
		} catch (Exception e) {
			plist = "Exception occured " + e;
			e.printStackTrace();
		}
		return plist;
	}
}
