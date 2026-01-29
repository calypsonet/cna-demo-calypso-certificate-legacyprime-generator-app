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

import org.eclipse.keyple.core.util.HexUtil;

/**
 * Utilities for certificate display and formatting.
 *
 * <p>This class provides methods for: - Displaying certificates in hexadecimal format - Formatting
 * certificate output with various display options
 */
public class CertificateUtils {

  private CertificateUtils() {
    // Utility class, prevent instantiation
  }

  /**
   * Displays a certificate in hexadecimal format (full output).
   *
   * <p>The certificate is displayed in blocks of 64 characters (32 bytes) per line, with
   * indentation for readability.
   *
   * @param certificate The certificate bytes to display.
   */
  public static void printCertificate(byte[] certificate) {
    String hex = HexUtil.toHex(certificate);

    // Display in blocks of 64 characters (32 bytes)
    for (int i = 0; i < hex.length(); i += 64) {
      int end = Math.min(i + 64, hex.length());
      System.out.println("   " + hex.substring(i, end));
    }
  }

  /**
   * Displays a certificate in compact hexadecimal format (limited lines).
   *
   * <p>The certificate is displayed in blocks of 64 characters per line, up to a maximum number of
   * lines. If the certificate is longer, an ellipsis and byte count are shown.
   *
   * @param certificate The certificate bytes to display.
   * @param maxLines Maximum number of lines to display (recommended: 4-8).
   */
  public static void printCertificateCompact(byte[] certificate, int maxLines) {
    String hex = HexUtil.toHex(certificate);
    int lineLength = 64;
    int linesShown = 0;

    for (int i = 0; i < hex.length() && linesShown < maxLines; i += lineLength) {
      int end = Math.min(i + lineLength, hex.length());
      System.out.println("   " + hex.substring(i, end));
      linesShown++;
    }

    if (hex.length() > lineLength * maxLines) {
      int remainingBytes = certificate.length - (maxLines * 32);
      System.out.println("   ... (" + remainingBytes + " bytes remaining)");
    }
  }

  /**
   * Displays a certificate with a custom line length.
   *
   * @param certificate The certificate bytes to display.
   * @param charsPerLine Number of hexadecimal characters per line (should be even).
   */
  public static void printCertificateCustom(byte[] certificate, int charsPerLine) {
    String hex = HexUtil.toHex(certificate);

    for (int i = 0; i < hex.length(); i += charsPerLine) {
      int end = Math.min(i + charsPerLine, hex.length());
      System.out.println("   " + hex.substring(i, end));
    }
  }
}
