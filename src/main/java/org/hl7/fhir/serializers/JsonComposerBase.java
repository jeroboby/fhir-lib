package org.hl7.fhir.serializers;

/*
Copyright (c) 2011-2013, HL7, Inc
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this 
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation 
   and/or other materials provided with the distribution.
 * Neither the name of HL7 nor the names of its contributors may be used to 
   endorse or promote products derived from this software without specific 
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

 */


import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.hl7.fhir.parsers.XmlParserBase;
import org.hl7.fhir.model.AtomEntry;
import org.hl7.fhir.model.AtomFeed;
import org.hl7.fhir.model.Binary;
import org.hl7.fhir.model.Resource;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xhtml.XhtmlComposer;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import com.google.gson.stream.JsonWriter;


public abstract class JsonComposerBase extends XmlParserBase implements Composer {

	protected JsonWriter json;
	private boolean htmlPretty;
	//private boolean jsonPretty;

	public void compose(OutputStream stream, Resource resource, boolean pretty) throws Exception {
		OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
		JsonWriter writer = new JsonWriter(osw);

        writer.setIndent(pretty ? "  ":"");
		writer.beginObject();
		compose(writer, resource);
		writer.endObject();
		osw.flush();
	}

	public void compose(OutputStream stream, AtomFeed feed, boolean pretty) throws Exception {
		OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
		JsonWriter writer = new JsonWriter(osw);
        writer.setIndent(pretty ? "  ":"");
		writer.beginObject();
		compose(writer, feed);
		writer.endObject();
		osw.flush();
	}

	public void compose(JsonWriter writer, Resource resource) throws Exception {
		json = writer;
		composeResource(resource);
	}

	public void compose(JsonWriter writer, AtomFeed feed) throws Exception {
		json = writer;
		openObject("feed");
		composeFeed(feed);
		closeObject();
	}

  // standard order for round-tripping examples succesfully:
  // title, id, links, updated, published, authors
	private void composeFeed(AtomFeed feed) throws Exception {

	  prop("title", feed.getTitle());
    prop("id", feed.getId());
    if (feed.getLinks().size() > 0) {
      openArray("link");
      for (String n : feed.getLinks().keySet()) {
        json.beginObject();
        prop("rel", n);
        prop("href", feed.getLinks().get(n));
        json.endObject();
      }
      closeArray();
    }
		if (feed.getUpdated() != null)
			prop("updated", dateToXml(feed.getUpdated()));
		if (feed.getTags().size() > 0) {
			openArray("category");
			for (String uri : feed.getTags().keySet()) {
				json.beginObject();
				prop("scheme", "http://hl7.org/fhir/tag");
				prop("term", uri);
				String label = feed.getTags().get(uri);
				if (!Utilities.noString(label))
					prop("label", label);
				json.endObject();
			}
			closeArray();
		}


		if (feed.getAuthorName() != null || feed.getAuthorUri() != null) {
		  openArray("author");
		  json.beginObject();
		  if (feed.getAuthorName() != null)
		    prop("name", feed.getAuthorName());
		  if (feed.getAuthorUri() != null)
		    prop("uri", feed.getAuthorUri());
		  json.endObject();
		  closeArray();
		}

		if (feed.getEntryList().size() > 0) {
			openArray("entry");
			for (AtomEntry<? extends Resource> e : feed.getEntryList())
				composeEntry(e);
			closeArray();
		}
	}

  // standard order for round-tripping examples succesfully:
  // title, id, links, updated, published, authors 
	private <T extends Resource> void composeEntry(AtomEntry<T> e) throws Exception {
		json.beginObject();
		prop("title", e.getTitle());
		prop("id", e.getId());
		if (e.getLinks().size() > 0) {
		  openArray("link");
		  for (String n : e.getLinks().keySet()) {
		    json.beginObject();
		    prop("rel", n);
		    prop("href", e.getLinks().get(n));
		    json.endObject();
		  }
		  closeArray();
		}

		if (e.getUpdated() != null)
			prop("updated", dateToXml(e.getUpdated()));
		if (e.getPublished() != null) 
			prop("published", dateToXml(e.getPublished()));

    if (e.getAuthorName() != null || e.getAuthorUri() != null) {
      openArray("author");
      json.beginObject();
      if (e.getAuthorName() != null)
        prop("name", e.getAuthorName());
      if (e.getAuthorUri() != null)
        prop("uri", e.getAuthorUri());
      json.endObject();
      closeArray();
    }


		if (e.getTags().size() > 0) {
			openArray("category");
			for (String uri : e.getTags().keySet()) {
				json.beginObject();
				prop("scheme", "http://hl7.org/fhir/tag");
				prop("term", uri);
				String label = e.getTags().get(uri);
				if (!Utilities.noString(label))
					prop("label", label);
				json.endObject();
			}
			closeArray();
		}

		open("content");
		composeResource(e.getResource());
		close();
		if (e.getSummary() != null) {
		  composeXhtml("summary", e.getSummary());
		}
		json.endObject();  

	}

	protected abstract void composeResource(Resource resource) throws Exception;

//	protected void composeElement(Element element) throws Exception {
//		if (element.getXmlId() != null) 
//			prop("_id", element.getXmlId());
//	}
//
	protected void prop(String name, String value) throws Exception {
		if (name != null)
			json.name(name);
		json.value(value);
	}

  protected void prop(String name, java.lang.Boolean value) throws Exception {
    if (name != null)
      json.name(name);
    json.value(value);
  }

  protected void prop(String name, java.lang.Integer value) throws Exception {
    if (name != null)
      json.name(name);
    json.value(value);
  }

//	protected void composeType(Type type) throws Exception {
//		composeElement(type);
//	}

//	protected void composeStringSimple(String name, String value) throws Exception {
//		if (value != null)
//			prop(name, value);
//	}
//
//	protected void composeStringSimple(String name, String_ value) throws Exception {
//		if (value != null)
//			composeStringSimple(name, value.getValue());
//	}
//
//	protected void composeStringSimple(String name, Code value) throws Exception {
//		if (value != null)
//			prop(name, value.getValue());
//	}
//
//	protected void composeCodeSimple(String name, Code value) throws Exception {
//		if (value != null)
//			prop(name, value.getValue());
//	}
//
//	 protected void composeString(String name, String value) throws Exception {
//		if (value != null)
//			prop(name, value);
//	}
//	protected void composeURI(String name, java.net.URI value) throws Exception {
//		if (value != null)
//			prop(name, value.toString());
//	}
//
//	protected void composeBigDecimal(String name, BigDecimal value) throws Exception {
//		if (value != null)
//			prop(name, value.toString());
//	}
//
//
//	protected void composeBigDecimalSimple(String name, Decimal value) throws Exception {
//		if (value != null)
//			composeBigDecimal(name, value.getValue());
//	}
//
//
//	protected void composeInt(String name, java.lang.Integer value) throws Exception {
//		if (value != null)
//			prop(name, value.toString());
//	}
//
//	protected void composeIntSimple(String name, Integer value) throws Exception {
//		if (value != null)
//			composeInt(name, value.getValue());
//	}
//
//	protected void composeBool(String name, java.lang.Boolean value) throws Exception {
//		if (value != null)
//			prop(name, value.toString());
//	}
//
//	protected void composeXhtmlSimple(String name, XhtmlNode html) throws Exception {
//		composeXhtml(name, html);
//	}
//	
	protected void composeXhtml(String name, XhtmlNode html) throws Exception {
		XhtmlComposer comp = new XhtmlComposer();
		comp.setPretty(htmlPretty);
		prop(name, comp.compose(html));
	}

//	protected void composeBytes(String name, byte[] content) throws Exception {
//		if (content != null) {
//			byte[] encodeBase64 = Base64.encodeBase64(content);
//			composeString(name, new String(encodeBase64));
//		}
//	}  
//
//	protected void composeBytesSimple(String name, Base64Binary content) throws Exception {
//		if (content != null) {
//			byte[] encodeBase64 = Base64.encodeBase64(content.getValue());
//			composeString(name, new String(encodeBase64));
//		}
//	}  
//
//	protected void composeBase64Binary(String name, Base64Binary value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			composeBytes("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeId(String name, Id value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeCode(String name, Code value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeOid(String name, Oid value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeUuid(String name, Uuid value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeSid(String name, Sid value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue().toString());
//			close();
//		}
//	}
//
//	protected void composeUriSimple(String name, Uri value) throws Exception {
//		if (value != null) {
//			prop(name, value.getValue().toString());
//		}
//	}
//
//	protected void composeUri(String name, Uri value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue().toString());
//			close();
//		}
//	}
//
//	protected void composeUri(String name, java.net.URI value) throws Exception {
//		composeURI(name, value);
//	}
//
//
//	protected void composeDecimal(String name, Decimal value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue().toString());
//			close();
//		}
//	}
//
//	protected void composeString_(String name, String_ value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeBoolean(String name, Boolean value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", java.lang.Boolean.toString(value.getValue()));
//			close();
//		}
//	}
//
//	protected void composeBooleanSimple(String name, Boolean value) throws Exception {
//		if (value != null) {
//			prop("value", java.lang.Boolean.toString(value.getValue()));
//		}
//	}
//
//	protected void composeBoolean(String name, java.lang.Boolean value) throws Exception {
//		if (value != null) {
//			open(name);
//			prop("value", value.toString());
//			close();
//		}
//	}
//
//	protected void composeInstant(String name, Instant value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", dateToXml(value.getValue()));
//			close();
//		}
//	}
//
//	protected void composeInteger(String name, Integer value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", java.lang.Integer.toString(value.getValue()));
//			close();
//		}
//	}
//
//	protected void composeDate(String name, java.util.Calendar value) throws Exception {
//		if (value != null) {
//			prop(name, dateToXml(value));
//		}
//	}
//
//	protected void composeDate(String name, Date value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeDateSimple(String name, Date value) throws Exception {
//		if (value != null) {
//			prop(name, value.getValue());
//		}
//	}
//
//	protected void composeDateSimple(String name, Instant value) throws Exception {
//		if (value != null) {
//			composeDate(name, value.getValue());
//		}
//	}
//
//	protected void composeDateTime(String name, DateTime value) throws Exception {
//		if (value != null) {
//			open(name);
//			composeTypeAttributes(value);
//			prop("value", value.getValue());
//			close();
//		}
//	}
//
//	protected void composeDateTimeSimple(String name, DateTime value) throws Exception {
//		if (value != null) {
//			prop(name, value.getValue());
//		}
//	}

	protected void open(String name) throws Exception {
		if (name != null) 
			json.name(name);
		json.beginObject();
	}

	protected void close() throws Exception {
		json.endObject();
	}

	protected void openArray(String name) throws Exception {
		if (name != null) 
			json.name(name);
		json.beginArray();
	}

	protected void closeArray() throws Exception {
		json.endArray();
	}

	protected void openObject(String name) throws Exception {
		if (name != null) 
			json.name(name);
		json.beginObject();
	}

	protected void closeObject() throws Exception {
		json.endObject();
	}

  protected void composeBinary(String name, Binary element) throws Exception {
    if (element != null) {
      open(name);
      if (element.getXmlId() != null)
        prop("_id", element.getXmlId());
      prop("contentType", element.getContentType());
      prop("content", toString(element.getContent()));
      close();
    }    
    
  }
  
}
