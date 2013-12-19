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

package org.picketlink.as.subsystem.federation.service;


import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.value.InjectedValue;
import org.picketlink.identity.federation.core.config.SPConfiguration;
import org.picketlink.identity.federation.core.config.STSConfiguration;

/**
 * <p>
 * Service implementation to enable a deployed applications as a Service Provider.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */

public class ServiceProviderService extends EntityProviderService<ServiceProviderService, SPConfiguration> {

    private static final String SERVICE_NAME = "SPConfigurationService";
    private InjectedValue<IdentityProviderService> identityProviderService = new InjectedValue<IdentityProviderService>();

    public ServiceProviderService(SPConfiguration configuration, STSConfiguration stsConfiguration) {
        super(configuration, stsConfiguration);
    }
    
    public void doConfigureDeployment(DeploymentUnit deploymentUnit) {
        configureBindingType();
    }
 
    private void configureBindingType() {
        if (getConfiguration().isPostBinding()) {
            getConfiguration().setBindingType("POST");
        } else {
            getConfiguration().setBindingType("REDIRECT");
        }
    }
    
    public static ServiceName createServiceName(String alias) {
        return ServiceName.JBOSS.append(SERVICE_NAME, alias);
    }

    /**
     * Returns a instance of the service associated with the given name.
     * 
     * @param registry
     * @param name
     * @return
     */
    public static ServiceProviderService getService(ServiceRegistry registry, String name) {
        ServiceController<?> container = registry.getService(ServiceProviderService.createServiceName(name));
        
        if (container != null) {
            return (ServiceProviderService) container.getValue();
        }
        
        return null;
    }

    public InjectedValue<IdentityProviderService> getIdentityProviderService() {
        return this.identityProviderService;
    }
}