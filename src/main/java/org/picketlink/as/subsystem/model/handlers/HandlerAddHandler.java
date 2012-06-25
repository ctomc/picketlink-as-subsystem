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

package org.picketlink.as.subsystem.model.handlers;

import java.util.List;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.picketlink.as.subsystem.model.AbstractResourceAddStepHandler;
import org.picketlink.as.subsystem.model.ModelElement;
import org.picketlink.as.subsystem.service.AbstractEntityProviderService;
import org.picketlink.as.subsystem.service.IdentityProviderService;
import org.picketlink.as.subsystem.service.ServiceProviderService;
import org.picketlink.identity.federation.core.handler.config.Handler;
import org.picketlink.identity.federation.core.handler.config.Handlers;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class HandlerAddHandler extends AbstractResourceAddStepHandler {

    public static final HandlerAddHandler INSTANCE = new HandlerAddHandler();

    private HandlerAddHandler() {
        super(ModelElement.HANDLER);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.controller.AbstractAddStepHandler#performRuntime(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode, org.jboss.dmr.ModelNode, org.jboss.as.controller.ServiceVerificationHandler, java.util.List)
     */
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        String providerAlias = operation.get(ModelDescriptionConstants.ADDRESS).asPropertyList().get(2).getValue().asString();
        String className = operation.get(ModelElement.HANDLER_CLASS.getName()).asString();
        
        AbstractEntityProviderService providerService = getParentProviderService(context, providerAlias);
        
        Handlers handlerChain = providerService.getPicketLinkType().getHandlers();
        
        Handler newHandler = new Handler();
        
        newHandler.setClazz(className);
        
        handlerChain.add(newHandler);
    }

    /**
     * <p>Returns the {@link AbstractEntityProviderService} instance to be used during the handler configuration.</p>
     * 
     * @param context
     * @param providerAlias
     * @return
     */
    private AbstractEntityProviderService getParentProviderService(OperationContext context, String providerAlias) {
        AbstractEntityProviderService providerService = IdentityProviderService.getService(context.getServiceRegistry(true), providerAlias);
        
        if (providerService == null) {
            providerService = ServiceProviderService.getService(context.getServiceRegistry(true), providerAlias);
        }
        return providerService;
    }
    
}