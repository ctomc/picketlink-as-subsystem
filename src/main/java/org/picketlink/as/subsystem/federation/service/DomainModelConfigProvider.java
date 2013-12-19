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

import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

import java.io.InputStream;

/**
 * <p>
 * {@link SAMLConfigurationProvider} to be used to with identity providers and service providers
 * deployments considering the configurations defined in the subsystem.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class DomainModelConfigProvider extends AbstractSAMLConfigurationProvider {

    private PicketLinkType configuration;

    public DomainModelConfigProvider(PicketLinkType picketLinkType) {
        this.configuration = picketLinkType;
    }

    @Override
    public IDPType getIDPConfiguration() {
        ProviderType providerType = this.getPicketLinkConfiguration().getIdpOrSP();

        if (providerType != null && providerType instanceof IDPType) {
            return (IDPType) providerType;
        }

        return null;
    }

    @Override
    public SPType getSPConfiguration() {
        ProviderType providerType = this.getPicketLinkConfiguration().getIdpOrSP();

        if (providerType != null && providerType instanceof SPType) {
            return (SPType) providerType;
        }

        return null;
    }

    @Override
    public PicketLinkType getPicketLinkConfiguration() {
        if (super.configParsedPicketLinkType != null) {
            if (super.configParsedPicketLinkType.getHandlers() != null) {
                this.configuration.setHandlers(super.configParsedPicketLinkType.getHandlers());
            }
            if (super.configParsedPicketLinkType.getStsType() != null) {
                this.configuration.setStsType(super.configParsedPicketLinkType.getStsType());
            }
        }

        return this.configuration;
    }

    @Override
    public void setConsolidatedConfigFile(InputStream is) throws ParsingException {
        try {
            super.setConsolidatedConfigFile(is);
        } catch (Exception e) {
            logger.trace("Configurations defined in picketlink.xml will be ignored.");
        }
    }

    boolean isServiceProviderConfiguration() {
        return getSPConfiguration() != null;
    }

    boolean isIdentityProviderConfiguration() {
        return getIDPConfiguration() != null;
    }

}