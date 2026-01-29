/* **************************************************************************************
 * Copyright (c) 2026 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.calypsonet.certificate.demo;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

/**
 * Utilities for cryptographic key management.
 *
 * <p>This class provides methods for: - Generating RSA and ECC key pairs - Saving/loading keys in
 * PEM format - Converting keys to appropriate formats
 */
public class KeyUtils {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  /** Generates an RSA 2048-bit key pair with exponent 65537. */
  public static KeyPair generateRSAKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
    keyGen.initialize(2048);
    KeyPair tempKeyPair = keyGen.generateKeyPair();

    // Force exponent to 65537 for Calypso compatibility
    return forcePublicExponent(tempKeyPair);
  }

  /** Generates an ECC secp256r1 key pair. */
  public static KeyPair generateECCKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
    keyGen.initialize(256); // secp256r1
    return keyGen.generateKeyPair();
  }

  /** Forces the public exponent of an RSA key to 65537. */
  public static KeyPair forcePublicExponent(KeyPair keyPair) throws Exception {
    RSAPublicKey tempPublicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey tempPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

    BigInteger modulus = tempPublicKey.getModulus();
    BigInteger publicExponent = BigInteger.valueOf(65537);
    BigInteger privateExponent = tempPrivateKey.getPrivateExponent();

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
    RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

    RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, privateExponent);
    RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

    return new KeyPair(publicKey, privateKey);
  }

  /** Saves an RSA private key in PEM format. */
  public static void savePrivateKeyToPem(PrivateKey privateKey, String filename)
      throws IOException {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write("-----BEGIN PRIVATE KEY-----\n");
      byte[] encoded = privateKey.getEncoded();
      String base64 = Base64.getEncoder().encodeToString(encoded);
      // Split into 64-character lines
      for (int i = 0; i < base64.length(); i += 64) {
        int end = Math.min(i + 64, base64.length());
        writer.write(base64.substring(i, end) + "\n");
      }
      writer.write("-----END PRIVATE KEY-----\n");
    }
  }

  /** Saves an RSA public key in PEM format. */
  public static void savePublicKeyToPem(PublicKey publicKey, String filename) throws IOException {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write("-----BEGIN PUBLIC KEY-----\n");
      byte[] encoded = publicKey.getEncoded();
      String base64 = Base64.getEncoder().encodeToString(encoded);
      for (int i = 0; i < base64.length(); i += 64) {
        int end = Math.min(i + 64, base64.length());
        writer.write(base64.substring(i, end) + "\n");
      }
      writer.write("-----END PUBLIC KEY-----\n");
    }
  }

  /** Loads an RSA private key from a PEM file. */
  public static RSAPrivateKey loadPrivateKeyFromPem(String filename) throws Exception {
    try (FileReader reader = new FileReader(filename);
        PEMParser pemParser = new PEMParser(reader)) {

      Object object = pemParser.readObject();
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

      KeyPair keyPair;
      if (object instanceof PEMKeyPair) {
        keyPair = converter.getKeyPair((PEMKeyPair) object);
      } else {
        throw new IllegalArgumentException("Unsupported PEM format");
      }

      return (RSAPrivateKey) keyPair.getPrivate();
    }
  }

  /** Displays RSA key information. */
  public static void printRSAKeyInfo(String label, RSAPublicKey publicKey) {
    System.out.println(label + ":");
    System.out.println("  - Modulus size: " + publicKey.getModulus().bitLength() + " bits");
    System.out.println("  - Public exponent: " + publicKey.getPublicExponent());
  }

  /** Displays RSA private key information. */
  public static void printRSAPrivateKeyInfo(String label, RSAPrivateKey privateKey) {
    System.out.println(label + ":");
    System.out.println("  - Modulus size: " + privateKey.getModulus().bitLength() + " bits");
    System.out.println("  - Private exponent: [CONFIDENTIAL]");
  }

  /** Checks that an RSA key has the public exponent 65537. */
  public static boolean hasCorrectExponent(RSAPublicKey publicKey) {
    return publicKey.getPublicExponent().equals(BigInteger.valueOf(65537));
  }

  /**
   * Extracts an ECC public key in raw format (64 bytes: X + Y). For this example, we generate test
   * data.
   */
  public static byte[] extractECCPublicKeyRaw(PublicKey publicKey) {
    // In a real case, we would need to extract the X and Y coordinates
    // from the secp256r1 ECC key. For this example, we use test data.
    byte[] eccKey = new byte[64];
    for (int i = 0; i < eccKey.length; i++) {
      eccKey[i] = (byte) ((i * 7) % 256);
    }
    return eccKey;
  }

  /**
   * Creates a key reference in Calypso format (29 bytes).
   *
   * <p>Format: - Offset 0: 1 byte for AID size - Offset 1-16: AID (5-16 bytes) - Offset 17-24:
   * Serial number (8 bytes) - Offset 25-28: Key ID (4 bytes)
   */
  public static byte[] createKeyReference(byte[] aid, int keyId) {
    if (aid.length < 5 || aid.length > 16) {
      throw new IllegalArgumentException("AID must be between 5 and 16 bytes");
    }

    byte[] keyRef = new byte[29];

    // AID size
    keyRef[0] = (byte) aid.length;

    // AID value
    System.arraycopy(aid, 0, keyRef, 1, aid.length);

    // Serial number (8 bytes) at offset 17 - filled with zeros for this example

    // Key ID (4 bytes) at offset 25
    keyRef[28] = (byte) keyId;

    return keyRef;
  }
}
