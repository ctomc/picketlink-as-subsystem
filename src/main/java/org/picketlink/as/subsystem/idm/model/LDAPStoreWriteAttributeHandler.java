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
package org.picketlink.as.subsystem.idm.model;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class LDAPStoreWriteAttributeHandler extends AbstractWriteAttributeHandler<Void> {

    public static final LDAPStoreWriteAttributeHandler INSTANCE = new LDAPStoreWriteAttributeHandler();

    private LDAPStoreWriteAttributeHandler() {
        super(LDAPStoreResourceDefinition.URL, LDAPStoreResourceDefinition.BIND_DN,
                LDAPStoreResourceDefinition.BIND_CREDENTIAL, LDAPStoreResourceDefinition.AGENT_DN_SUFFIX,
                LDAPStoreResourceDefinition.BASE_DN_SUFFIX, LDAPStoreResourceDefinition.GROUP_DN_SUFFIX,
                LDAPStoreResourceDefinition.ROLE_DN_SUFFIX, LDAPStoreResourceDefinition.USER_DN_SUFFIX,
                LDAPStoreResourceDefinition.REALMS, LDAPStoreResourceDefinition.TIERS);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
            ModelNode resolvedValue, ModelNode currentValue,
            org.jboss.as.controller.AbstractWriteAttributeHandler.HandbackHolder<Void> handbackHolder)
            throws OperationFailedException {

        return false;
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
            ModelNode valueToRestore, ModelNode valueToRevert, Void handback) throws OperationFailedException {

    }

}
