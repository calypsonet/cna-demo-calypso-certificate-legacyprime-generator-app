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
package org.calypsonet.demo.calypso.certificate.legacyprime.generator;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.calypsonet.terminal.calypso.certificate.legacyprime.*;
import org.calypsonet.terminal.calypso.certificate.legacyprime.spi.CalypsoCertificateLegacyPrimeSigner;
import org.eclipse.keyple.core.util.HexUtil;

/**
 * Demonstration of a self-signed CA certificate.
 *
 * <p>This program demonstrates the special case where the CA certificate is self-signed, meaning
 * the CA acts as its own PCA (Prime Certificate Authority). This is typically used for:
 *
 * <ul>
 *   <li>Testing and development environments
 *   <li>Root CA that is the ultimate trust anchor
 *   <li>Simplified PKI hierarchies
 * </ul>
 *
 * <p>In this scenario: - A single RSA key pair is used for both PCA and CA roles - The CA
 * certificate is signed using the CA's own private key - The pcaKeyRef and caKeyRef reference the
 * same cryptographic key
 */
public class CalypsoCertificateDemoSelfSigned {

  private static final String SEPARATOR = "=".repeat(80);
  private static final String AID_ROOT = "A000000291";

  public static void main(String[] args) {
    System.out.println(SEPARATOR);
    System.out.println("CALYPSO LEGACY PRIME: SELF-SIGNED CA CERTIFICATE DEMONSTRATION");
    System.out.println(SEPARATOR);

    try {
      CalypsoCertificateLegacyPrimeApiFactory factory =
          CalypsoCertificateLegacyPrimeService.getInstance()
              .getCalypsoCertificateLegacyPrimeApiFactory();

      CalypsoCertificateLegacyPrimeStore store = factory.getCalypsoCertificateLegacyPrimeStore();

      System.out.println("\n[1/6] Generating cryptographic keys...");
      KeyPair caKeyPair = KeyUtils.generateRSAKeyPair();
      KeyPair cardKeyPair = KeyUtils.generateECCKeyPair();
      System.out.println("[OK] CA and Card keys generated");

      System.out.println("\n[2/6] Preparing self-signed configuration...");
      byte[] aid = HexUtil.toByteArray(AID_ROOT);
      // For self-signed: same key, but different role references
      // Index 1 = Issuer role (PCA - signs the certificate)
      // Index 2 = Subject role (CA - the entity being certified)
      byte[] pcaKeyRef = KeyUtils.createKeyReference(aid, 1);
      byte[] caKeyRef = KeyUtils.createKeyReference(aid, 2);

      // Add the key as PCA (issuer/signer) first
      store.addPcaPublicKey(pcaKeyRef, (RSAPublicKey) caKeyPair.getPublic());
      System.out.println("[OK] Key registered as PCA (issuer role)");
      System.out.println("   - PCA key ref (issuer): " + HexUtil.toHex(pcaKeyRef));
      System.out.println("   - CA key ref (subject): " + HexUtil.toHex(caKeyRef));
      System.out.println("   - Note: Same physical RSA key, different roles");

      System.out.println("\n[3/6] Generating self-signed CA certificate...");
      // Create signer with CA's own private key (self-signing)
      CalypsoCertificateLegacyPrimeSigner caSigner =
          DefaultCalypsoCertificateLegacyPrimeSigner.fromRSAPrivateKey(
              (RSAPrivateKey) caKeyPair.getPrivate());

      byte[] targetAid = HexUtil.toByteArray(AID_ROOT);

      // PCA (index 1) signs a certificate for CA (index 2), using the same physical key
      byte[] caCertificate =
          factory
              .createCalypsoCaCertificateLegacyPrimeGenerator(pcaKeyRef, caSigner)
              .withCaPublicKey(caKeyRef, (RSAPublicKey) caKeyPair.getPublic())
              .withStartDate(2024, 1, 1)
              .withEndDate(2034, 12, 31)
              .withTargetAid(targetAid, false)
              .withCaRights((byte) 0x0A) // Can sign CA and Card certificates
              .withCaScope((byte) 0xFF) // Unrestricted scope
              .generate();

      System.out.println(
          "[OK] Self-signed CA certificate generated (" + caCertificate.length + " bytes)");
      System.out.println("   - Issuer (PCA): " + HexUtil.toHex(pcaKeyRef));
      System.out.println("   - Subject (CA): " + HexUtil.toHex(caKeyRef));
      System.out.println("   - Same cryptographic key for both roles");
      System.out.println("   - Target AID: " + HexUtil.toHex(targetAid));
      System.out.println("   - Validity: 2024-01-01 to 2034-12-31");
      System.out.println("   - CA rights: 0x0A (CA + Card signing)");
      System.out.println("   Certificate (hex):");
      CertificateUtils.printCertificate(caCertificate);

      System.out.println("\n[4/6] Adding self-signed CA certificate to store...");
      store.addCalypsoCaCertificateLegacyPrime(caCertificate);
      System.out.println("[OK] Certificate validated and added to store");

      System.out.println("\n[6/6] Generating Card certificate signed by self-signed CA...");
      byte[] cardPublicKey = KeyUtils.extractECCPublicKeyRaw(cardKeyPair.getPublic());
      byte[] cardAid = HexUtil.toByteArray(AID_ROOT + "AABBCC");
      byte[] cardSerialNumber = HexUtil.toByteArray("0123456789ABCDEF");
      byte[] cardStartupInfo = HexUtil.toByteArray("00112233445566");

      // Use the CA key reference (index 2) as the issuer for the Card certificate
      byte[] cardCertificate =
          factory
              .createCalypsoCardCertificateLegacyPrimeGenerator(caKeyRef, caSigner)
              .withCardPublicKey(cardPublicKey)
              .withCardAid(cardAid)
              .withCardSerialNumber(cardSerialNumber)
              .withCardStartupInfo(cardStartupInfo)
              .withStartDate(2024, 1, 1)
              .withEndDate(2029, 12, 31)
              .withIndex(0)
              .generate();

      System.out.println("[OK] Card certificate generated (" + cardCertificate.length + " bytes)");
      System.out.println("   - Card AID: " + HexUtil.toHex(cardAid));
      System.out.println("   - Signed by: Self-signed CA");
      System.out.println("   Certificate (hex):");
      CertificateUtils.printCertificate(cardCertificate);

      System.out.println("\n" + SEPARATOR);
      System.out.println("DEMONSTRATION COMPLETED SUCCESSFULLY");
      System.out.println(SEPARATOR);

      System.out.println("\nKey Concept:");
      System.out.println("- In a self-signed CA setup:");
      System.out.println(
          "  * The SAME RSA key pair is used for BOTH issuer (PCA) and subject (CA) roles");
      System.out.println("  * Different key references (index 1 vs 2) distinguish the roles");
      System.out.println("  * The PCA reference (index 1) signs the CA certificate");
      System.out.println(
          "  * The CA reference (index 2) is certified and can then sign Card certificates");
      System.out.println("- This represents a root CA that is its own trust anchor");
      System.out.println(
          "- Use cases: testing, development, root CAs in simplified PKI hierarchies");

    } catch (Exception e) {
      System.err.println("\nERROR: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
