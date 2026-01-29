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

import org.eclipse.keyple.core.util.HexUtil;

/**
 * Utilities for certificate display.
 *
 * <p>This class provides methods for displaying certificates in hexadecimal format.
 */
public class CertificateUtils {

  private CertificateUtils() {
    // Utility class, prevent instantiation
  }

  /**
   * Displays a certificate in hexadecimal format.
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
}
