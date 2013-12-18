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

package org.picketlink.as.subsystem.model;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.picketlink.as.subsystem.federation.model.KeyProviderResourceDefinition;
import org.picketlink.as.subsystem.federation.model.idp.IdentityProviderResourceDefinition;
import org.picketlink.as.subsystem.federation.model.saml.SAMLResourceDefinition;
import org.picketlink.as.subsystem.federation.model.sp.ServiceProviderResourceDefinition;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.identity.federation.core.config.IDPConfiguration;
import org.picketlink.identity.federation.core.config.SPConfiguration;
import org.picketlink.identity.federation.core.config.STSConfiguration;
import org.picketlink.identity.federation.core.impl.KeyStoreKeyManager;

/**
 * <p>
 * Utility methods for the PicketLink Subsystem's model.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class ModelUtils {

    /**
     * <p>
     * Extract from the ${@ModelNode} instance the federation's alias attribute value.
     * </p>
     *
     * @param fromModel
     *
     * @return
     */
    public static String getFederationAlias(ModelNode fromModel) {
        return fromModel.get(ModelDescriptionConstants.ADDRESS).asPropertyList().get(1).getValue().asString();
    }

    /**
     * <p>
     * Converts a {@ModelNode} instance to a {@link STSConfiguration} instance.
     * This method only extract the attributes defined in the <code>ModelElement.SAML</code> model.
     * </p>
     *
     * @param model
     *
     * @return
     */
    public static final STSConfiguration toSAMLConfig(OperationContext context, ModelNode fromModel) throws OperationFailedException {
        int tokenTimeout = SAMLResourceDefinition.TOKEN_TIMEOUT.resolveModelAttribute(context, fromModel).asInt();
        int clockSkew = SAMLResourceDefinition.CLOCK_SKEW.resolveModelAttribute(context, fromModel).asInt();

        STSConfiguration stsType = new STSConfiguration();

        stsType.setTokenTimeout(tokenTimeout);
        stsType.setClockSkew(clockSkew);

        return stsType;
    }

    /**
     * <p>
     * Converts a {@ModelNode} instance to a {@link SPConfiguration} instance.
     * </p>
     *
     * @param model
     *
     * @return
     */
    public static SPConfiguration toSPConfig(OperationContext context, ModelNode fromModel) throws OperationFailedException {
        SPConfiguration spType = new SPConfiguration();

        String alias = ServiceProviderResourceDefinition.ALIAS.resolveModelAttribute(context, fromModel).asString();

        spType.setAlias(alias);

        String url = ServiceProviderResourceDefinition.URL.resolveModelAttribute(context, fromModel).asString();

        spType.setServiceURL(url);

        String securityDomain = ServiceProviderResourceDefinition.SECURITY_DOMAIN.resolveModelAttribute(context, fromModel).asString();

        spType.setSecurityDomain(securityDomain);

        boolean postBinding = ServiceProviderResourceDefinition.POST_BINDING.resolveModelAttribute(context, fromModel).asBoolean();

        if (postBinding) {
            spType.setBindingType("POST");
        } else {
            spType.setBindingType("REDIRECT");
        }

        spType.setPostBinding(postBinding);

        boolean supportsSignatures = ServiceProviderResourceDefinition.SUPPORTS_SIGNATURES.resolveModelAttribute(context, fromModel).asBoolean();

        spType.setSupportsSignature(supportsSignatures);

        boolean strictPostBinding = ServiceProviderResourceDefinition.STRICT_POST_BINDING.resolveModelAttribute(context, fromModel).asBoolean();

        spType.setIdpUsesPostBinding(strictPostBinding);

        String errorPage = ServiceProviderResourceDefinition.ERROR_PAGE.resolveModelAttribute(context, fromModel).asString();

        spType.setErrorPage(errorPage);

        String logoutPage = ServiceProviderResourceDefinition.LOGOUT_PAGE.resolveModelAttribute(context, fromModel).asString();

        spType.setLogOutPage(logoutPage);

        return spType;
    }

    /**
     * <p>
     * Converts a {@ModelNode} instance to a {@link IDPConfiguration} instance.
     * </p>
     *
     * @param model
     *
     * @return
     */
    public static IDPConfiguration toIDPConfig(OperationContext context, ModelNode fromModel) throws OperationFailedException {
        IDPConfiguration idpType = new IDPConfiguration();

        String alias = IdentityProviderResourceDefinition.ALIAS.resolveModelAttribute(context, fromModel).asString();

        idpType.setAlias(alias);

        String url = IdentityProviderResourceDefinition.URL.resolveModelAttribute(context, fromModel).asString();

        idpType.setIdentityURL(url);

        boolean supportsSignatures = IdentityProviderResourceDefinition.SUPPORTS_SIGNATURES.resolveModelAttribute(context, fromModel).asBoolean();

        idpType.setSupportsSignature(supportsSignatures);

        boolean encrypt = IdentityProviderResourceDefinition.ENCRYPT.resolveModelAttribute(context, fromModel).asBoolean();

        idpType.setEncrypt(encrypt);

        boolean strictPostBinding = IdentityProviderResourceDefinition.STRICT_POST_BINDING.resolveModelAttribute(context, fromModel).asBoolean();

        idpType.setStrictPostBinding(strictPostBinding);

        String securityDomain = IdentityProviderResourceDefinition.SECURITY_DOMAIN.resolveModelAttribute(context, fromModel).asString();

        idpType.setSecurityDomain(securityDomain);

        ModelNode attributeManager = IdentityProviderResourceDefinition.ATTRIBUTE_MANAGER.resolveModelAttribute(context, fromModel);

        if (attributeManager.isDefined()) {
            idpType.setAttributeManager(attributeManager.asString());
        }

        ModelNode roleGenerator = IdentityProviderResourceDefinition.ROLE_GENERATOR.resolveModelAttribute(context, fromModel);

        if (roleGenerator.isDefined()) {
            idpType.setRoleGenerator(roleGenerator.asString());
        }

        return idpType;
    }

    /**
     * <p>
     * Converts a {@ModelNode} instance to a {@KeyProviderType} instance.
     * </p>
     *
     * @param model
     *
     * @return
     */
    public static KeyProviderType toKeyProviderType(OperationContext context, ModelNode model) throws OperationFailedException {
        KeyProviderType keyProviderType = new KeyProviderType();

        keyProviderType.setClassName(KeyStoreKeyManager.class.getName());

        keyProviderType.setSigningAlias(KeyProviderResourceDefinition.SIGN_KEY_ALIAS.resolveModelAttribute(context, model).asString());

        AuthPropertyType keyStoreURL = new AuthPropertyType();

        keyStoreURL.setKey("KeyStoreURL");
        keyStoreURL.setValue(KeyProviderResourceDefinition.URL.resolveModelAttribute(context, model).asString());

        keyProviderType.add(keyStoreURL);

        AuthPropertyType keyStorePass = new AuthPropertyType();

        keyStorePass.setKey("KeyStorePass");
        keyStorePass.setValue(KeyProviderResourceDefinition.PASSWD.resolveModelAttribute(context, model).asString());

        keyProviderType.add(keyStorePass);

        AuthPropertyType signingKeyPass = new AuthPropertyType();

        signingKeyPass.setKey("SigningKeyPass");
        signingKeyPass.setValue(KeyProviderResourceDefinition.SIGN_KEY_PASSWD.resolveModelAttribute(context, model).asString());

        keyProviderType.add(signingKeyPass);

        AuthPropertyType signingKeyAlias = new AuthPropertyType();

        signingKeyAlias.setKey("SigningKeyAlias");
        signingKeyAlias.setValue(KeyProviderResourceDefinition.SIGN_KEY_ALIAS.resolveModelAttribute(context, model).asString());

        keyProviderType.add(signingKeyAlias);

        return keyProviderType;
    }
}
