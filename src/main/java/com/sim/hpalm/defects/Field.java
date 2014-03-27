package com.sim.hpalm.defects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Field 
{
	@XmlElement(name="Value")
	public String value = null;
	
	@XmlAttribute(name="Name")
	public String name  = null;
}
