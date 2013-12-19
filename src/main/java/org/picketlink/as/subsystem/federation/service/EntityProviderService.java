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
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.as.web.ext.WebContextFactory;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.picketlink.as.subsystem.PicketLinkLogger;
import org.picketlink.as.subsystem.federation.metrics.PicketLinkSubsystemMetrics;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.TokenProviderType;
import org.picketlink.config.federation.handler.Handler;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.identity.federation.core.config.ProviderConfiguration;
import org.picketlink.identity.federation.core.config.SAMLConfiguration;
import org.picketlink.identity.federation.core.config.STSConfiguration;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.web.handlers.saml2.RolesGenerationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2EncryptionHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2LogOutHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler;

import java.util.ArrayList;
import java.util.List;

import static org.picketlink.identity.federation.core.config.PicketLinkConfigUtil.addHandler;
import static org.picketlink.identity.federation.core.config.PicketLinkConfigUtil.createSTSType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public abstract class EntityProviderService<T extends PicketLinkFederationService<T>, C extends ProviderConfiguration> implements PicketLinkFederationService<T> {

    private PicketLinkType picketLinkType;
    private InjectedValue<FederationService> federationService = new InjectedValue<FederationService>();
    private PicketLinkSubsystemMetrics metrics;
    private static List<Class<? extends SAML2Handler>> commonHandlersList = new ArrayList<Class<? extends SAML2Handler>>();

    static {
        commonHandlersList.add(SAML2LogOutHandler.class);
        commonHandlersList.add(SAML2AuthenticationHandler.class);
        commonHandlersList.add(RolesGenerationHandler.class);
        commonHandlersList.add(SAML2EncryptionHandler.class);
        commonHandlersList.add(SAML2SignatureValidationHandler.class);
    }

    private SAMLConfiguration stsType;

    public EntityProviderService(C configuration, STSConfiguration stsConfiguration) {
        createPicketLinkType((ProviderType) configuration, stsConfiguration);
    }

    @Override
    public void start(StartContext context) throws StartException {
        PicketLinkLogger.ROOT_LOGGER.debugf("Starting service for %s.", getConfiguration().getAlias());
    }

    @Override
    public void stop(StopContext context) {
        PicketLinkLogger.ROOT_LOGGER.debugf("Stopping service for %s.", getConfiguration().getAlias());
    }

    /**
     * <p>
     * Configures a {@link DeploymentUnit} as a PicketLink Provider.
     * </p>
     *
     * @param deploymentUnit
     */
    public void configure(DeploymentUnit deploymentUnit) {
        configureHandlers();
        configureWarMetadata(deploymentUnit);
        configurePicketLinkWebContextFactory(deploymentUnit);
        doConfigureDeployment(deploymentUnit);
        configureTokenProviders();
    }

    /**
     * <p>Configure the STS Token Providers.</p>
     */
    private void configureTokenProviders() {
        if (getPicketLinkType().getStsType() != null) {
            int tokenTimeout = getPicketLinkType().getStsType().getTokenTimeout();
            int clockSkew = getPicketLinkType().getStsType().getClockSkew();

            List<TokenProviderType> tokenProviders = getPicketLinkType().getStsType().getTokenProviders().getTokenProvider();

            for (TokenProviderType tokenProviderType : tokenProviders) {
                if (tokenProviderType.getTokenType().equals(JBossSAMLURIConstants.ASSERTION_NSURI.get())) {
                    KeyValueType keyValueTypeTokenTimeout = new KeyValueType();

                    keyValueTypeTokenTimeout.setKey(GeneralConstants.ASSERTIONS_VALIDITY);
                    keyValueTypeTokenTimeout.setValue(String.valueOf(tokenTimeout));

                    KeyValueType keyValueTypeClockSkew = new KeyValueType();

                    keyValueTypeClockSkew.setKey(GeneralConstants.CLOCK_SKEW);
                    keyValueTypeClockSkew.setValue(String.valueOf(clockSkew));

                    tokenProviderType.add(keyValueTypeTokenTimeout);
                    tokenProviderType.add(keyValueTypeClockSkew);
                }
            }
        }
    }

    /**
     * <p>Configure the SAML Handlers.</p>
     */
    private void configureHandlers() {
        List<Handler> handlers = getPicketLinkType().getHandlers().getHandler();

        // remove the common handlers from the configuration. leaving only the user defined handlers.
        for (Class<?> commonHandlerClass : commonHandlersList) {
            for (Handler handler : new ArrayList<Handler>(handlers)) {
                if (handler.getClazz().equals(commonHandlerClass.getName())) {
                    getPicketLinkType().getHandlers().remove(handler);
                }
            }
        }

        getPicketLinkType().setHandlers(new Handlers());

        doAddHandlers();

        for (Handler handler : handlers) {
            getPicketLinkType().getHandlers().add(handler);
        }

        for (Class<? extends SAML2Handler> commonHandlerClass : commonHandlersList) {
            addHandler(commonHandlerClass, getPicketLinkType());
        }
    }

    /**
     * <p>Hook for pre-configured handlers.</p>
     */
    protected void doAddHandlers() {
    }

    /**
     * <p>Configures the {@link WarMetaData}.</p>
     *
     * @param deploymentUnit
     */
    private void configureWarMetadata(DeploymentUnit deploymentUnit) {
        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);

        warMetaData.getMergedJBossWebMetaData().setSecurityDomain(this.getConfiguration().getSecurityDomain());
    }

    /**
     * <p>
     * Add a instance of {@link PicketLinkWebContextFactory} to the attachment list for this {@link DeploymentUnit}
     * instance.
     * This methods allows to pass to the JBoss Web subsystem a custom {@link WebContextFactory} implementation that
     * will be used
     * to configureDeployment the deployment unit.
     * </p>
     *
     * @param deploymentUnit
     */
    private void configurePicketLinkWebContextFactory(DeploymentUnit deploymentUnit) {
        deploymentUnit.putAttachment(WebContextFactory.ATTACHMENT, createPicketLinkWebContextFactory());
    }

    /**
     * <p>
     * Creates a instance of {@link PicketLinkWebContextFactory}.
     * </p>
     *
     * @return
     */
    private PicketLinkWebContextFactory createPicketLinkWebContextFactory() {
        return new PicketLinkWebContextFactory(new DomainModelConfigProvider(getPicketLinkType()), getMetrics());
    }

    @Override
    public PicketLinkSubsystemMetrics getMetrics() {
        if (this.metrics == null) {
            try {
                this.metrics = new PicketLinkSubsystemMetrics(((ProviderConfiguration) getPicketLinkType().getIdpOrSP()).getSecurityDomain());
            } catch (ConfigurationException e) {
                PicketLinkLogger.ROOT_LOGGER.error("Error while configuring the metrics collector. Metrics will not be collected.", e);
            }
        }

        return this.metrics;
    }

    /**
     * <p>
     * Subclasses should implement this method to configureDeployment a specific PicketLink Provider type. Eg.:
     * Identity
     * Provider
     * or Service Provider.
     * </p>
     *
     * @param deploymentUnit
     */
    protected abstract void doConfigureDeployment(DeploymentUnit deploymentUnit);

    @SuppressWarnings("unchecked")
    @Override
    public T getValue() throws IllegalStateException, IllegalArgumentException {
        return (T) this;
    }

    public C getConfiguration() {
        return (C) getPicketLinkType().getIdpOrSP();
    }

    public InjectedValue<FederationService> getFederationService() {
        return this.federationService;
    }

    public Handlers getHandlers() {
        return getPicketLinkType().getHandlers();
    }

    public STSType getStsType() {
        return getPicketLinkType().getStsType();
    }

    protected PicketLinkType getPicketLinkType() {
        return this.picketLinkType;
    }

    private void createPicketLinkType(final ProviderType configuration, final STSConfiguration stsConfiguration) {
        this.picketLinkType = new PicketLinkType();

        STSType stsType = createSTSType();

        if (stsConfiguration != null) {
            stsType.setClockSkew(stsConfiguration.getClockSkew());
            stsType.setTokenTimeout(stsConfiguration.getTokenTimeout());
        }

        this.picketLinkType.setStsType(stsType);
        this.picketLinkType.setHandlers(new Handlers());
        this.picketLinkType.setEnableAudit(true);

        this.picketLinkType.setIdpOrSP(configuration);
    }

}
