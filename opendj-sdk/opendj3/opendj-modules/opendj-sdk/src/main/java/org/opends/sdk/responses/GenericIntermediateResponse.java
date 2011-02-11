/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2010 Sun Microsystems, Inc.
 */

package org.opends.sdk.responses;



import java.util.List;

import org.opends.sdk.ByteString;
import org.opends.sdk.DecodeException;
import org.opends.sdk.DecodeOptions;
import org.opends.sdk.controls.Control;
import org.opends.sdk.controls.ControlDecoder;



/**
 * A Generic Intermediate response provides a mechanism for communicating
 * unrecognized or unsupported Intermediate responses to the client.
 */
public interface GenericIntermediateResponse extends IntermediateResponse
{
  /**
   * {@inheritDoc}
   */
  GenericIntermediateResponse addControl(Control control)
      throws UnsupportedOperationException, NullPointerException;



  /**
   * {@inheritDoc}
   */
  <C extends Control> C getControl(ControlDecoder<C> decoder,
      DecodeOptions options) throws NullPointerException, DecodeException;



  /**
   * {@inheritDoc}
   */
  List<Control> getControls();



  /**
   * {@inheritDoc}
   */
  String getOID();



  /**
   * {@inheritDoc}
   */
  ByteString getValue();



  /**
   * {@inheritDoc}
   */
  boolean hasValue();



  /**
   * Sets the numeric OID, if any, associated with this intermediate response.
   *
   * @param oid
   *          The numeric OID associated with this intermediate response, or
   *          {@code null} if there is no value.
   * @return This generic intermediate response.
   * @throws UnsupportedOperationException
   *           If this intermediate response does not permit the response name
   *           to be set.
   */
  GenericIntermediateResponse setOID(String oid)
      throws UnsupportedOperationException;



  /**
   * Sets the value, if any, associated with this intermediate response. Its
   * format is defined by the specification of this intermediate response.
   *
   * @param bytes
   *          The value associated with this intermediate response, or {@code
   *          null} if there is no value.
   * @return This generic intermediate response.
   * @throws UnsupportedOperationException
   *           If this intermediate response does not permit the response value
   *           to be set.
   */
  GenericIntermediateResponse setValue(ByteString bytes)
      throws UnsupportedOperationException;

}
