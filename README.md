# capacitor-notification-reader

[![npm version](https://badge.fury.io/js/capacitor-notification-reader.svg)](https://badge.fury.io/js/capacitor-notification-reader)
[![npm downloads](https://img.shields.io/npm/dm/capacitor-notification-reader.svg)](https://www.npmjs.com/package/capacitor-notification-reader)

Capacitor plugin to read active notifications on Android

## Install

```bash
npm install capacitor-notification-reader
npx cap sync
```

## API

<docgen-index>

* [`getActiveNotifications()`](#getactivenotifications)
* [`openAccessSettings()`](#openaccesssettings)
* [`isAccessEnabled()`](#isaccessenabled)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getActiveNotifications()

```typescript
getActiveNotifications() => Promise<GetActiveNotificationsResult>
```

Gets all active notifications from the notification listener service.

**Returns:** <code>Promise&lt;<a href="#getactivenotificationsresult">GetActiveNotificationsResult</a>&gt;</code>

**Since:** 1.0.0

--------------------


### openAccessSettings()

```typescript
openAccessSettings() => Promise<void>
```

Opens the system settings page to allow the user to grant notification access
to the app.

**Since:** 1.0.0

--------------------


### isAccessEnabled()

```typescript
isAccessEnabled() => Promise<{ enabled: boolean; }>
```

Checks if the app has notification access enabled.

**Returns:** <code>Promise&lt;{ enabled: boolean; }&gt;</code>

**Since:** 1.0.0

--------------------


### Interfaces


#### GetActiveNotificationsResult

Result returned by getActiveNotifications.

| Prop                | Type                            | Description                    |
| ------------------- | ------------------------------- | ------------------------------ |
| **`notifications`** | <code>NotificationItem[]</code> | Array of active notifications. |


#### NotificationItem

Represents a notification item.

| Prop            | Type                        | Description                                                       |
| --------------- | --------------------------- | ----------------------------------------------------------------- |
| **`app`**       | <code>string</code>         | The package name of the app that posted the notification.         |
| **`title`**     | <code>string \| null</code> | The title of the notification.                                    |
| **`text`**      | <code>string \| null</code> | The text content of the notification.                             |
| **`timestamp`** | <code>number</code>         | The timestamp when the notification was posted (in milliseconds). |

</docgen-api>

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## Release Process

This package uses [npm Trusted Publishers](https://docs.npmjs.com/trusted-publishers) with OIDC for secure, automated releases. Releases are triggered automatically when commits are pushed to the `main` branch.

### Setup (One-time)

Configure the trusted publisher on npm:

1. Go to your package settings on [npmjs.com](https://www.npmjs.com/package/capacitor-notification-reader)
2. Navigate to "Publishing access" → "Trusted Publisher"
3. Select "GitHub Actions"
4. Configure:
   - **Organization/User**: `WhyAsh5114`
   - **Repository**: `capacitor-notification-reader`
   - **Workflow filename**: `release.yml`

No npm tokens needed! The workflow uses OpenID Connect (OIDC) for authentication.

### Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/) for your commit messages:

- `feat:` - New features (triggers minor version bump)
- `fix:` - Bug fixes (triggers patch version bump)
- `docs:` - Documentation changes
- `chore:` - Maintenance tasks
- `BREAKING CHANGE:` - Breaking changes (triggers major version bump)

Example:
```bash
git commit -m "feat: add support for notification icons"
git commit -m "fix: resolve crash when notification has no title"
```

The CI pipeline will automatically:
1. Build the package
2. Determine version bump based on commits
3. Create a GitHub release with changelog
4. Publish to npm with provenance attestation
