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
 * Copyright 2009 Sun Microsystems, Inc.
 * Portions Copyright 2014 ForgeRock AS.
 */

package org.forgerock.opendj.ldap;

import org.forgerock.opendj.ldap.responses.Result;

/**
 * Thrown when the result code returned in a Result indicates that the Request
 * was unsuccessful because of a connection failure.
 */
@SuppressWarnings("serial")
public class ConnectionException extends LdapException {
    ConnectionException(final Result result) {
        super(result);
    }
}
