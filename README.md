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
cna-demo-calypso-certificate-legacyprime-generator-app/
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── calypsonet/
│                   └── demo/
│                       └── calypso/
│                           └── certificate/
│                               └── legacyprime/
│                                   └── generator/
│                                       ├── CalypsoCertificateDemoInMemory.java   (in-memory demo)
│                                       ├── CalypsoCertificateDemoWithFiles.java  (demo with PEM files)
│                                       ├── CalypsoCertificateDemoSelfSigned.java (self-signed CA demo)
│                                       ├── KeyUtils.java                         (key utilities)
│                                       └── CertificateUtils.java                 (certificate utilities)
├── build.gradle.kts
├── settings.gradle
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

## Compilation

```bash
# Build with Gradle Wrapper (recommended)
./gradlew build      # Linux/Mac
gradlew.bat build    # Windows
```

## Running the Demonstrations

After building, you can run the demonstrations using the Java command with the runtime classpath.

### Available Demonstrations

**1. CalypsoCertificateDemoInMemory** - In-memory demonstration
- PCA → CA → Card certificate chain
- All keys generated and stored in memory only
- No file persistence

**2. CalypsoCertificateDemoWithFiles** - Advanced demonstration with PEM files
- Saves/loads private keys in PEM format
- Creates a `keys/` directory with PCA and CA private keys
- Demonstrates key persistence and reuse

**3. CalypsoCertificateDemoSelfSigned** - Self-signed CA demonstration
- Same RSA key for both PCA (issuer) and CA (subject) roles
- Demonstrates a root CA as its own trust anchor

### Execution

Run the desired demo class:

```bash
# Example: In-memory demo
java -cp "build/classes/java/main:$HOME/.gradle/caches/modules-2/files-2.1/*/*/*.jar" \
  org.calypsonet.demo.calypso.certificate.legacyprime.generator.CalypsoCertificateDemoInMemory

# Example: Demo with files
java -cp "build/classes/java/main:$HOME/.gradle/caches/modules-2/files-2.1/*/*/*.jar" \
  org.calypsonet.demo.calypso.certificate.legacyprime.generator.CalypsoCertificateDemoWithFiles

# Example: Self-signed demo
java -cp "build/classes/java/main:$HOME/.gradle/caches/modules-2/files-2.1/*/*/*.jar" \
  org.calypsonet.demo.calypso.certificate.legacyprime.generator.CalypsoCertificateDemoSelfSigned
```

**Note**: Adjust the classpath pattern according to your environment and dependency locations.

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
[OK] Keys generated successfully

[2/5] Adding PCA public key to store...
[OK] PCA key added: 05A00000029100000000000000000000000000000000000001

[3/5] Generating CA certificate...
   - CA key reference: 05A00000029100000000000000000000000000000000000002
   - Target AID: A000000291
   - Validity: 2024-01-01 to 2034-12-31
   - CA rights: 0x0A (CA + Card signing)
[OK] CA certificate generated (384 bytes):
   ...

[4/5] Adding CA certificate to store...
[OK] CA certificate added with reference: ...

[5/5] Generating Card certificate...
   - Card AID: A000000291AABBCC
   - Serial number: 0123456789ABCDEF
   - Startup info: 00112233445566
   - Validity: 2024-01-01 to 2029-12-31
[OK] Card certificate generated (316 bytes):
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