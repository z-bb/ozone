/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.ozone.common;

import org.apache.hadoop.util.PureJavaCrc32;
import org.apache.hadoop.util.PureJavaCrc32C;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.RandomUtils;
import java.util.zip.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link ChecksumByteBuffer} implementations.
 */
public class TestChecksumByteBuffer {
  @Test
  public void testPureJavaCrc32ByteBuffer() {
    final Checksum expected = new PureJavaCrc32();
    final ChecksumByteBuffer testee = ChecksumByteBufferFactory.crc32Impl();
    new VerifyChecksumByteBuffer(expected, testee).testCorrectness();
  }

  @Test
  public void testPureJavaCrc32CByteBuffer() {
    final Checksum expected = new PureJavaCrc32C();
    final ChecksumByteBuffer testee = ChecksumByteBufferFactory.crc32CImpl();
    new VerifyChecksumByteBuffer(expected, testee).testCorrectness();
  }

  static class VerifyChecksumByteBuffer {
    private final Checksum expected;
    private final ChecksumByteBuffer testee;

    VerifyChecksumByteBuffer(Checksum expected, ChecksumByteBuffer testee) {
      this.expected = expected;
      this.testee = testee;
    }

    void testCorrectness() {
      checkSame();

      checkBytes("hello world!".getBytes(StandardCharsets.UTF_8));

      final int len = 1 << 10;
      for (int i = 0; i < 1000; i++) {
        checkBytes(RandomUtils.nextBytes(len), RandomUtils.nextInt(0, len));
      }
    }

    void checkBytes(byte[] bytes) {
      checkBytes(bytes, bytes.length);
    }

    void checkBytes(byte[] bytes, int length) {
      expected.reset();
      testee.reset();
      checkSame();

      for (byte b : bytes) {
        expected.update(b);
        testee.update(b);
        checkSame();
      }

      expected.reset();
      testee.reset();

      for (int i = 0; i < length; i++) {
        expected.update(bytes, 0, i);
        testee.update(bytes, 0, i);
        checkSame();
      }

      expected.reset();
      testee.reset();
      checkSame();
    }

    private void checkSame() {
      assertEquals(expected.getValue(), testee.getValue());
    }
  }
}
