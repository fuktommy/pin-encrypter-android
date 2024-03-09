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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Encoder {
    String encodeToken(String key, String pin, int length)
            throws InvalidKeyException, NoSuchAlgorithmException {
        final String algo = "HMacSHA512";
        final SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), algo);
        final Mac mac = Mac.getInstance(algo);
        mac.init(keySpec);
        final byte[] sign = mac.doFinal(pin.getBytes());
        final StringBuilder fullDigest = new StringBuilder();
        for (byte b : sign) {
            fullDigest.append(String.format("%02x", b & 0xff));
        }
        final String digest = fullDigest.toString().replaceAll("[a-f]", "");
        if (digest.equals(pin)) {
            return encodeToken(key + key, pin, length);
        } else if (digest.length() < length) {
            return digest + encodeToken(key + key, pin, length - digest.length());
        } else {
            return digest.substring(0, length);
        }
    }

    List<String> decodeToken(String key, String pin)
            throws InvalidKeyException, NoSuchAlgorithmException {
        List<String> result = new ArrayList<>();
        final int length = pin.length();
        for (int i = 0; i < Math.pow(10, length); i++) {
            String p = String.format(String.format(Locale.US, "%%0%dd", length), i);
            if (encodeToken(key, p, length).equals(pin)) {
                result.add(p);
            }
        }
        return result;
    }

    String encode(String key, String pin)
            throws InvalidKeyException, NoSuchAlgorithmException {
        List<String> digests = new ArrayList<>();
        for (String p : pin.split("((?<=\\D)|(?=\\D))")) {
            if (p.matches("\\d+")) {
                digests.add(encodeToken(key, p, p.length()));
            } else {
                digests.add(p);
            }
        }
        return String.join("", digests);
    }

    List<List<String>> decode(String key, String pin)
            throws InvalidKeyException, NoSuchAlgorithmException {
        List<List<String>> digests = new ArrayList<>();
        for (String p : pin.split("((?<=\\D)|(?=\\D))")) {
            if (p.matches("\\d+")) {
                digests.add(decodeToken(key, p));
            } else {
                List<String> list = new ArrayList<>();
                list.add(p);
                digests.add(list);
            }
        }
        return digests;
    }
}
