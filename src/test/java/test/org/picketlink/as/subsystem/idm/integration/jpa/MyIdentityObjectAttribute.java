/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.picketlink.as.subsystem.idm.integration.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.picketlink.idm.jpa.annotations.EntityType;
import org.picketlink.idm.jpa.annotations.IDMEntity;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
@IDMEntity(EntityType.IDENTITY_ATTRIBUTE)
public class MyIdentityObjectAttribute implements Serializable {

    private static final long serialVersionUID = 3072864795743589609L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn
    @IDMProperty (PropertyType.ATTRIBUTE_IDENTITY)
    private MyIdentityObject identityObject;

    @IDMProperty(PropertyType.ATTRIBUTE_NAME)
    private String name;

    @IDMProperty(PropertyType.ATTRIBUTE_VALUE)
    @Column (length=1024)
    private String value;

    @IDMProperty(PropertyType.ATTRIBUTE_TYPE)
    private String type;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyIdentityObject getIdentityObject() {
        return this.identityObject;
    }

    public void setIdentityObject(MyIdentityObject identityObject) {
        this.identityObject = identityObject;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        MyIdentityObjectAttribute other = (MyIdentityObjectAttribute) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
