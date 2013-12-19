/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.as.subsystem.parser;

import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamWriter;
import org.picketlink.as.subsystem.PicketLinkMessages;
import org.picketlink.as.subsystem.model.AbstractResourceDefinition;
import org.picketlink.as.subsystem.model.ModelElement;
import org.picketlink.as.subsystem.model.XMLElement;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A generic XML Writer for all {@link ModelElement} definitions.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @since Mar 9, 2012
 */
public class ModelXMLElementWriter {

    private Map<String, ModelXMLElementWriter> register;
    private ModelElement modelElement;
    private XMLElement parentElement;
    private String nameAttribute;

    public ModelXMLElementWriter(ModelElement element, Map<String, ModelXMLElementWriter> register) {
        this.modelElement = element;
        this.register = register;
    }

    public ModelXMLElementWriter(ModelElement element, XMLElement parentElement, Map<String, ModelXMLElementWriter> register) {
        this.modelElement = element;
        this.parentElement = parentElement;
        this.register = register;
    }

    public ModelXMLElementWriter(ModelElement element, XMLElement parentElement, String nameAttribute, Map<String, ModelXMLElementWriter> register) {
        this.modelElement = element;
        this.parentElement = parentElement;
        this.nameAttribute = nameAttribute;
        this.register = register;
    }

    public ModelXMLElementWriter(ModelElement element, String nameAttribute, Map<String, ModelXMLElementWriter> register) {
        this.modelElement = element;
        this.nameAttribute = nameAttribute;
        this.register = register;
    }

    public void write(XMLExtendedStreamWriter writer, ModelNode modelNode) throws XMLStreamException {
        if (modelNode.asProperty().getName().equals(this.getModelElement().getName()) || modelNode.asProperty().getValue().hasDefined(this.getModelElement().getName())) {
            if (this.getParentElement() != null) {
                writer.writeStartElement(this.getParentElement().getName());                
            }

            for (ModelNode valueNode : modelNode.asProperty().getValue().asList()) {
                writer.writeStartElement(this.getModelElement().getName());

                if (this.nameAttribute != null) {
                    writer.writeAttribute(this.nameAttribute, valueNode.keys().iterator().next());
                }

                writeAttributes(writer, valueNode.asProperty().getValue());
                
                for (ModelNode propertyIdentity: valueNode.asProperty().getValue().asList()) {
                    List<ResourceDefinition> children = this.getChildResourceDefinitions();
                    
                    if (children != null) {
                        for (ResourceDefinition child : children) {
                            get(child.getPathElement().getKey()).write(writer, propertyIdentity);
                        }
                    }
                }

                writer.writeEndElement();
            }
            
            if (this.getParentElement() != null) {
                writer.writeEndElement();                
            }
        }        
    }

    public ModelXMLElementWriter get(String writerKey) {
        ModelXMLElementWriter writer = this.register.get(writerKey);

        if (writer == null) {
            throw PicketLinkMessages.MESSAGES.noModelElementWriterProvided(writerKey);
        }

        return writer;
    }

    protected List<SimpleAttributeDefinition> getAttributeDefinitions() {
        return AbstractResourceDefinition.getAttributeDefinition(this.modelElement);
    }

    protected List<ResourceDefinition> getChildResourceDefinitions() {
        return AbstractResourceDefinition.getChildResourceDefinitions(this.modelElement);
    }

    public void writeAttributes(XMLStreamWriter writer, ModelNode modelNode) throws XMLStreamException {
        for (SimpleAttributeDefinition simpleAttributeDefinition : getAttributeDefinitions()) {
            if (modelNode.hasDefined(simpleAttributeDefinition.getXmlName())) {
                simpleAttributeDefinition.marshallAsAttribute(modelNode, writer);
            }
        }
    }

    public ModelElement getModelElement() {
        return modelElement;
    }

    public XMLElement getParentElement() {
        return this.parentElement;
    }
}
