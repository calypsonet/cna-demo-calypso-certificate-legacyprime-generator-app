# Project Overview

## Description

This console project provides a comprehensive demonstration of the Calypso APIs for generating Legacy Prime certificates. It offers two complementary implementations that illustrate different approaches to using the libraries.

## Learning Objectives

1.  **Understand the Calypso PKI architecture**
    *   Hierarchy: PCA → CA → Card
    *   Certificate chain of trust
    *   Roles and responsibilities of each level

2.  **Master the Calypso APIs**
    *   `CalypsoCertificateLegacyPrimeApiFactory` - Factory pattern
    *   `CalypsoCertificateLegacyPrimeStore` - Certificate management
    *   Certificate generators (CA and Card)
    *   `CalypsoCertificateLegacyPrimeSigner` interface (SPI)

3.  **Manage cryptographic keys**
    *   Generation of RSA 2048-bit keys
    *   Generation of ECC secp256r1 keys
    *   Saving and loading in PEM format
    *   Use of Bouncy Castle

## Project Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Console Demo                              │
│  ┌─────────────────────┐     ┌─────────────────────────┐   │
│  │ Simple Demo         │     │ Advanced Demo (PEM)     │   │
│  │ - In-memory keys    │     │ - File-based keys       │   │
│  │ - Direct usage      │     │ - Key persistence       │   │
│  └──────────┬──────────┘     └──────────┬──────────────┘   │
│             │                           │                   │
│             └───────────┬───────────────┘                   │
│                         │                                   │
└─────────────────────────┼───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Calypso Certificate Library                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ DefaultCalypsoCertificateLegacyPrimeSigner          │  │
│  │ - ISO 9796-2 PSS signature                          │  │
│  │ - SHA-256 digest                                     │  │
│  │ - PEM file loading                                   │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ CalypsoCertificateLegacyPrimeApiFactory             │  │
│  │ - CA Generator                                       │  │
│  │ - Card Generator                                     │  │
│  │ - Certificate Store                                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Calypso Certificate API                         │
│  - Public interfaces                                        │
│  - Data types                                               │
│  - Exceptions                                               │
└─────────────────────────────────────────────────────────────┘
```

## Comparison of the Two Demos

### Simple Demo (`CalypsoCertificateDemo.java`)

**Use case:** Quick demonstration, prototyping, testing

**Features:**
- ✓ In-memory key generation
- ✓ Fast and autonomous execution
- ✓ No file management
- ✓ Ideal for understanding basic concepts
- ✗ Keys are lost after execution
- ✗ Not suitable for production

**Key code:**
```java
// Direct creation of the signer with an in-memory key
RSAPrivateKey privateKey = generateRSAKey();
CalypsoCertificateLegacyPrimeSigner signer =
    new DefaultCalypsoCertificateLegacyPrimeSigner(privateKey);
```

### Advanced Demo (`CalypsoCertificateDemoWithFiles.java`)

**Use case:** Development, integration, testing with persistent keys

**Features:**
- ✓ Saving keys in PEM format
- ✓ Loading existing keys
- ✓ Reusability of keys
- ✓ Close to a real-world use case
- ✓ File management
- ✓ Documentation of file formats

**Key code:**
```java
// Save the key
KeyUtils.savePrivateKeyToPem(privateKey, "keys/pca-private.pem");

// Load and create the signer from a PEM file
CalypsoCertificateLegacyPrimeSigner signer =
    DefaultCalypsoCertificateLegacyPrimeSigner.fromPemFile("keys/pca-private.pem");
```

## Utility Classes

### `KeyUtils.java`

Provides static methods for:

1.  **Key generation**
    *   `generateRSAKeyPair()` - RSA 2048-bit with exponent 65537
    *   `generateECCKeyPair()` - ECC secp256r1
    *   `forcePublicExponent()` - Normalizes the RSA public exponent

2.  **PEM file management**
    *   `savePrivateKeyToPem()` - Saves a private key
    *   `savePublicKeyToPem()` - Saves a public key
    *   `loadPrivateKeyFromPem()` - Loads a private key

3.  **Utilities**
    *   `createKeyReference()` - Creates a Calypso key reference (29 bytes)
    *   `extractECCPublicKeyRaw()` - Extracts X+Y coordinates (64 bytes)
    *   `printRSAKeyInfo()` - Displays key information

## Certificate Generation Flow

### Generating a CA Certificate

```
1. Prerequisites
   └─ PCA public key added to the store
   └─ PCA private key for signing

2. CA key pair generation
   └─ RSA 2048-bit with exponent 65537

3. Generator configuration
   ├─ Issuer key reference (PCA)
   ├─ CA public key
   ├─ Validity dates
   ├─ Target AID (optional)
   ├─ CA rights (0x0A = can sign CA and Card)
   └─ Scope (0xFF = unrestricted)

4. Generation and signing
   └─ 384 bytes (128 data + 256 signature)

5. Add to store
   └─ Automatic signature validation
   └─ Extraction of the CA key reference
```

### Generating a Card Certificate

```
1. Prerequisites
   └─ CA certificate added to the store
   └─ CA private key for signing

2. Card key pair generation
   └─ ECC secp256r1

3. Generator configuration
   ├─ Issuer key reference (CA)
   ├─ ECC public key (64 bytes)
   ├─ Card AID
   ├─ Serial number (8 bytes)
   ├─ Startup info (7 bytes)
   ├─ Validity dates
   └─ Index (optional, default 0)

4. Generation and signing
   └─ 316 bytes (60 data + 256 signature with recovery)
```

## Data Formats

### Key Reference (29 bytes)

```
+--------+--------+--------------------------------------------+
| Offset | Size   |                  Field                     |
+--------+--------+--------------------------------------------+
|   0    | 1 byte | AID length (5-16)                          |
|   1    | 5-16 b | AID (Application Identifier)               |
| 17     | 8 bytes| Serial Number                              |
| 25     | 4 bytes| Key ID                                     |
+--------+--------+--------------------------------------------+
```

### CA Certificate (384 bytes)

```
+--------+--------+--------------------------------------------+
| Offset | Size   |                  Field                     |
+--------+--------+--------------------------------------------+
|   0    | 1 byte | Type (0x90 = CA)                           |
|   1    | 1 byte | Version (0x01)                             |
|   2    | 29 b   | Issuer key reference (PCA)                 |
|  31    | 29 b   | Target CA key reference                    |
|  60    | 34 b   | RSA public key header                      |
|  94    | 256 b  | RSA 2048-bit modulus                       |
| 350    | ~34 b  | Dates, rights, scope, AID, padding         |
+--------+--------+--------------------------------------------+
|     Total data to be signed: 128 bytes                       |
+--------+--------+--------------------------------------------+
| 128    | 256 b  | RSA Signature (ISO 9796-2 PSS)             |
+--------+--------+--------------------------------------------+
|                    Total: 384 bytes                          |
+----------------------------------------------------------------+
```

### Card Certificate (316 bytes)

```
+--------+--------+--------------------------------------------+
| Offset | Size   |                  Field                     |
+--------+--------+--------------------------------------------+
|   0    | 1 byte | Type (0x91 = Card)                         |
|   1    | 1 byte | Version (0x01)                             |
|   2    | 29 b   | Issuer key reference (CA)                  |
|  31    | 64 b   | ECC public key (X + Y)                     |
|  95    | ~25 b  | Other non-recoverable fields               |
+--------+--------+--------------------------------------------+
|     Total non-recoverable data: 60 bytes                     |
+--------+--------+--------------------------------------------+
|  60    | 256 b  | RSA signature with 222 bytes of            |
|        |        | recoverable data (AID, serial, dates, etc.)|
+--------+--------+--------------------------------------------+
|                    Total: 316 bytes                          |
+----------------------------------------------------------------+
```

## ISO 9796-2 PSS Signature

The system uses RSA signature with message recovery according to ISO 9796-2:

**Features:**
- Algorithm: ISO/IEC 9796-2 PSS (Probabilistic Signature Scheme)
- Digest: SHA-256
- Signature size: 256 bytes (2048 bits)
- Salt: None (deterministic)
- Message recovery: 222 bytes for Card certificates

**Advantages:**
- Allows recovering part of the data from the signature
- Reduces the total size of the certificate
- A recognized standard for smart cards

## Dependencies

```gradle
// Calypso APIs
org.calypsonet:calypsonet-terminal-calypso-certificate-legacyprime-java-api:0.1.0-SNAPSHOT
org.calypsonet:calypsonet-terminal-calypso-certificate-legacyprime-java-lib:0.1.0-SNAPSHOT

// Utilities
org.eclipse.keyple:keyple-util-java-lib:2.4.0

// Cryptography
org.bouncycastle:bcprov-jdk18on:1.79
org.bouncycastle:bcpkix-jdk18on:1.79

// Logging
org.slf4j:slf4j-api:1.7.36
org.slf4j:slf4j-simple:1.7.36
```

## Best Practices

### Security

1.  **Private keys**
    *   Never commit private key files
    *   Use HSMs in production
    *   Protect PEM files with strict permissions

2.  **Validation**
    *   Always validate certificates after generation
    *   Verify the chain of trust
    *   Check validity dates

3.  **Key management**
    *   Use different keys for each environment
    *   Implement key rotation
    *   Keep secure backups

### Development

1.  **Tests**
    *   Test with test keys only
    *   Never use production keys in development
    *   Implement automated tests

2.  **Logging**
    *   Log important events
    *   Never log private keys
    *   Use appropriate log levels

3.  **Error handling**
    *   Capture and handle specific exceptions
    *   Provide clear error messages
    *   Implement retry mechanisms if necessary

## Possible Extensions

1.  **Graphical interface**
    *   See `cna-tool-calypso-certificate-legacyprime-generator-app` for an example with Compose Desktop

2.  **REST API**
    *   Expose certificate generation via REST
    *   Implement authentication and authorization
    *   Use an HSM for key management

3.  **Command line**
    *   Add arguments to customize generation
    *   Support configuration files
    *   Implement a batch mode

4.  **Integration**
    *   Integrate with existing PKI systems
    *   Support other key formats (PKCS#12, JKS)
    *   Implement connectors for databases

## Resources

- **API Documentation**: `calypsonet-terminal-calypso-certificate-legacyprime-java-api/src/main/java/`
- **Integration Tests**: `calypsonet-terminal-calypso-certificate-legacyprime-java-lib/src/test/`
- **GUI Application**: `cna-tool-calypso-certificate-legacyprime-generator-app/`
- **Calypso Networks**: https://calypsonet.org/

## License

- API: MIT License
- Library: EPL-2.0 (Eclipse Public License 2.0)
- This demo: Follow the license of the parent project

## Contribution

To contribute to this project:
1.  Understand the architecture of the Calypso APIs
2.  Follow existing code conventions
3.  Add tests for any new functionality
4.  Document the changes

## Support

For technical questions:
- Consult the inline documentation in the code
- Study the integration tests
- Refer to the Calypso specifications
- Contact the Calypso Networks Association