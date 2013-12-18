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

package org.picketlink.as.subsystem.federation.model.handlers;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.picketlink.as.subsystem.federation.service.EntityProviderService;
import org.picketlink.as.subsystem.federation.service.IdentityProviderService;
import org.picketlink.as.subsystem.federation.service.ServiceProviderService;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.handler.Handler;
import org.picketlink.config.federation.handler.Handlers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class HandlerParameterAddHandler extends AbstractAddStepHandler {

    public static final HandlerParameterAddHandler INSTANCE = new HandlerParameterAddHandler();

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext,
     * org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                                  ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        PathAddress pathAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS));
        String providerAlias = pathAddress.subAddress(0, pathAddress.size() - 2).getLastElement().getValue();
        String handlerClassName = pathAddress.subAddress(0, pathAddress.size() - 1).getLastElement().getValue();
        String paramName = pathAddress.getLastElement().getValue();
        String paramValue = HandlerParameterResourceDefinition.VALUE.resolveModelAttribute(context, model).asString();

        EntityProviderService providerService = getParentProviderService(context, providerAlias);

        if (providerService != null) {
            Handlers handlerChain = providerService.getPicketLinkType().getHandlers();

            for (Handler handler : new ArrayList<Handler>(handlerChain.getHandler())) {
                if (handler.getClazz().equals(handlerClassName)) {
                    KeyValueType kv = new KeyValueType();

                    kv.setKey(paramName);
                    kv.setValue(paramValue);

                    handler.add(kv);
                }
            }
        }
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (SimpleAttributeDefinition attribute: HandlerParameterResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

    /**
     * <p>
     * Returns the {@link org.picketlink.as.subsystem.federation.service.EntityProviderService} instance to be used
     * during the handler configuration.
     * </p>
     *
     * @param context
     * @param providerAlias
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    private EntityProviderService getParentProviderService(OperationContext context, String providerAlias) {
        ServiceController<? extends EntityProviderService> serviceController =
                (ServiceController<IdentityProviderService>) context.getServiceRegistry(true).getService(IdentityProviderService.createServiceName(providerAlias));

        if (serviceController == null) {
            serviceController = (ServiceController<ServiceProviderService>) context.getServiceRegistry(true).getService(ServiceProviderService.createServiceName(providerAlias));
        }

        return serviceController.getValue();
    }

}
