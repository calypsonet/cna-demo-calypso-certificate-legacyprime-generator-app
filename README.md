# Calypso Legacy Prime Certificate Generation Demonstration

This console project demonstrates how to use the Calypso APIs to generate CA and Card certificates.

## Description

This program illustrates the following steps:

1.  **Cryptographic key generation**
    *   RSA 2048-bit keys for PCA (Primary Certificate Authority)
    *   RSA 2048-bit keys for CA (Certificate Authority)
    *   ECC secp256r1 keys for Card

2.  **Generation of a CA certificate**
    *   Size: 384 bytes (128 bytes data + 256 bytes RSA signature)
    *   Signed by the PCA private key
    *   Contains the CA public key

3.  **Generation of a Card certificate**
    *   Size: 316 bytes (60 bytes data + 256 bytes signature with recoverable data)
    *   Signed by the CA private key
    *   Contains the card's ECC public key

## Project Structure

```
calypso-certificate-demo-console/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── calypso/
│                   └── demo/
│                       ├── CalypsoCertificateDemo.java           (simple demo)
│                       ├── CalypsoCertificateDemoWithFiles.java  (demo with PEM files)
│                       └── KeyUtils.java                        (utilities)
├── build.gradle
├── settings.gradle
├── gradle.properties
├── run.bat         (Windows script)
├── run.sh          (Linux/Mac script)
└── README.md
```

## Prerequisites

- Java 11 or higher
- Gradle 7.0 or higher
- The Calypso libraries must be installed in the local Maven repository:
  - `org.calypsonet:calypsonet-terminal-calypso-certificate-legacyprime-java-api:0.1.0-SNAPSHOT`
  - `org.calypsonet:calypsonet-terminal-calypso-certificate-legacyprime-java-lib:0.1.0-SNAPSHOT`

## Installing Dependencies

Before running this project, install the Calypso libraries in your local Maven repository:

```bash
# From the parent directory
cd ../calypsonet-terminal-calypso-certificate-legacyprime-java-api
gradle publishToMavenLocal

cd ../calypsonet-terminal-calypso-certificate-legacyprime-java-lib
gradle publishToMavenLocal
```

## Compilation and Execution

### With Gradle

```bash
# Compilation
gradle build

# Run the simple demo (in-memory)
gradle run
# or
gradle runSimple

# Run the demo with PEM files
gradle runWithFiles
```

### With Gradle Wrapper (recommended)

```bash
# Compilation
./gradlew build      # Linux/Mac
gradlew.bat build    # Windows

# Run the simple demo
./gradlew run        # Linux/Mac
gradlew.bat run      # Windows

# Run the demo with PEM files
./gradlew runWithFiles      # Linux/Mac
gradlew.bat runWithFiles    # Windows
```

### With the provided scripts

```bash
# Windows
run.bat

# Linux/Mac
chmod +x run.sh
./run.sh
```

## Two Demo Versions

### 1. CalypsoCertificateDemo (Simple)

The simple version generates all keys in memory and demonstrates:
- Generation of RSA and ECC keys
- Direct creation of signers with `new DefaultCalypsoCertificateLegacyPrimeSigner(privateKey)`
- Generation of CA and Card certificates
- Use of the store

**Command**: `gradle run` or `gradle runSimple`

### 2. CalypsoCertificateDemoWithFiles (Advanced)

The advanced version saves and loads keys from PEM files and demonstrates:
- Generation and saving of keys in PEM format
- Loading keys with `DefaultCalypsoCertificateLegacyPrimeSigner.fromPemFile()`
- Reuse of existing keys
- Management of key files

**Command**: `gradle runWithFiles`

This version creates a `keys/` directory containing:
- `pca-private.pem` - PCA private key
- `ca-private.pem` - CA private key

## Expected Output

The program displays:

1.  The key generation steps
2.  The addition of the PCA key to the store
3.  The generation of the CA certificate (384 bytes in hexadecimal)
4.  The addition of the CA certificate to the store
5.  The generation of the Card certificate (316 bytes in hexadecimal)

Example output:

```
================================================================================
CALYPSO LEGACY PRIME CERTIFICATE GENERATION DEMONSTRATION
================================================================================

[1/5] Generating cryptographic keys...
✓ Keys generated successfully

[2/5] Adding PCA public key to store...
✓ PCA key added: 05A00000029100000000000000000000000000000000000001

[3/5] Generating CA certificate...
   - CA key reference: 05A00000029100000000000000000000000000000000000002
   - Target AID: A000000291
   - Validity: 2024-01-01 to 2034-12-31
   - CA rights: 0x0A (CA + Card signing)
✓ CA certificate generated (384 bytes):
   ...

[4/5] Adding CA certificate to store...
✓ CA certificate added with reference: ...

[5/5] Generating Card certificate...
   - Card AID: A000000291AABBCC
   - Serial number: 0123456789ABCDEF
   - Startup info: 00112233445566
   - Validity: 2024-01-01 to 2029-12-31
✓ Card certificate generated (316 bytes):
   ...

================================================================================
DEMONSTRATION COMPLETED SUCCESSFULLY
================================================================================
```

## Certificate Structure

### CA Certificate (384 bytes)

- Type: 1 byte (0x90)
- Version: 1 byte (0x01)
- Issuer key reference: 29 bytes
- CA public key header: 34 bytes
- Other fields: 64 bytes
- **Total data**: 128 bytes
- **RSA Signature**: 256 bytes

### Card Certificate (316 bytes)

- Type: 1 byte (0x91)
- Version: 1 byte (0x01)
- Issuer key reference: 29 bytes
- ECC public key: 64 bytes
- Other fields: 25 bytes
- **Total non-recoverable data**: 60 bytes
- **Recoverable data**: 222 bytes (integrated into the signature)
- **RSA Signature**: 256 bytes

## Main APIs Used

- `CalypsoCertificateLegacyPrimeService` - Singleton entry point
- `CalypsoCertificateLegacyPrimeApiFactory` - Factory to create generators
- `CalypsoCertificateLegacyPrimeStore` - Storage for certificates and keys
- `CalypsoCaCertificateLegacyPrimeGenerator` - Generator for CA certificates
- `CalypsoCardCertificateLegacyPrimeGenerator` - Generator for Card certificates
- `DefaultCalypsoCertificateLegacyPrimeSigner` - Default implementation of the signer

## Technical Notes

- RSA keys must be 2048 bits with a public exponent of 65537
- ECC keys use the secp256r1 curve (64 bytes)
- The signature uses ISO/IEC 9796-2 PSS with SHA-256
- Key references are 29 bytes long

## License

See the license files of the Calypso libraries:
- API: MIT License
- Library: EPL-2.0

## References

- [Calypso Networks Association](https://calypsonet.org/)
- API Documentation: `calypsonet-terminal-calypso-certificate-legacyprime-java-api`
- Library Documentation: `calypsonet-terminal-calypso-certificate-legacyprime-java-lib`