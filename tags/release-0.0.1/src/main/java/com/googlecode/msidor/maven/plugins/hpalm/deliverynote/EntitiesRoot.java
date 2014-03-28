package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * <Entities TotalResults="19791"><Entity Type="defect"><Fields><Field Name="has-change"><Value/></Field><Field Name="planned-closing-ver"><Value>DBM GDS_v4.0a</Value>
 * */

@XmlRootElement(name="Entities")
public class EntitiesRoot 
{
	@XmlElement(name="Entity")
	public List<Entity> entities = null;
}
