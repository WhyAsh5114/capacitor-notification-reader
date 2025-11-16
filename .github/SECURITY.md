# Security Policy

## Supported Versions

Currently being supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 0.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in this plugin, please follow these steps:

1. **Do not** open a public issue
2. Email the maintainer directly (check package.json for contact info)
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

## Security Considerations

This plugin requests sensitive permissions:

- **Notification Listener Access**: Allows reading all notifications from all apps on the device

### Best Practices for Users

1. Only use this plugin if you absolutely need notification reading functionality
2. Clearly communicate to your users why you need this permission
3. Handle notification data securely and never transmit sensitive information without encryption
4. Respect user privacy and comply with applicable data protection regulations (GDPR, CCPA, etc.)
5. Consider implementing opt-in flows and allowing users to revoke access easily

### Privacy Notice

Apps using this plugin should include a clear privacy notice explaining:
- What notification data is collected
- How it is used
- Where it is stored
- How users can revoke access
