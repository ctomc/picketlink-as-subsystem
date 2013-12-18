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

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.picketlink.as.subsystem.federation.service.IdentityProviderService;
import org.picketlink.as.subsystem.model.ModelElement;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;

import java.util.List;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class TrustDomainAddHandler extends AbstractAddStepHandler {

    public static final TrustDomainAddHandler INSTANCE = new TrustDomainAddHandler();

    /* (non-Javadoc)
     * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
     */
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        PathAddress pathAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS));
        String alias = pathAddress.subAddress(0, pathAddress.size() - 1).getLastElement().getValue();
        String domain = pathAddress.getLastElement().getValue();
        ModelNode certAliasNode = TrustDomainResourceDefinition.CERT_ALIAS.resolveModelAttribute(context, model);
        String certAlias = null;

        if (certAliasNode.isDefined()) {
            certAlias = operation.get(ModelElement.IDENTITY_PROVIDER_TRUST_DOMAIN_CERT_ALIAS.getName()).asString();
        }
        
        ServiceController<IdentityProviderService> serviceController =
                (ServiceController<IdentityProviderService>) context.getServiceRegistry(true).getService(IdentityProviderService.createServiceName(alias));
        IdentityProviderService service = serviceController.getValue();

        KeyProviderType keyProvider = service.getConfiguration().getKeyProvider();
        
        if (keyProvider != null) {
            KeyValueType keyValue = new KeyValueType();
            
            keyValue.setKey(domain);
            
            if (certAlias != null) {
                keyValue.setValue(certAlias);
            } else {
                keyValue.setValue(domain);
            }
            
            keyProvider.add(keyValue);
        }
        
        service.getConfiguration().addTrustDomain(domain, certAlias);
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attribute: TrustDomainResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }
    
}
