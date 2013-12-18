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
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.identity.federation.core.config.IDPConfiguration;
import org.picketlink.identity.federation.core.config.PicketLinkConfigUtil;
import org.picketlink.identity.federation.core.config.STSConfiguration;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2IssuerTrustHandler;

/** ty Provider.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class IdentityProviderService extends EntityProviderService<IdentityProviderService, IDPConfiguration> {

    private static final String SERVICE_NAME = "IDPConfigurationService";

    public IdentityProviderService(IDPConfiguration idpConfiguration, STSConfiguration stsConfiguration) {
        super(idpConfiguration, stsConfiguration);
    }

    /* (non-Javadoc)
     * @see org.jboss.msc.service.Service#start(org.jboss.msc.service.StartContext)
     */
    @Override
    public void start(StartContext context) throws StartException {
        super.start(context);
    }

    /* (non-Javadoc)
     * @see org.jboss.msc.service.Service#stop(org.jboss.msc.service.StopContext)
     */
    @Override
    public void stop(StopContext context) {
        super.stop(context);
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.subsystem.service.EntityProviderService#doConfigureDeployment(org.jboss.as.server.deployment.DeploymentUnit)
     */
    protected void doConfigureDeployment(DeploymentUnit deploymentUnit) {
        if (getConfiguration().getKeyProvider() != null) {
            TrustType trustType = this.getConfiguration().getTrust();
            
            if (trustType != null) {
                String domainsStr = trustType.getDomains();
                
                if (domainsStr != null) {
                    String[] domains = domainsStr.split(",");
                    
                    for (int i = 0; i < domains.length; i++) {
                        KeyValueType kv = new KeyValueType();
                        
                        kv.setKey(domains[i]);
                        
                        String value = domains[i];
                        
                        if (this.getConfiguration().getTrustDomainAlias() != null) {
                            value = this.getConfiguration().getTrustDomainAlias().get(domains[i]);
                        }
                        
                        kv.setValue(value);
                        
                        getConfiguration().getKeyProvider().remove(kv);
                        getConfiguration().getKeyProvider().add(kv);
                    }
                }
            }
        }
    }

    @Override
    protected void doAddHandlers() {
        PicketLinkConfigUtil.addHandler(SAML2IssuerTrustHandler.class, getPicketLinkType());
    }

    public static ServiceName createServiceName(String alias) {
        return ServiceName.JBOSS.append(SERVICE_NAME, alias);
    }

}