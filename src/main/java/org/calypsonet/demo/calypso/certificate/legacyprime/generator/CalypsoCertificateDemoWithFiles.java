/* **************************************************************************************
 * Copyright (c) 2026 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * BSD 3-Clause License which is available at https://opensource.org/license/bsd-3-clause
 *
 * SPDX-License-Identifier: BSD-3-Clause
 ************************************************************************************** */
package org.calypsonet.demo.calypso.certificate.legacyprime.generator;

import java.io.File;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import org.calypsonet.terminal.calypso.certificate.legacyprime.*;
import org.calypsonet.terminal.calypso.certificate.legacyprime.DefaultCalypsoCertificateLegacyPrimeSigner;
import org.calypsonet.terminal.calypso.certificate.legacyprime.spi.CalypsoCertificateLegacyPrimeSigner;
import org.eclipse.keyple.core.util.HexUtil;

/**
 * Advanced demonstration of Calypso certificate generation with keys loaded from files.
 *
 * <p>This program demonstrates: 1. Key generation and saving in PEM format 2. Loading keys from PEM
 * files 3. Using DefaultCalypsoCertificateLegacyPrimeSigner.fromPemFile() 4. CA and Card
 * certificate generation
 */
public class CalypsoCertificateDemoWithFiles {

  private static final String SEPARATOR = "=".repeat(80);
  private static final String KEYS_DIR = "keys";
  private static final String AID_ROOT = "A000000291";

  public static void main(String[] args) {
    System.out.println(SEPARATOR);
    System.out.println("ADVANCED DEMONSTRATION: CALYPSO CERTIFICATES WITH PEM FILES");
    System.out.println(SEPARATOR);

    try {
      // Create directory for keys
      File keysDir = new File(KEYS_DIR);
      if (!keysDir.exists()) {
        keysDir.mkdirs();
      }

      // Get the factory
      CalypsoCertificateLegacyPrimeApiFactory factory =
          CalypsoCertificateLegacyPrimeService.getInstance()
              .getCalypsoCertificateLegacyPrimeApiFactory();

      CalypsoCertificateLegacyPrimeStore store = factory.getCalypsoCertificateLegacyPrimeStore();

      System.out.println("\n[1/8] Generating key pairs...");
      KeyPair pcaKeyPair = KeyUtils.generateRSAKeyPair();
      KeyPair caKeyPair = KeyUtils.generateRSAKeyPair();
      KeyPair cardKeyPair = KeyUtils.generateECCKeyPair();
      System.out.println("[OK] Keys generated");

      System.out.println("\n[2/8] Saving private keys in PEM format...");
      String pcaPrivateKeyFile = KEYS_DIR + "/pca-private.pem";
      String caPrivateKeyFile = KEYS_DIR + "/ca-private.pem";

      KeyUtils.savePrivateKeyToPem(pcaKeyPair.getPrivate(), pcaPrivateKeyFile);
      KeyUtils.savePrivateKeyToPem(caKeyPair.getPrivate(), caPrivateKeyFile);
      System.out.println("[OK] Keys saved:");
      System.out.println("   - " + pcaPrivateKeyFile);
      System.out.println("   - " + caPrivateKeyFile);

      System.out.println("\n[3/8] Displaying key information...");
      KeyUtils.printRSAKeyInfo("PCA public key", (RSAPublicKey) pcaKeyPair.getPublic());
      KeyUtils.printRSAKeyInfo("CA public key", (RSAPublicKey) caKeyPair.getPublic());

      System.out.println("\n[4/8] Adding PCA public key to store...");
      byte[] aid = HexUtil.toByteArray(AID_ROOT);
      byte[] pcaKeyRef = KeyUtils.createKeyReference(aid, 1);
      store.addPcaPublicKey(pcaKeyRef, (RSAPublicKey) pcaKeyPair.getPublic());
      System.out.println("[OK] PCA key added: " + HexUtil.toHex(pcaKeyRef));

      System.out.println("\n[5/8] Loading PCA private key from PEM file...");
      CalypsoCertificateLegacyPrimeSigner pcaSigner =
          DefaultCalypsoCertificateLegacyPrimeSigner.fromPemFile(pcaPrivateKeyFile);
      System.out.println("[OK] PCA signer created from: " + pcaPrivateKeyFile);

      System.out.println("\n[6/8] Generating CA certificate...");
      byte[] caKeyRef = KeyUtils.createKeyReference(aid, 2);
      byte[] targetAid = HexUtil.toByteArray(AID_ROOT);

      byte[] caCertificate =
          factory
              .createCalypsoCaCertificateLegacyPrimeGenerator(pcaKeyRef, pcaSigner)
              .withCaPublicKey(caKeyRef, (RSAPublicKey) caKeyPair.getPublic())
              .withStartDate(2024, 1, 1)
              .withEndDate(2034, 12, 31)
              .withTargetAid(targetAid, false)
              .withCaRights((byte) 0x0A) // Can sign CA and Card
              .withCaScope((byte) 0xFF)
              .generate();

      System.out.println("[OK] CA certificate generated (" + caCertificate.length + " bytes)");
      System.out.println("   CA certificate (hex):");
      CertificateUtils.printCertificate(caCertificate);

      System.out.println("\n[7/8] Adding CA certificate to store...");
      store.addCalypsoCaCertificateLegacyPrime(caCertificate);
      System.out.println("[OK] CA certificate added to store");

      System.out.println("\n[8/8] Generating Card certificate with signer loaded from PEM...");
      CalypsoCertificateLegacyPrimeSigner caSigner =
          DefaultCalypsoCertificateLegacyPrimeSigner.fromPemFile(caPrivateKeyFile);

      byte[] cardPublicKey = KeyUtils.extractECCPublicKeyRaw(cardKeyPair.getPublic());
      byte[] cardAid = HexUtil.toByteArray(AID_ROOT + "AABBCC");
      byte[] cardSerialNumber = HexUtil.toByteArray("0123456789ABCDEF");
      byte[] cardStartupInfo = HexUtil.toByteArray("00112233445566");

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
      System.out.println("   Card certificate (hex):");
      CertificateUtils.printCertificate(cardCertificate);

      System.out.println("\n" + SEPARATOR);
      System.out.println("DEMONSTRATION COMPLETED SUCCESSFULLY");
      System.out.println(SEPARATOR);

      System.out.println("\nNotes:");
      System.out.println("- Private keys have been saved in the '" + KEYS_DIR + "/' directory");
      System.out.println("- You can reuse these keys to sign other certificates");
      System.out.println(
          "- DefaultCalypsoCertificateLegacyPrimeSigner.fromPemFile() directly loads");
      System.out.println("  an RSA private key in PEM PKCS#8 format");

    } catch (Exception e) {
      System.err.println("\nERROR: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
