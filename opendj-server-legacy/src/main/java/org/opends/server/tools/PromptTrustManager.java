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
 * Copyright 2008-2010 Sun Microsystems, Inc.
 * Portions Copyright 2014-2016 ForgeRock AS.
 */
package org.opends.server.tools;

import static org.opends.messages.ToolMessages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.forgerock.i18n.LocalizableMessage;

/**
 * This class provides an implementation of an X.509 trust manager which will
 * interactively prompt the user (via the CLI) whether a given certificate
 * should be trusted.  It should only be used by interactive command-line tools,
 * since it will block until it gets a response from the user.
 * <BR><BR>
 * Note that this class is only intended for client-side use, and therefore may
 * not be used by a server to determine whether a client certificate is trusted.
 */
public class PromptTrustManager
       implements X509TrustManager
{
  /** The singleton trust manager array for this class. */
  private static TrustManager[] trustManagerArray =
       new TrustManager[] { new PromptTrustManager() };

  /** Creates a new instance of this prompt trust manager. */
  private PromptTrustManager()
  {
    // No implementation is required.
  }

  /**
   * Retrieves the trust manager array that should be used to initialize an SSL
   * context in cases where the user should be interactively prompted about
   * whether to trust the server certificate.
   *
   * @return  The trust manager array that should be used to initialize an SSL
   *          context in cases where the user should be interactively prompted
   *          about whether to trust the server certificate.
   */
  public static TrustManager[] getTrustManagers()
  {
    return trustManagerArray;
  }

  /**
   * Determines whether an SSL client with the provided certificate chain should
   * be trusted.  This implementation is not intended for server-side use, and
   * therefore this method will always throw an exception.
   *
   * @param  chain     The certificate chain for the SSL client.
   * @param  authType  The authentication type based on the client certificate.
   *
   * @throws  CertificateException  To indicate that the provided client
   *                                certificate is not trusted.
   */
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
         throws CertificateException
  {
    LocalizableMessage message = ERR_PROMPTTM_REJECTING_CLIENT_CERT.get();
    throw new CertificateException(message.toString());
  }

  /**
   * Determines whether an SSL server with the provided certificate chain should
   * be trusted.  In this case, the user will be interactively prompted as to
   * whether the certificate should be trusted.
   *
   * @param  chain     The certificate chain for the SSL server.
   * @param  authType  The key exchange algorithm used.
   *
   * @throws  CertificateException  If the user rejects the certificate.
   */
  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
         throws CertificateException
  {
    if (chain == null || chain.length == 0)
    {
      System.out.println(WARN_PROMPTTM_NO_SERVER_CERT_CHAIN.get());
    }
    else
    {
      Date currentDate   = new Date();
      Date notAfterDate  = chain[0].getNotAfter();
      Date notBeforeDate = chain[0].getNotBefore();

      if (currentDate.after(notAfterDate))
      {
        System.err.println(WARN_PROMPTTM_CERT_EXPIRED.get(notAfterDate));
      }
      else if (currentDate.before(notBeforeDate))
      {
        System.err.println(WARN_PROMPTTM_CERT_NOT_YET_VALID.get(notBeforeDate));
      }

      System.out.println(INFO_PROMPTTM_SERVER_CERT.get(
              chain[0].getSubjectDN().getName(),
              chain[0].getIssuerDN().getName(),
              notBeforeDate,
              notAfterDate));
    }

    LocalizableMessage prompt = INFO_PROMPTTM_YESNO_PROMPT.get();
    BufferedReader reader =
         new BufferedReader(new InputStreamReader(System.in));
    while (true)
    {
      try
      {
        System.out.print(prompt);
        String line = reader.readLine().toLowerCase();
        if (line.equalsIgnoreCase(
            INFO_PROMPT_YES_COMPLETE_ANSWER.get().toString()) ||
            line.equalsIgnoreCase(
            INFO_PROMPT_YES_FIRST_LETTER_ANSWER.get().toString()))
        {
          // Returning without an exception is sufficient to consider the
          // certificate trusted.
          return;
        }
        if (line.equalsIgnoreCase(
            INFO_PROMPT_NO_COMPLETE_ANSWER.get().toString()) ||
            line.equalsIgnoreCase(
            INFO_PROMPT_NO_FIRST_LETTER_ANSWER.get().toString()))
        {
          LocalizableMessage message = ERR_PROMPTTM_USER_REJECTED.get();
          throw new CertificateException(message.toString());
        }
      } catch (IOException ioe) {}

      System.out.println();
    }
  }

  /**
   * Retrieves the set of certificate authority certificates which are trusted
   * for authenticating peers.
   *
   * @return  An empty array, since we don't care what certificates are
   *          presented because we will always prompt the user.
   */
  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
