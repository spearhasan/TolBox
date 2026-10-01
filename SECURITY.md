# Security Policy

## Supported Versions
Security updates are actively maintained on the latest stable releases of ToolBox.

## Security Practices
1. **Local Isolation**: All file operations, archive extractions, and script parsers execute locally within Android application sandbox constraints.
2. **Zip Slip Prevention**: Archive extraction implementations explicitly validate directory paths against canonical base folders before extracting any files, preventing directory traversal attacks.
3. **Safe Storage**: Output files use Android FileProvider with private grants rather than world-readable permissions.
4. **No Arbitrary Code Execution**: No dynamic code loading (`.dex`, `.jar`, `.so`) is allowed.

## Reporting a Vulnerability
If you discover a security vulnerability within ToolBox, please open a confidential security advisory or reach out to the project maintainers with details and reproduction steps.
