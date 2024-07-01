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
 * Copyright 2006-2009 Sun Microsystems, Inc.
 * Portions Copyright 2014-2015 ForgeRock AS.
 */
package org.opends.server.types;

import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.ByteStringBuilder;

import java.io.InputStream;
import java.io.IOException;

/**
 * A wrapper InputStream that will record all reads from an underlying
 * InputStream. The recorded bytes will append to any previous
 * recorded bytes until the clear method is called.
 */
public class RecordingInputStream extends InputStream
{
  private boolean enableRecording;
  private InputStream parentStream;
  private ByteStringBuilder buffer;

  /**
   * Constructs a new RecordingInputStream that will record all reads
   * from the given input stream.
   *
   * @param parentStream The input stream to record.
   */
  public RecordingInputStream(InputStream parentStream)
  {
    this.enableRecording = false;
    this.parentStream = parentStream;
    this.buffer = new ByteStringBuilder(32);
  }

  /** {@inheritDoc} */
  @Override
  public int read() throws IOException {
    int readByte = parentStream.read();
    if(enableRecording)
    {
      buffer.appendByte(readByte);
    }
    return readByte;
  }

  /** {@inheritDoc} */
  @Override
  public int read(byte[] bytes) throws IOException {
    int bytesRead = parentStream.read(bytes);
    if(enableRecording)
    {
      buffer.appendBytes(bytes, 0, bytesRead);
    }
    return bytesRead;
  }

  /** {@inheritDoc} */
  @Override
  public int read(byte[] bytes, int i, int i1) throws IOException {
    int bytesRead = parentStream.read(bytes, i, i1);
    if(enableRecording)
    {
      buffer.appendBytes(bytes, i, bytesRead);
    }
    return bytesRead;
  }

  /** {@inheritDoc} */
  @Override
  public long skip(long l) throws IOException {
    return parentStream.skip(l);
  }

  /** {@inheritDoc} */
  @Override
  public int available() throws IOException {
    return parentStream.available();
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    parentStream.close();
  }

  /** {@inheritDoc} */
  @Override
  public void mark(int i) {
    parentStream.mark(i);
  }

  /** {@inheritDoc} */
  @Override
  public void reset() throws IOException {
    parentStream.reset();
  }

  /** {@inheritDoc} */
  @Override
  public boolean markSupported() {
    return parentStream.markSupported();
  }

  /**
   * Retrieve the bytes read from this input stream since the last
   * clear.
   *
   * @return the bytes read from this input stream since the last
   *         clear.
   */
  public ByteString getRecordedBytes() {
    return buffer.toByteString();
  }

  /**
   * Clear the bytes currently recorded by this input stream.
   */
  public void clearRecordedBytes() {
    buffer.clear();
  }

  /**
   * Retrieves whether recording is enabled.
   *
   * @return whether recording is enabled.
   */
  public boolean isRecordingEnabled()
  {
    return enableRecording;
  }

  /**
   * Set whether if this input stream is recording all reads or not.
   *
   * @param enabled <code>true</code> to recording all reads or
   *                <code>false</code> otherwise.
   */
  public void setRecordingEnabled(boolean enabled)
  {
    this.enableRecording = enabled;
  }
}
