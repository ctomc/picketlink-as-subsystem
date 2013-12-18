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

package org.picketlink.as.subsystem.idm.model;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.picketlink.as.subsystem.PicketLinkMessages;
import org.picketlink.as.subsystem.idm.config.JPAStoreSubsystemConfiguration;
import org.picketlink.as.subsystem.idm.config.JPAStoreSubsystemConfigurationBuilder;
import org.picketlink.as.subsystem.idm.service.PartitionManagerService;
import org.picketlink.as.subsystem.model.ModelElement;
import org.picketlink.idm.config.FileStoreConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;
import org.picketlink.idm.config.LDAPMappingConfigurationBuilder;
import org.picketlink.idm.config.LDAPStoreConfigurationBuilder;
import org.picketlink.idm.config.NamedIdentityConfigurationBuilder;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;

import java.util.Set;


/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @since Mar 16, 2012
 */
public class IdentityManagementConfiguration {

    public static IdentityStoreConfigurationBuilder configureStore(OperationContext context, String storeType, ResourceEntry resource, final NamedIdentityConfigurationBuilder builder, final PartitionManagerService partitionManagerService) throws OperationFailedException {
        IdentityStoreConfigurationBuilder storeConfig = null;
        ModelNode modelNode = resource.getModel();
        ModelNode alternativeModuleNode = modelNode.get(ModelElement.COMMON_MODULE.getName());
        Module alternativeModule = null;

        if (alternativeModuleNode.isDefined()) {
            ModuleLoader moduleLoader = Module.getContextModuleLoader();
            try {
                alternativeModule = moduleLoader.loadModule(ModuleIdentifier.create(alternativeModuleNode.asString()));
            } catch (ModuleLoadException e) {
                throw new IllegalStateException("Could not load module [" + alternativeModuleNode.asString() + "].");
            }
        } else {
            alternativeModule = Module.getCallerModule();
        }

        if (storeType.equals(ModelElement.JPA_STORE.getName())) {
            storeConfig = configureJPAIdentityStore(context, resource, builder, partitionManagerService);
        } else if (storeType.equals(ModelElement.FILE_STORE.getName())) {
            storeConfig = configureFileIdentityStore(context, resource, builder);
        } else if (storeType.equals(ModelElement.LDAP_STORE.getName())) {
            storeConfig = configureLDAPIdentityStore(context, alternativeModule, resource, builder);
        } else {
            throw PicketLinkMessages.MESSAGES.idmNoConfigurationProvided();
        }

        ModelNode supportAttributeNode = modelNode.get(ModelElement.IDENTITY_STORE_SUPPORT_ATTRIBUTE.getName());

        storeConfig.supportAttributes(true);

        if (supportAttributeNode.isDefined()) {
            storeConfig.supportAttributes(supportAttributeNode.asBoolean());
        }

        ModelNode supportCredentialNode = modelNode.get(ModelElement.IDENTITY_STORE_SUPPORT_CREDENTIAL.getName());

        storeConfig.supportCredentials(true);

        if (supportCredentialNode.isDefined()) {
            storeConfig.supportCredentials(supportCredentialNode.asBoolean());
        }

        Set<ResourceEntry> featuresSetEntries = resource.getChildren(ModelElement.SUPPORTED_TYPES.getName());

        if (featuresSetEntries != null && !featuresSetEntries.isEmpty()) {
            ResourceEntry featuresSet = featuresSetEntries.iterator().next();

            configureAllFeatures(featuresSet.getModel(), storeConfig);

            Set<ResourceEntry> featuresList = featuresSet.getChildren(ModelElement.SUPPORTED_TYPE.getName());

            for (ResourceEntry feature : featuresList) {
                String typeName = feature.getModel().get(ModelElement.COMMON_CLASS.getName()).asString();

                try {
                    Class<? extends AttributedType> attributedTypeClass = (Class<? extends AttributedType>) loadClass(alternativeModule, typeName);

                    if (Relationship.class.isAssignableFrom(attributedTypeClass)) {
                        storeConfig.supportGlobalRelationship((Class<? extends Relationship>) attributedTypeClass);
                    } else {
                        storeConfig.supportType(attributedTypeClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not find type [" + typeName + "].", e);
                }
            }
        }

        Set<ResourceEntry> featuresList = resource.getChildren(ModelElement.IDENTITY_STORE_CREDENTIAL_HANDLER.getName());

        for (ResourceEntry feature : featuresList) {
            String typeName = feature.getModel().get(ModelElement.COMMON_CLASS.getName()).asString();

            try {
                storeConfig.addCredentialHandler(loadClass(alternativeModule, typeName));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find type [" + typeName + "].", e);
            }
        }

        return storeConfig;
    }

    private static JPAStoreSubsystemConfigurationBuilder configureJPAIdentityStore(OperationContext context,
                                                                                   final ResourceEntry resource,
                                                                                   final NamedIdentityConfigurationBuilder builder,
                                                                                   final PartitionManagerService partitionManagerService) throws OperationFailedException {
        JPAStoreSubsystemConfigurationBuilder storeConfig = builder.stores().add(JPAStoreSubsystemConfiguration.class, JPAStoreSubsystemConfigurationBuilder.class);

        ModelNode jpaDataSourceNode = JPAStoreResourceDefinition.DATA_SOURCE.resolveModelAttribute(context, resource.getModel());
        ModelNode jpaEntityModule = JPAStoreResourceDefinition.ENTITY_MODULE.resolveModelAttribute(context, resource.getModel());
        ModelNode jpaEntityModuleUnitName = JPAStoreResourceDefinition.ENTITY_MODULE_UNIT_NAME.resolveModelAttribute(context, resource.getModel());
        ModelNode jpaEntityManagerFactoryNode = JPAStoreResourceDefinition.ENTITY_MANAGER_FACTORY.resolveModelAttribute(context, resource.getModel());

        if (jpaEntityModule.isDefined()) {
            storeConfig.entityModule(jpaEntityModule.asString());
        }

        if (jpaEntityModuleUnitName.isDefined()) {
            storeConfig.entityModuleUnitName(jpaEntityModuleUnitName.asString());
        }

        if (jpaDataSourceNode.isDefined()) {
            storeConfig.dataSourceJndiUrl(toJndiName(jpaDataSourceNode.asString()));
        }

        if (jpaEntityManagerFactoryNode.isDefined()) {
            storeConfig.entityManagerFactoryJndiName(jpaEntityManagerFactoryNode.asString());
        }

        storeConfig.transactionManager(partitionManagerService.getTransactionManager());

        return storeConfig;
    }

    private static Class<?> loadClass(final Module module, final String typeName) throws ClassNotFoundException {
        if (module != null) {
            return (Class<?>) module.getClassLoader().loadClass(typeName);
        }

        return (Class<?>) Class.forName(typeName);
    }

    private static void configureAllFeatures(ModelNode operation, IdentityStoreConfigurationBuilder storeConfig) {
        ModelNode supportsAll = operation.get(ModelElement.COMMON_SUPPORTS_ALL.getName());

        if (supportsAll.isDefined() && supportsAll.asBoolean()) {
            storeConfig.supportAllFeatures();
        }
    }

    private static IdentityStoreConfigurationBuilder configureFileIdentityStore(OperationContext context, ResourceEntry resource, final NamedIdentityConfigurationBuilder builder) throws OperationFailedException {
        ModelNode modelNode = resource.getModel();
        FileStoreConfigurationBuilder fileStoreBuilder = builder.stores().file();

        ModelNode workingDir = FileStoreResourceDefinition.WORKING_DIR.resolveModelAttribute(context, resource.getModel());
        ModelNode alwaysCreateFiles = FileStoreResourceDefinition.ALWAYS_CREATE_FILE.resolveModelAttribute(context, resource.getModel());
        ModelNode asyncWrite = FileStoreResourceDefinition.ASYNC_WRITE.resolveModelAttribute(context, resource.getModel());
        ModelNode asyncWriteThreadPool = FileStoreResourceDefinition.ASYNC_WRITE_THREAD_POOL.resolveModelAttribute(context, resource.getModel());

        if (workingDir.isDefined()) {
            fileStoreBuilder.workingDirectory(workingDir.asString());
        }

        if (alwaysCreateFiles.isDefined()) {
            fileStoreBuilder.preserveState(!alwaysCreateFiles.asBoolean());
        }

        if (asyncWrite.isDefined()) {
            fileStoreBuilder.asyncWrite(asyncWrite.asBoolean());
        }

        if (asyncWriteThreadPool.isDefined()) {
            fileStoreBuilder.asyncWriteThreadPool(asyncWriteThreadPool.asInt());
        }

        return fileStoreBuilder;
    }

    private static LDAPStoreConfigurationBuilder configureLDAPIdentityStore(OperationContext context, Module alternativeModule, ResourceEntry resource, NamedIdentityConfigurationBuilder builder) throws OperationFailedException {
        LDAPStoreConfigurationBuilder storeConfig = builder.stores().ldap();
        ModelNode modelNode = resource.getModel();
        ModelNode url = LDAPStoreResourceDefinition.URL.resolveModelAttribute(context, modelNode);
        ModelNode bindDn = LDAPStoreResourceDefinition.BIND_DN.resolveModelAttribute(context, modelNode);
        ModelNode bindCredential = LDAPStoreResourceDefinition.BIND_CREDENTIAL.resolveModelAttribute(context, modelNode);
        ModelNode baseDn = LDAPStoreResourceDefinition.BASE_DN_SUFFIX.resolveModelAttribute(context, modelNode);

        if (url.isDefined()) {
            storeConfig.url(url.asString());
        }

        if (bindDn.isDefined()) {
            storeConfig.bindDN(bindDn.asString());
        }

        if (bindCredential.isDefined()) {
            storeConfig.bindCredential(bindCredential.asString());
        }

        if (baseDn.isDefined()) {
            storeConfig.baseDN(baseDn.asString());
        }

        Set<ResourceEntry> mappings = resource.getChildren(ModelElement.LDAP_STORE_MAPPING.getName());

        for (ResourceEntry mapping : mappings) {
            ModelNode mappingModelNode = mapping.getModel();
            String mappingClass = LDAPStoreMappingResourceDefinition.CLASS.resolveModelAttribute(context, mappingModelNode).asString();
            LDAPMappingConfigurationBuilder storeMapping;

            try {
                storeMapping = storeConfig.mapping((Class<? extends AttributedType>) loadClass(alternativeModule, mappingClass));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not load LDAP mapped class [" + mappingClass + "].", e);
            }

            ModelNode relatesTo = LDAPStoreMappingResourceDefinition.RELATES_TO.resolveModelAttribute(context, mappingModelNode);

            if (relatesTo.isDefined()) {
                try {
                    storeMapping.forMapping((Class<? extends AttributedType>) loadClass(alternativeModule, relatesTo.asString()));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not load LDAP mapped class [" + mappingClass + "].", e);
                }
            } else {
                String baseDN = LDAPStoreMappingResourceDefinition.BASE_DN.resolveModelAttribute(context, mappingModelNode).asString();

                storeMapping.baseDN(baseDN);

                String objectClasses = LDAPStoreMappingResourceDefinition.OBJECT_CLASSES.resolveModelAttribute(context, mappingModelNode).asString();

                for (String objClass: objectClasses.split(",")) {
                    if (!objClass.trim().isEmpty()) {
                        storeMapping.objectClasses(objClass);
                    }
                }

                ModelNode parentAttributeName = LDAPStoreMappingResourceDefinition.PARENT_ATTRIBUTE.resolveModelAttribute(context, mappingModelNode);

                if (parentAttributeName.isDefined()) {
                    storeMapping.parentMembershipAttributeName(parentAttributeName.asString());
                }
            }

            Set<ResourceEntry> attributes = mapping.getChildren(ModelElement.LDAP_STORE_ATTRIBUTE.getName());

            for (ResourceEntry attribute : attributes) {
                ModelNode attributeModel = attribute.getModel();

                String name = attributeModel.get(ModelElement.LDAP_STORE_ATTRIBUTE_NAME.getName()).asString();

                String ldapName = LDAPStoreAttributeResourceDefinition.LDAP_NAME.resolveModelAttribute(context, attributeModel).asString();

                ModelNode readOnlyModelNode = LDAPStoreAttributeResourceDefinition.READ_ONLY.resolveModelAttribute(context, attributeModel);

                if (readOnlyModelNode.isDefined() && readOnlyModelNode.asBoolean()) {
                    storeMapping.readOnlyAttribute(name, ldapName);
                } else {
                    ModelNode identifierModelNode = LDAPStoreAttributeResourceDefinition.IS_IDENTIFIER.resolveModelAttribute(context, attributeModel);
                    boolean isIdentifier = false;

                    if (identifierModelNode.isDefined()) {
                        isIdentifier = identifierModelNode.asBoolean();
                    }

                    storeMapping.attribute(name, ldapName, isIdentifier);
                }
            }
        }

        return storeConfig;
    }

    public static String toJndiName(String jndiName) {
        if (jndiName != null) {
            if (jndiName.startsWith("java:")) {
                jndiName = jndiName.substring(jndiName.indexOf(":") + 1);
            }
        }

        return jndiName;
    }
}