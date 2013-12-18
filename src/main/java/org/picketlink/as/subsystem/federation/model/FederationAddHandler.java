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
package org.picketlink.as.subsystem.federation.model;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.picketlink.as.subsystem.federation.service.FederationService;
import org.picketlink.as.subsystem.federation.service.IdentityProviderService;
import org.picketlink.as.subsystem.federation.service.ServiceProviderService;
import org.picketlink.as.subsystem.model.ModelElement;
import org.picketlink.as.subsystem.model.ModelUtils;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.identity.federation.core.config.IDPConfiguration;
import org.picketlink.identity.federation.core.config.SPConfiguration;
import org.picketlink.identity.federation.core.config.STSConfiguration;

import java.util.List;
import java.util.Set;

import static org.jboss.as.controller.registry.Resource.ResourceEntry;
import static org.picketlink.as.subsystem.model.ModelUtils.toSAMLConfig;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class FederationAddHandler extends AbstractAddStepHandler {

    public static final FederationAddHandler INSTANCE = new FederationAddHandler();

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext,
     * org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
     */
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                                  ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        createFederationService(operation, context, verificationHandler, newControllers);
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attribute: FederationResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

    /**
     * <p>
     * Creates a new {@link FederationService} instance for this configuration.
     * </p>
     *
     * @param operation
     * @param context
     * @param verificationHandler
     * @param newControllers
     */
    public void createFederationService(ModelNode operation, OperationContext context,
                                         ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        PathAddress pathAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));
        String federationAlias = pathAddress.getLastElement().getValue();
        Resource federation = context.readResourceFromRoot(pathAddress);

        final STSConfiguration stsConfiguration;

        if (federation.hasChildren(ModelElement.SAML.getName())) {
            ResourceEntry samlConfiguration = federation.getChildren(ModelElement.SAML.getName()).iterator().next();
            stsConfiguration = toSAMLConfig(context, samlConfiguration.getModel());
        } else {
            stsConfiguration = null;
        }

        KeyProviderType keyProviderType = null;

        if (federation.hasChildren(ModelElement.KEY_STORE.getName())) {
            ResourceEntry keyStoreConfiguration = federation.getChildren(ModelElement.KEY_STORE.getName()).iterator().next();
            keyProviderType = ModelUtils.toKeyProviderType(context, keyStoreConfiguration.getModel());
        }

        final IDPConfiguration idpConfiguration;

        if (federation.hasChildren(ModelElement.IDENTITY_PROVIDER.getName())) {
            ResourceEntry identityProvider = federation.getChildren(ModelElement.IDENTITY_PROVIDER.getName()).iterator().next();

            idpConfiguration = ModelUtils.toIDPConfig(context, identityProvider.getModel());

            idpConfiguration.setAlias(identityProvider.getName());

            idpConfiguration.setKeyProvider(keyProviderType);

            ServiceName name = IdentityProviderService.createServiceName(identityProvider.getName());
            IdentityProviderService identityProviderService = new IdentityProviderService(idpConfiguration, stsConfiguration);
            ServiceBuilder<IdentityProviderService> serviceBuilder = context.getServiceTarget().addService(name, identityProviderService);

            serviceBuilder.addDependency(FederationService.createServiceName(federationAlias), FederationService.class, identityProviderService.getFederationService());

            ServiceController<IdentityProviderService> controller = serviceBuilder.addListener(verificationHandler).setInitialMode(Mode.PASSIVE).install();

            if (newControllers != null) {
                newControllers.add(controller);
            }
        } else {
            idpConfiguration = null;
        }

        if (federation.hasChildren(ModelElement.SERVICE_PROVIDER.getName())) {
            Set<ResourceEntry> serviceProviders = federation.getChildren(ModelElement.SERVICE_PROVIDER.getName());

            for (ResourceEntry serviceProvider : serviceProviders) {
                SPConfiguration spConfiguration = ModelUtils.toSPConfig(context, serviceProvider.getModel());

                spConfiguration.setIdentityURL(idpConfiguration.getIdentityURL());

                spConfiguration.setKeyProvider(keyProviderType);

                ServiceName name = IdentityProviderService.createServiceName(serviceProvider.getName());
                ServiceProviderService serviceProviderService = new ServiceProviderService(spConfiguration, stsConfiguration);
                ServiceBuilder<ServiceProviderService> serviceBuilder = context.getServiceTarget().addService(name, serviceProviderService);

                serviceBuilder.addDependency(FederationService.createServiceName(federationAlias), FederationService.class, serviceProviderService.getFederationService());
                serviceBuilder.addDependency(IdentityProviderService.createServiceName(idpConfiguration.getAlias()), IdentityProviderService.class, serviceProviderService.getIdentityProviderService());

                ServiceController<ServiceProviderService> controller = serviceBuilder.addListener(verificationHandler).setInitialMode(Mode.PASSIVE).install();

                if (newControllers != null) {
                    newControllers.add(controller);
                }
            }
        }

        FederationService service = new FederationService();

        ServiceName serviceName = FederationService.createServiceName(federationAlias);
        ServiceBuilder<FederationService> serviceBuilder = context.getServiceTarget().addService(serviceName, service);

        serviceBuilder.addDependency(WebSubsystemServices.JBOSS_WEB);

        ServiceController<FederationService> controller = serviceBuilder
                .addListener(verificationHandler).setInitialMode(Mode.ACTIVE).install();

        if (newControllers != null) {
            newControllers.add(controller);
        }
    }

}
