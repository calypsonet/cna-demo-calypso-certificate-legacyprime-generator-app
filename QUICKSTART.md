# Quick Start Guide

## Installation

### 1. Publish Calypso Libraries to Maven Local

Before running the demo, you need to publish the Calypso libraries to your local Maven repository:

```bash
# From the parent directory calypsonet/
cd calypsonet-terminal-calypso-certificate-legacyprime-java-api
./gradlew publishToMavenLocal

cd ../calypsonet-terminal-calypso-certificate-legacyprime-java-lib
./gradlew publishToMavenLocal

cd ../cna-demo-calypso-certificate-legacyprime-generator-app
```

### 2. Compile the Project

```bash
./gradlew build
```

## Execution

### In-Memory Demo

This version generates all keys in memory:

```bash
./gradlew build
java -cp "build/classes/java/main:$HOME/.gradle/caches/modules-2/files-2.1/*/*/*.jar" \
  org.calypsonet.demo.calypso.certificate.legacyprime.generator.CalypsoCertificateDemoInMemory
```

**Expected output:**
```
================================================================================
CALYPSO LEGACY PRIME CERTIFICATE GENERATION DEMONSTRATION
================================================================================

[1/5] Generating cryptographic keys...
✓ Keys generated successfully

[2/5] Adding PCA public key to store...
✓ PCA key added: 05A0000002910000000000000000000000000000000000000000000001

[3/5] Generating CA certificate...
   - CA key reference: 05A0000002910000000000000000000000000000000000000000000002
   - Target AID: A000000291
   - Validity: 2024-01-01 to 2034-12-31
   - CA rights: 0x0A (CA + Card signing)
✓ CA certificate generated (384 bytes):
   [certificate in hexadecimal]

[4/5] Adding CA certificate to store...
✓ CA certificate added with reference: ...

[5/5] Generating Card certificate...
   - Card AID: A000000291AABBCC
   - Serial number: 0123456789ABCDEF
   - Startup info: 00112233445566
   - Validity: 2024-01-01 to 2029-12-31
✓ Card certificate generated (316 bytes):
   [certificate in hexadecimal]

================================================================================
DEMONSTRATION COMPLETED SUCCESSFULLY
================================================================================
```

### Advanced Demo (With PEM Files)

This version saves and loads keys from files:

```bash
java -cp "build/classes/java/main:$HOME/.gradle/caches/modules-2/files-2.1/*/*/*.jar" \
  org.calypsonet.demo.calypso.certificate.legacyprime.generator.CalypsoCertificateDemoWithFiles
```

This demo creates a `keys/` directory containing:
- `pca-private.pem` - PCA private key
- `ca-private.pem` - CA private key

### Self-Signed CA Demo

This version demonstrates a self-signed CA setup:

```bash
java -cp "build/classes/java/main:$HOME/.gradle/caches/modules-2/files-2.1/*/*/*.jar" \
  org.calypsonet.demo.calypso.certificate.legacyprime.generator.CalypsoCertificateDemoSelfSigned
```

## Structure of Generated Certificates

### CA Certificate (384 bytes)

| Field | Size | Description |
|---|---|---|
| Type | 1 byte | 0x90 (CA certificate) |
| Version | 1 byte | 0x01 |
| Issuer key reference | 29 bytes | Identifies the PCA key |
| Target CA key reference | 29 bytes | Identifies the new CA key |
| CA public key | 256 bytes | RSA 2048-bit modulus |
| Dates & rights | ~68 bytes | Validity, rights, scope, AID |
| **Total data** | **128 bytes** | |
| **RSA Signature** | **256 bytes** | ISO 9796-2 PSS signature |

### Card Certificate (316 bytes)

| Field | Size | Description |
|---|---|---|
| Type | 1 byte | 0x91 (Card certificate) |
| Version | 1 byte | 0x01 |
| Issuer key reference | 29 bytes | Identifies the CA key |
| ECC public key | 64 bytes | secp256r1 (X + Y) |
| Other fields | ~25 bytes | |
| **Total non-recoverable data** | **60 bytes** | |
| **Recoverable data** | **222 bytes** | Integrated into the signature |
| **RSA Signature** | **256 bytes** | With message recovery |

## Key Points

### CA Rights

The CA rights byte contains 2 fields of 2 bits each:
- **Bits 0-1**: Right to sign Card certificates
- **Bits 2-3**: Right to sign CA certificates

Possible values for each field:
- `0b00` (0x0): NOT_SPECIFIED
- `0b01` (0x1): SHALL_NOT_SIGN
- `0b10` (0x2): MAY_SIGN
- `0b11` (0x3): Reserved (RFU)

**Examples:**
- `0x0A` (0b1010): May sign CA and Card
- `0x08` (0b1000): May sign Card only
- `0x02` (0b0010): May sign CA only

### Key References (29 bytes)

Format:
```
Offset  | Size   | Description
--------|--------|-------------
0       | 1 byte | AID length
1-16    | 5-16 b | AID (Application Identifier)
17-24   | 8 b    | Serial number
25-28   | 4 b    | Key ID
```

### Cryptographic Keys

- **RSA**: 2048-bit with public exponent 65537 (0x10001)
- **ECC**: secp256r1 curve (NIST P-256)

## Troubleshooting

### Error: "cannot find symbol DefaultCalypsoCertificateLegacyPrimeSigner"

The Calypso libraries are not installed. Run:
```bash
cd ../calypsonet-terminal-calypso-certificate-legacyprime-java-api
./gradlew publishToMavenLocal

cd ../calypsonet-terminal-calypso-certificate-legacyprime-java-lib
./gradlew publishToMavenLocal
```

### Error: "Certificate right value 0b11 is reserved"

The CA rights value is invalid. Use `0x0A` to allow signing of CA and Card certificates.

### Character Encoding Issues

The project is configured for UTF-8. If you see strange characters, check your terminal's encoding.

## Next Steps

- Consult the source code to understand how the APIs are used
- Read README.md for more details
- Explore the integration tests in `calypsonet-terminal-calypso-certificate-legacyprime-java-lib`
- Consult the API documentation in the source files

## Support

For questions or issues:
- Consult the documentation in the API and Library projects
- Check the integration tests for usage examples
- Contact [Calypso Networks Association](https://calypsonet.org/)