//
// Copyright (c) 2024 Satoshi Fukutomi <info@fuktommy.com>.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//

package com.fuktommy.pinencrypter;

import junit.framework.TestCase;

import java.util.List;

public class EncoderTest extends TestCase {

    public void testEncodeToken() throws Exception {
        final String result1 = new Encoder().encodeToken("key", "1234", 4);
        assertEquals("5793", result1);

        final String result2 = new Encoder().encodeToken("key", "1234", 10);
        assertEquals("5793116204", result2);
    }

    public void testDecodeToken() throws Exception {
        final List<String> result = new Encoder().decodeToken("key", "5792");
        assertEquals("1558,4657", String.join(",", result));
    }

    public void testEncode() throws Exception {
        final String result1 = new Encoder().encode("key", "1234-5678");
        assertEquals("5793-9976", result1);

        // 全角文字は数字扱いされない
        final String result2 = new Encoder().encode("key", "1２3");
        assertEquals("5２0", result2);
    }

    public void testDecode() throws Exception {
        final List<List<String>> result1 = new Encoder().decode("key", "5793-5792");
        assertEquals(3, result1.size());
        assertEquals("1234", String.join(",", result1.get(0)));
        assertEquals("-", String.join(",", result1.get(1)));
        assertEquals("1558,4657", String.join(",", result1.get(2)));

        // 全角文字は数字扱いされない
        final List<List<String>> result2 = new Encoder().decode("key", "5２0");
        assertEquals(3, result2.size());
        assertEquals("1", String.join(",", result2.get(0)));
        assertEquals("２", String.join(",", result2.get(1)));
        assertEquals("3,9", String.join(",", result2.get(2)));
    }
}
