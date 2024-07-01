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
 * Copyright 2006-2008 Sun Microsystems, Inc.
 * Portions Copyright 2011-2015 ForgeRock AS.
 */
package org.opends.server.protocols.ldap;

import java.util.ArrayList;

import org.forgerock.opendj.io.ASN1;
import org.forgerock.opendj.io.ASN1Reader;
import org.forgerock.opendj.io.ASN1Writer;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.ByteStringBuilder;
import org.opends.server.TestCaseUtils;
import org.opends.server.types.LDAPException;
import org.opends.server.types.RawFilter;
import org.opends.server.types.SearchFilter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.opends.server.util.CollectionUtils.*;
import static org.testng.Assert.*;

public class TestLDAPFilter extends LdapTestCase
{
  @BeforeTest
  public void setup() throws Exception
  {
    TestCaseUtils.startServer();
  }
  @DataProvider(name="badfilterstrings")
  public Object[][] getBadFilterStrings() throws Exception
  {
    return new Object[][]
    {
      { null, null },
      { "", null },
      { "=", null },
      { "()", null },
      { "(&(objectClass=*)(sn=s*s)", null },
      { "(dob>12221)", null },
      { "(cn=bob\\2 doe)", null },
      { "(cn=\\4j\\w2\\yu)", null },
      { "(cn=ds\\2)", null },
      { "(&(givenname=bob)|(sn=pep)dob=12))", null },
      { "(:=bob)", null },
      { "(=sally)", null },
      { "(cn=billy bob", null },
      { "(|(!(title=sweep*)(l=Paris*)))", null },
      { "(|(!))", null },
      { "((uid=user.0))", null },
      { "(&&(uid=user.0))", null },
      { "!uid=user.0", null },
      { "(:dn:=Sally)", null },

    };
  }
  @DataProvider(name="filterstrings")
  public Object[][] getFilterStrings() throws Exception
  {
    LDAPFilter equal = LDAPFilter.createEqualityFilter("objectClass",
                                        ByteString.valueOfUtf8("\\test*(Value)"));
    LDAPFilter equal2 = LDAPFilter.createEqualityFilter("objectClass",
                                                      ByteString.valueOfUtf8(""));
    LDAPFilter approx = LDAPFilter.createApproximateFilter("sn",
                                        ByteString.valueOfUtf8("\\test*(Value)"));
    LDAPFilter greater = LDAPFilter.createGreaterOrEqualFilter("employeeNumber",
                                        ByteString.valueOfUtf8("\\test*(Value)"));
    LDAPFilter less = LDAPFilter.createLessOrEqualFilter("dob",
                                        ByteString.valueOfUtf8("\\test*(Value)"));
    LDAPFilter presense = LDAPFilter.createPresenceFilter("login");

    ArrayList<ByteString> any = new ArrayList<>(0);
    ArrayList<ByteString> multiAny = newArrayList(ByteString.valueOfUtf8("\\wid*(get)"), ByteString.valueOfUtf8("*"));

    LDAPFilter substring1 = LDAPFilter.createSubstringFilter("givenName",
                                                 ByteString.valueOfUtf8("\\Jo*()"),
                                                      any,
                                                 ByteString.valueOfUtf8("\\n*()"));
    LDAPFilter substring2 = LDAPFilter.createSubstringFilter("givenName",
                                                 ByteString.valueOfUtf8("\\Jo*()"),
                                                      multiAny,
                                                 ByteString.valueOfUtf8("\\n*()"));
    LDAPFilter substring3 = LDAPFilter.createSubstringFilter("givenName",
                                                      ByteString.valueOfUtf8(""),
                                                      any,
                                                 ByteString.valueOfUtf8("\\n*()"));
    LDAPFilter substring4 = LDAPFilter.createSubstringFilter("givenName",
                                                 ByteString.valueOfUtf8("\\Jo*()"),
                                                      any,
                                                      ByteString.valueOfUtf8(""));
    LDAPFilter substring5 = LDAPFilter.createSubstringFilter("givenName",
                                                      ByteString.valueOfUtf8(""),
                                                      multiAny,
                                                      ByteString.valueOfUtf8(""));
    LDAPFilter extensible1 = LDAPFilter.createExtensibleFilter("2.4.6.8.19",
                                                "cn",
                                           ByteString.valueOfUtf8("\\John* (Doe)"),
                                                false);
    LDAPFilter extensible2 = LDAPFilter.createExtensibleFilter("2.4.6.8.19",
                                                "cn",
                                           ByteString.valueOfUtf8("\\John* (Doe)"),
                                                true);
    LDAPFilter extensible3 = LDAPFilter.createExtensibleFilter("2.4.6.8.19",
                                                null,
                                           ByteString.valueOfUtf8("\\John* (Doe)"),
                                                true);
    LDAPFilter extensible4 = LDAPFilter.createExtensibleFilter(null,
                                                "cn",
                                           ByteString.valueOfUtf8("\\John* (Doe)"),
                                                true);
    LDAPFilter extensible5 = LDAPFilter.createExtensibleFilter("2.4.6.8.19",
                                                null,
                                           ByteString.valueOfUtf8("\\John* (Doe)"),
                                                false);

    ArrayList<RawFilter> list1 = new ArrayList<>();
    list1.add(equal);
    list1.add(approx);

    LDAPFilter and = LDAPFilter.createANDFilter(list1);

    ArrayList<RawFilter> list2 = new ArrayList<>();
    list2.add(substring1);
    list2.add(extensible1);
    list2.add(and);

    return new Object[][]
    {
        { "(objectClass=\\5ctest\\2a\\28Value\\29)", equal },

        { "(objectClass=)", equal2 },

        { "(sn~=\\5ctest\\2a\\28Value\\29)", approx },

        { "(employeeNumber>=\\5ctest\\2a\\28Value\\29)", greater },

        { "(dob<=\\5ctest\\2a\\28Value\\29)", less },

        { "(login=*)", presense },

        { "(givenName=\\5cJo\\2a\\28\\29*\\5cn\\2a\\28\\29)", substring1 },

        { "(givenName=\\5cJo\\2a\\28\\29*\\5cwid\\2a\\28get\\29*\\2a*\\5cn\\2a\\28\\29)", substring2 },

        { "(givenName=*\\5cn\\2a\\28\\29)", substring3 },

        { "(givenName=\\5cJo\\2a\\28\\29*)", substring4 },

        { "(givenName=*\\5cwid\\2a\\28get\\29*\\2a*)", substring5 },

        { "(cn:2.4.6.8.19:=\\5cJohn\\2a \\28Doe\\29)", extensible1 },

        { "(cn:dn:2.4.6.8.19:=\\5cJohn\\2a \\28Doe\\29)", extensible2 },

        { "(:dn:2.4.6.8.19:=\\5cJohn\\2a \\28Doe\\29)", extensible3 },

        { "(cn:dn:=\\5cJohn\\2a \\28Doe\\29)", extensible4 },

        { "(:2.4.6.8.19:=\\5cJohn\\2a \\28Doe\\29)", extensible5 },

        { "(&(objectClass=\\5ctest\\2a\\28Value\\29)(sn~=\\5ctest\\2a\\28Value\\29))",
            LDAPFilter.createANDFilter(list1) },

        { "(|(objectClass=\\5ctest\\2a\\28Value\\29)(sn~=\\5ctest\\2a\\28Value\\29))",
            LDAPFilter.createORFilter(list1) },

        { "(!(objectClass=\\5ctest\\2a\\28Value\\29))", LDAPFilter.createNOTFilter(equal) },

        { "(|(givenName=\\5cJo\\2a\\28\\29*\\5cn\\2a\\28\\29)(cn:2.4.6.8.19:=\\5cJohn\\2a \\28Doe\\29)" +
            "(&(objectClass=\\5ctest\\2a\\28Value\\29)(sn~=\\5ctest\\2a\\28Value\\29)))",
            LDAPFilter.createORFilter(list2) },

        // OpenDJ issue 23.
        {
            "(ds-sync-conflict=uid=\\5c+3904211775265,ou=SharedAddressBook,cn=1038372,dc=cab)",
            LDAPFilter.createEqualityFilter("ds-sync-conflict",
                    ByteString.valueOfUtf8("uid=\\+3904211775265,ou=SharedAddressBook,cn=1038372,dc=cab")) },

      // OPENDJ-735
      { "(&)", LDAPFilter.createANDFilter(new ArrayList<RawFilter>()) },

      // OPENDJ-735
      { "(|)", LDAPFilter.createORFilter(new ArrayList<RawFilter>()) }
    };
  }

  @Test(dataProvider = "filterstrings")
  public void testDecode(String filterStr, LDAPFilter filter) throws Exception
  {
    LDAPFilter decoded = LDAPFilter.decode(filterStr);
    assertEquals(decoded.toString(), filter.toString());
    assertEquals(decoded.getAssertionValue(), filter.getAssertionValue());
    assertEquals(decoded.getAttributeType(), filter.getAttributeType());
    assertEquals(decoded.getDNAttributes(), filter.getDNAttributes());
    if(decoded.getFilterComponents() != null || filter.getFilterComponents() != null)
    {
      assertEquals(decoded.getFilterComponents().toString(), filter.getFilterComponents().toString());
    }
    assertEquals(decoded.getFilterType(), filter.getFilterType());
    assertEquals(decoded.getMatchingRuleID(), filter.getMatchingRuleID());
    if(decoded.getNOTComponent() != null || filter.getNOTComponent() != null)
    {
      assertEquals(decoded.getNOTComponent().toString(), filter.getNOTComponent().toString());
    }
    if(isNotEmpty(decoded.getSubAnyElements()) || isNotEmpty(filter.getSubAnyElements()))
    {
      assertEquals(decoded.getSubAnyElements(), filter.getSubAnyElements());
    }
    if(isNotEmpty(decoded.getSubFinalElement()) || isNotEmpty(filter.getSubFinalElement()))
    {
      assertEquals(decoded.getSubFinalElement(), filter.getSubFinalElement());
    }
    if(isNotEmpty(decoded.getSubInitialElement()) || isNotEmpty(filter.getSubInitialElement()))
    {
      assertEquals(decoded.getSubInitialElement(), filter.getSubInitialElement());
    }
  }

  private boolean isNotEmpty(ByteString bs)
  {
    return bs != null && !bs.toString().equals("");
  }

  private boolean isNotEmpty(ArrayList<ByteString> col)
  {
    return col != null && !col.isEmpty();
  }

  @Test(dataProvider = "badfilterstrings", expectedExceptions = LDAPException.class)
  public void testDecodeException (String filterStr, LDAPFilter filter) throws Exception
  {
    LDAPFilter.decode(filterStr);
  }

  @Test
  public void testToSearchFilter() throws Exception
  {
    LDAPFilter filter = LDAPFilter.decode(
        "(&" +
          "(cn>=*)" +
          "(:2.5.13.2:=Bob)" +
          "(cn:=Jane)" +
          "(|" +
            "(sn<=gh*sh*sl)" +
            "(!(cn:dn:2.5.13.5:=Sally))" +
            "(cn~=blvd)" +
            "(cn=*)" +
          ")" +
          "(cn=*n)" +
          "(cn=n*)" +
          "(cn=n*n)" +
          "(:dn:1.3.6.1.4.1.1466.109.114.1:=Doe)" +
          "(cn:2.5.13.2:=)" +
        ")");

    SearchFilter searchFilter = filter.toSearchFilter();
    LDAPFilter newFilter = new LDAPFilter(searchFilter);
    assertEquals(filter.toString(), newFilter.toString());
  }

  @Test(dataProvider = "filterstrings")
  public void testEncodeDecode(String filterStr, LDAPFilter filter) throws Exception
  {
    ByteStringBuilder builder = new ByteStringBuilder();
    ASN1Writer writer = ASN1.getWriter(builder);
    filter.write(writer);

    ASN1Reader reader = ASN1.getReader(builder.toByteString());
    assertEquals(LDAPFilter.decode(reader).toString(), filter.toString());
  }

  @Test
  public void testEncodeDecodeComplex() throws Exception
  {
    LDAPFilter filter = LDAPFilter.decode(
        "(&" +
          "(cn>=*)" +
          "(:1.2.3.4:=Bob)" +
          "(cn:=Jane)" +
          "(|" +
            "(sn<=gh*sh*sl)" +
            "(!(cn:dn:2.4.6.8.19:=Sally))" +
            "(cn~=blvd)" +
            "(cn=*)" +
          ")" +
          "(cn=*n)" +
          "(cn=n*)" +
          "(cn=n*n)" +
          "(:dn:1.2.3.4:=Doe)" +
          "(cn:2.4.6.8.10:=)" +
        ")");

    ByteStringBuilder builder = new ByteStringBuilder();
    ASN1Writer writer = ASN1.getWriter(builder);
    filter.write(writer);

    ASN1Reader reader = ASN1.getReader(builder.toByteString());
    assertEquals(LDAPFilter.decode(reader).toString(), filter.toString());
  }
}
