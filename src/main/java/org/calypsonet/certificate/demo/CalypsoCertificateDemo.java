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

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.calypsonet.terminal.calypso.certificate.legacyprime.*;
import org.calypsonet.terminal.calypso.certificate.legacyprime.DefaultCalypsoCertificateLegacyPrimeSigner;
import org.calypsonet.terminal.calypso.certificate.legacyprime.spi.CalypsoCertificateLegacyPrimeSigner;
import org.eclipse.keyple.core.util.HexUtil;

/**
 * Demonstration of Calypso Legacy Prime certificate generation.
 *
 * <p>This program demonstrates: 1. RSA and ECC key generation 2. CA (Certificate Authority)
 * certificate creation 3. Card certificate creation 4. Certificate store usage for certificate
 * management
 */
public class CalypsoCertificateDemo {

  private static final String SEPARATOR = "=".repeat(80);

  static {
    // Add Bouncy Castle as security provider
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) {
    System.out.println(SEPARATOR);
    System.out.println("CALYPSO LEGACY PRIME CERTIFICATE GENERATION DEMONSTRATION");
    System.out.println(SEPARATOR);

    try {
      // Get the factory to create generators
      CalypsoCertificateLegacyPrimeApiFactory factory =
          CalypsoCertificateLegacyPrimeService.getInstance()
              .getCalypsoCertificateLegacyPrimeApiFactory();

      CalypsoCertificateLegacyPrimeStore store = factory.getCalypsoCertificateLegacyPrimeStore();

      System.out.println("\n[1/5] Generating cryptographic keys...");
      CryptoKeys keys = generateKeys();
      System.out.println("✓ Keys generated successfully");

      System.out.println("\n[2/5] Adding PCA public key to store...");
      byte[] aid = HexUtil.toByteArray("A000000291");
      byte[] pcaKeyRef = KeyUtils.createKeyReference(aid, 1);
      store.addPcaPublicKey(pcaKeyRef, (RSAPublicKey) keys.pcaPublicKey);
      System.out.println("✓ PCA key added: " + HexUtil.toHex(pcaKeyRef));

      System.out.println("\n[3/5] Generating CA certificate...");
      byte[] caCertificate = generateCaCertificate(factory, keys, pcaKeyRef);
      System.out.println("✓ CA certificate generated (" + caCertificate.length + " bytes):");
      CertificateUtils.printCertificate(caCertificate);

      System.out.println("\n[4/5] Adding CA certificate to store...");
      byte[] caKeyRef = store.addCalypsoCaCertificateLegacyPrime(caCertificate);
      System.out.println("✓ CA certificate added with reference: " + HexUtil.toHex(caKeyRef));

      System.out.println("\n[5/5] Generating Card certificate...");
      byte[] cardCertificate = generateCardCertificate(factory, keys, caKeyRef);
      System.out.println("✓ Card certificate generated (" + cardCertificate.length + " bytes):");
      CertificateUtils.printCertificate(cardCertificate);

      System.out.println("\n" + SEPARATOR);
      System.out.println("DEMONSTRATION COMPLETED SUCCESSFULLY");
      System.out.println(SEPARATOR);

    } catch (Exception e) {
      System.err.println("\nERROR: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Generates the required cryptographic keys. RSA keys must have a public exponent of 65537
   * (0x10001).
   */
  private static CryptoKeys generateKeys() throws Exception {
    // Use KeyUtils for key generation (already forces exponent to 65537)
    KeyPair pcaKeyPair = KeyUtils.generateRSAKeyPair();
    KeyPair caKeyPair = KeyUtils.generateRSAKeyPair();
    KeyPair cardKeyPair = KeyUtils.generateECCKeyPair();

    return new CryptoKeys(pcaKeyPair, caKeyPair, cardKeyPair);
  }

  /**
   * Generates a CA (Certificate Authority) certificate. Size: 384 bytes (128 bytes data + 256 bytes
   * RSA signature)
   */
  private static byte[] generateCaCertificate(
      CalypsoCertificateLegacyPrimeApiFactory factory, CryptoKeys keys, byte[] pcaKeyRef)
      throws Exception {

    // Create a signer with the PCA private key
    CalypsoCertificateLegacyPrimeSigner pcaSigner =
        new DefaultCalypsoCertificateLegacyPrimeSigner((RSAPrivateKey) keys.pcaPrivateKey);

    // Create the CA certificate generator
    CalypsoCaCertificateLegacyPrimeGenerator generator =
        factory.createCalypsoCaCertificateLegacyPrimeGenerator(pcaKeyRef, pcaSigner);

    // CA public key reference (different from PCA)
    byte[] aid = HexUtil.toByteArray("A000000291");
    byte[] caPublicKeyRef = KeyUtils.createKeyReference(aid, 2);

    // Target Application ID (AID)
    byte[] targetAid = HexUtil.toByteArray("A000000291");

    // Configure and generate the certificate
    byte[] certificate =
        generator
            .withCaPublicKey(caPublicKeyRef, (RSAPublicKey) keys.caPublicKey)
            .withStartDate(2024, 1, 1)
            .withEndDate(2034, 12, 31)
            .withTargetAid(targetAid, false)
            .withCaRights((byte) 0x0A) // Can sign CA and Card certificates (0b1010)
            .withCaScope((byte) 0xFF) // Unrestricted scope
            .generate();

    System.out.println("   - CA key reference: " + HexUtil.toHex(caPublicKeyRef));
    System.out.println("   - Target AID: " + HexUtil.toHex(targetAid));
    System.out.println("   - Validity: 2024-01-01 to 2034-12-31");
    System.out.println("   - CA rights: 0x0A (CA + Card signing)");

    return certificate;
  }

  /**
   * Generates a Card certificate. Size: 316 bytes (60 bytes data + 256 bytes signature with
   * recoverable data)
   */
  private static byte[] generateCardCertificate(
      CalypsoCertificateLegacyPrimeApiFactory factory, CryptoKeys keys, byte[] caKeyRef)
      throws Exception {

    // Create a signer with the CA private key
    CalypsoCertificateLegacyPrimeSigner caSigner =
        new DefaultCalypsoCertificateLegacyPrimeSigner((RSAPrivateKey) keys.caPrivateKey);

    // Create the Card certificate generator
    CalypsoCardCertificateLegacyPrimeGenerator generator =
        factory.createCalypsoCardCertificateLegacyPrimeGenerator(caKeyRef, caSigner);

    // Card ECC public key (64 bytes)
    byte[] cardPublicKey = KeyUtils.extractECCPublicKeyRaw(keys.cardPublicKey);

    // Card data
    byte[] cardAid = HexUtil.toByteArray("A000000291AABBCC");
    byte[] cardSerialNumber = HexUtil.toByteArray("0123456789ABCDEF");
    byte[] cardStartupInfo = HexUtil.toByteArray("00112233445566");

    // Configure and generate the certificate
    byte[] certificate =
        generator
            .withCardPublicKey(cardPublicKey)
            .withStartDate(2024, 1, 1)
            .withEndDate(2029, 12, 31)
            .withCardAid(cardAid)
            .withCardSerialNumber(cardSerialNumber)
            .withCardStartupInfo(cardStartupInfo)
            .withIndex(0) // Optional index (default 0)
            .generate();

    System.out.println("   - Card AID: " + HexUtil.toHex(cardAid));
    System.out.println("   - Serial number: " + HexUtil.toHex(cardSerialNumber));
    System.out.println("   - Startup info: " + HexUtil.toHex(cardStartupInfo));
    System.out.println("   - Validity: 2024-01-01 to 2029-12-31");

    return certificate;
  }

  /** Class to store generated key pairs. */
  private static class CryptoKeys {
    final PrivateKey pcaPrivateKey;
    final PublicKey pcaPublicKey;
    final PrivateKey caPrivateKey;
    final PublicKey caPublicKey;
    final PrivateKey cardPrivateKey;
    final PublicKey cardPublicKey;

    CryptoKeys(KeyPair pca, KeyPair ca, KeyPair card) {
      this.pcaPrivateKey = pca.getPrivate();
      this.pcaPublicKey = pca.getPublic();
      this.caPrivateKey = ca.getPrivate();
      this.caPublicKey = ca.getPublic();
      this.cardPrivateKey = card.getPrivate();
      this.cardPublicKey = card.getPublic();
    }
  }
}
