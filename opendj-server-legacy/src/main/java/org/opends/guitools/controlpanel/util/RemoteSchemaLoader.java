/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2009-2010 Sun Microsystems, Inc.
 * Portions Copyright 2013-2016 ForgeRock AS.
 */
package org.opends.guitools.controlpanel.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.forgerock.opendj.config.server.ConfigException;
import org.forgerock.opendj.ldap.ByteStringBuilder;
import org.forgerock.opendj.ldap.ResultCode;
import org.forgerock.opendj.ldap.schema.MatchingRule;
import org.forgerock.opendj.ldap.schema.MatchingRuleImpl;
import org.forgerock.opendj.ldap.schema.SchemaBuilder;
import org.opends.guitools.controlpanel.browser.BrowserController;
import org.opends.guitools.controlpanel.datamodel.CustomSearchResult;
import org.opends.server.api.AttributeSyntax;
import org.opends.server.config.ConfigConstants;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.ServerContext;
import org.opends.server.replication.plugin.HistoricalCsnOrderingMatchingRuleImpl;
import org.opends.server.schema.AciSyntax;
import org.opends.server.schema.SchemaConstants;
import org.opends.server.schema.SubtreeSpecificationSyntax;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.InitializationException;
import org.opends.server.types.Schema;

/** Class used to retrieve the schema from the schema files. */
public class RemoteSchemaLoader extends SchemaLoader
{
  private Schema schema;

  /**
   * In remote mode we cannot load the matching rules and syntaxes from local
   * configuration, so we should instead bootstrap them from the SDK's core schema.
   */
  public RemoteSchemaLoader()
  {
    matchingRulesToKeep.clear();
    syntaxesToKeep.clear();
    matchingRulesToKeep.addAll(org.forgerock.opendj.ldap.schema.Schema.getCoreSchema().getMatchingRules());
    syntaxesToKeep.addAll(org.forgerock.opendj.ldap.schema.Schema.getCoreSchema().getSyntaxes());
  }
  /**
   * Reads the schema.
   *
   * @param ctx
   *          the connection to be used to load the schema.
   * @throws NamingException
   *           if an error occurs reading the schema.
   * @throws DirectoryException
   *           if an error occurs parsing the schema.
   * @throws InitializationException
   *           if an error occurs finding the base schema.
   * @throws ConfigException
   *           if an error occurs loading the configuration required to use the
   *           schema classes.
   */
  public void readSchema(InitialLdapContext ctx) throws NamingException, DirectoryException, InitializationException,
      ConfigException
  {
    final String[] schemaAttrs = { ConfigConstants.ATTR_LDAP_SYNTAXES_LC, ConfigConstants.ATTR_ATTRIBUTE_TYPES_LC,
      ConfigConstants.ATTR_OBJECTCLASSES_LC };
    final SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
    searchControls.setReturningAttributes(schemaAttrs);
    final NamingEnumeration<SearchResult> srs = ctx.search(
        ConfigConstants.DN_DEFAULT_SCHEMA_ROOT, BrowserController.ALL_OBJECTS_FILTER, searchControls);
    SearchResult sr = null;
    try
    {
      while (srs.hasMore())
      {
        sr = srs.next();
      }
    }
    finally
    {
      srs.close();
    }

    final CustomSearchResult csr = new CustomSearchResult(sr, ConfigConstants.DN_DEFAULT_SCHEMA_ROOT);
    schema = getBaseSchema();
    // Add missing matching rules and attribute syntaxes to base schema to allow read of remote server schema
    // (see OPENDJ-1122 for more details)
    addMissingSyntaxesToBaseSchema(new AciSyntax(), new SubtreeSpecificationSyntax());
    addMissingMatchingRuleToBaseSchema("1.3.6.1.4.1.26027.1.4.4", "historicalCsnOrderingMatch",
        "1.3.6.1.4.1.1466.115.121.1.40", new HistoricalCsnOrderingMatchingRuleImpl());
    for (final String str : schemaAttrs)
    {
      registerSchemaAttr(csr, str);
    }
  }

  private void addMissingSyntaxesToBaseSchema(final AttributeSyntax<?>... syntaxes)
      throws DirectoryException, InitializationException, ConfigException
  {
    for (AttributeSyntax<?> syntax : syntaxes)
    {
      final ServerContext serverContext = DirectoryServer.getInstance().getServerContext();
      final org.forgerock.opendj.ldap.schema.Schema schemaNG = serverContext.getSchemaNG();
      if (!schemaNG.hasSyntax(syntax.getOID()))
      {
        syntax.initializeSyntax(null, serverContext);
      }
      schema.registerSyntax(syntax.getSDKSyntax(schemaNG), true);
    }
  }

  private void addMissingMatchingRuleToBaseSchema(final String oid, final String name, final String syntaxOID,
      final MatchingRuleImpl impl)
      throws InitializationException, ConfigException, DirectoryException
  {
    final org.forgerock.opendj.ldap.schema.Schema schemaNG = schema.getSchemaNG();
    final MatchingRule matchingRule;
    if (schemaNG.hasMatchingRule(name))
    {
      matchingRule = schemaNG.getMatchingRule(name);
    }
    else
    {
      matchingRule = new SchemaBuilder(schemaNG).buildMatchingRule(oid)
                                                                .names(name)
                                                                .syntaxOID(syntaxOID)
                                                                .implementation(impl)
                                                                .addToSchema().toSchema().getMatchingRule(oid);
    }
    schema.registerMatchingRules(Arrays.asList(matchingRule), true);
  }

  private void registerSchemaAttr(final CustomSearchResult csr, final String schemaAttr) throws DirectoryException
  {
    final Set<Object> remainingAttrs = new HashSet<>(csr.getAttributeValues(schemaAttr));
    if (schemaAttr.equals(ConfigConstants.ATTR_LDAP_SYNTAXES_LC))
    {
      registerSchemaLdapSyntaxDefinitions(remainingAttrs);
      return;
    }

    while (!remainingAttrs.isEmpty())
    {
      DirectoryException lastException = null;
      final Set<Object> registered = new HashSet<>();
      for (final Object definition : remainingAttrs)
      {
        final ByteStringBuilder sb = new ByteStringBuilder();
        sb.appendObject(definition);
        try
        {
          switch (schemaAttr)
          {
          case ConfigConstants.ATTR_ATTRIBUTE_TYPES_LC:
            schema.registerAttributeType(sb.toString(), null, true);
            break;
          case ConfigConstants.ATTR_OBJECTCLASSES_LC:
            schema.registerObjectClass(sb.toString(), null, true);
            break;
          }
          registered.add(definition);
        }
        catch (DirectoryException de)
        {
          lastException = de;
        }
      }
      if (registered.isEmpty())
      {
        throw lastException;
      }
      remainingAttrs.removeAll(registered);
    }
  }

  private void registerSchemaLdapSyntaxDefinitions(Set<Object> remainingAttrs) throws DirectoryException
  {
    for (final Object definition : remainingAttrs)
    {
      final ByteStringBuilder sb = new ByteStringBuilder();
      sb.appendObject(definition);
      if (definition.toString().contains(SchemaConstants.OID_OPENDS_SERVER_BASE))
      {
        try
        {
          schema.registerLdapSyntaxDescription(sb.toString(), true);
        }
        catch (DirectoryException e)
        {
          // Filter error code to ignore exceptions raised on description syntaxes.
          if (e.getResultCode() != ResultCode.UNWILLING_TO_PERFORM)
          {
            throw e;
          }
        }
      }
    }
  }

  /**
   * Returns the schema that was read.
   *
   * @return the schema that was read.
   */
  @Override
  public Schema getSchema()
  {
    return schema;
  }
}
