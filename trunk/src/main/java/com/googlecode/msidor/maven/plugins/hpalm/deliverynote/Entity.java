package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Entity")
public class Entity 
{
	@XmlElement(name="Field")
	@XmlElementWrapper(name="Fields")
	public List<Field> fields = null;

	@XmlAttribute
	public String type= null;

	public String id;
}
