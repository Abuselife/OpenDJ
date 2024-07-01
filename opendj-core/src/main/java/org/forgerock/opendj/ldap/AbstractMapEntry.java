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
 * Portions copyright 2012-2016 ForgeRock AS.
 */
package org.forgerock.opendj.ldap;

import java.util.Collection;
import java.util.Map;

import org.forgerock.util.Reject;

/** Abstract implementation for {@code Map} based entries. */
abstract class AbstractMapEntry extends AbstractEntry {
    private final Map<AttributeDescription, Attribute> attributes;
    private DN name;

    /**
     * Creates an empty entry using the provided distinguished name and
     * {@code Map}.
     *
     * @param name
     *            The distinguished name of this entry.
     * @param attributes
     *            The attribute map.
     */
    AbstractMapEntry(final DN name, final Map<AttributeDescription, Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public final boolean addAttribute(final Attribute attribute,
            final Collection<? super ByteString> duplicateValues) {
        final AttributeDescription attributeDescription = attribute.getAttributeDescription();
        final Attribute oldAttribute = getAttribute(attributeDescription);
        if (oldAttribute != null) {
            return oldAttribute.addAll(attribute, duplicateValues);
        } else {
            attributes.put(attributeDescription, attribute);
            return true;
        }
    }

    @Override
    public final Entry clearAttributes() {
        attributes.clear();
        return this;
    }

    @Override
    public final Iterable<Attribute> getAllAttributes() {
        return attributes.values();
    }

    @Override
    public final Attribute getAttribute(final AttributeDescription attributeDescription) {
        final Attribute attribute = attributes.get(attributeDescription);
        if (attribute == null && attributeDescription.isPlaceHolder()) {
            // Fall-back to inefficient search using place-holder.
            return super.getAttribute(attributeDescription);
        } else {
            return attribute;
        }
    }

    @Override
    public final int getAttributeCount() {
        return attributes.size();
    }

    @Override
    public final DN getName() {
        return name;
    }

    @Override
    public final boolean removeAttribute(final Attribute attribute,
            final Collection<? super ByteString> missingValues) {
        final AttributeDescription attributeDescription = attribute.getAttributeDescription();
        if (attribute.isEmpty()) {
            return attributes.remove(attributeDescription) != null
                || (attributeDescription.isPlaceHolder()
                    // Fall-back to inefficient remove using place-holder.
                    && super.removeAttribute(attribute, missingValues));
        } else {
            final Attribute oldAttribute = getAttribute(attributeDescription);
            if (oldAttribute != null) {
                final boolean modified = oldAttribute.removeAll(attribute, missingValues);
                if (oldAttribute.isEmpty()) {
                    // Use old attribute's description in case it is different
                    // (e.g. this may be the case when using place-holders).
                    attributes.remove(oldAttribute.getAttributeDescription());
                    return true;
                }
                return modified;
            } else {
                if (missingValues != null) {
                    missingValues.addAll(attribute);
                }
                return false;
            }
        }
    }

    @Override
    public final Entry setName(final DN dn) {
        Reject.ifNull(dn);
        this.name = dn;
        return this;
    }
}
