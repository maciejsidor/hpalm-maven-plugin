/*
Copyright 2014 Maciej SIDOR [maciejsidor@gmail.com]

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
package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Maciej SIDOR
 * 
 * JAXB object representing defect
 */
@XmlRootElement(name="Entity")
public class Entity 
{
	@XmlElement(name="Field")
	@XmlElementWrapper(name="Fields")
	public List<Field> fields = null;

	@XmlAttribute
	public String type= null;

	public String id;

    public String dev;

    public String desc;

    public String dueTo;

    public String changeType; 
}
