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

package org.picketlink.as.subsystem.federation.model.idp;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.RestartParentResourceAddHandler;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.picketlink.as.subsystem.federation.model.FederationAddHandler;
import org.picketlink.as.subsystem.federation.service.FederationService;
import org.picketlink.as.subsystem.model.ModelElement;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class IdentityProviderAddHandler extends RestartParentResourceAddHandler {

    public static final IdentityProviderAddHandler INSTANCE = new IdentityProviderAddHandler();

    protected IdentityProviderAddHandler() {
        super(ModelElement.FEDERATION.getName());
    }

    @Override
    protected void recreateParentService(OperationContext context, PathAddress parentAddress, ModelNode parentModel,
                                         ServiceVerificationHandler verificationHandler) throws OperationFailedException {
        FederationAddHandler.INSTANCE.createFederationService(parentModel, context, verificationHandler, null);
    }

    @Override
    protected ServiceName getParentServiceName(PathAddress parentAddress) {
        return FederationService.createServiceName(parentAddress.getLastElement().getValue());
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition attribute : IdentityProviderResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

}
