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
 * Copyright 2014 ForgeRock AS.
 */
package org.opends.server.schema;

import org.opends.legacy.DummyByteArrayComparator;

/**
 * Required by rebuild-index process when upgrading to OpenDJ3.
 *
 * @deprecated since OPENDJ-1591 Migrate matching rules
 */
@Deprecated
public class UUIDEqualityMatchingRule extends DummyByteArrayComparator {
    // empty
}
