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

package test.org.picketlink.as.subsystem.idm.integration;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.SimpleUser;

/**
 * @author Pedro Silva
 *
 */
@RunWith(Arquillian.class)
public class IdentityManagementConfigurationTestCase {
    
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(IdentityManagementConfigurationTestCase.class.getClassLoader().getResource("deployment/emf-jndi-persistence.xml"), "META-INF/persistence.xml")
                .addAsManifestResource(IdentityManagementConfigurationTestCase.class.getClassLoader().getResource("deployment/jboss-deployment-structure-idm.xml"), "jboss-deployment-structure.xml")
                .addClass(IdentityManagementConfigurationTestCase.class);

        System.out.println(deployment.toString(true));
        
        return deployment;
    }
    
    @Resource (mappedName="picketlink/DevIdentityManager")
    private IdentityManagerFactory devIdentityManagerFactory;
    
    @Resource (mappedName="picketlink/StagingIdentityManager")
    private IdentityManagerFactory stagingIdentityManagerFactory;

    @Resource (mappedName="picketlink/FileBasedCompleteIdentityManager")
    private IdentityManagerFactory fileCompleteIdentityManagerFactory;

    @Resource (mappedName="picketlink/FileBasedSimpleIdentityManager")
    private IdentityManagerFactory fileSimpleIdentityManagerFactory;

    @Test
    public void testDevIdentityManager() throws Exception {
        IdentityManager identityManager = this.devIdentityManagerFactory.createIdentityManager();
        
        SimpleUser user = new SimpleUser("jonhy");
        
        identityManager.add(user);
        
        Password password = new Password("mypassWd");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credentials);
        
        Assert.assertEquals(Status.VALID, credentials.getStatus());
    }
    
    @Test
    public void testStagingIdentityManager() throws Exception {
        IdentityManager identityManager = this.stagingIdentityManagerFactory.createIdentityManager();
        
        SimpleUser user = new SimpleUser("anne");
        
        identityManager.add(user);
        
        Password password = new Password("mypassWd");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credentials);
        
        Assert.assertEquals(Status.VALID, credentials.getStatus());
    }
    
    @Test
    public void testFileSimpleIdentityManager() throws Exception {
        IdentityManager identityManager = this.fileSimpleIdentityManagerFactory.createIdentityManager();
        
        SimpleUser user = new SimpleUser("peter");
        
        identityManager.add(user);
        
        Password password = new Password("mypassWd");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credentials);
        
        Assert.assertEquals(Status.VALID, credentials.getStatus());
    }
    
    @Test
    public void testFileCompleteIdentityManager() throws Exception {
        IdentityManager identityManager = this.fileCompleteIdentityManagerFactory.createIdentityManager();
        
        SimpleUser user = new SimpleUser("mark");
        
        identityManager.add(user);
        
        Password password = new Password("mypassWd");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credentials);
        
        Assert.assertEquals(Status.VALID, credentials.getStatus());
    }

}