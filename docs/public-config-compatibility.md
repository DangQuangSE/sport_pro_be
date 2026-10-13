# Public configuration compatibility

## Public content contract

Anonymous reads are served by `/api/public-configs`. A row is eligible only
when it is `PUBLIC`, active, and present in the allowlist maintained by
`SettingDefinitionRegistry`. Locale-specific copy uses a separate key with a
`.vi` or `.en` suffix, for example `footer.description.vi`.

The frontend consumes this endpoint through its typed public-content map and
keeps dictionary copy as the fallback when an optional content row is absent.
Pricing, membership, payment, and infrastructure settings remain internal.

## Compatibility route

`/api/admin/public-configs` remains available as an administrative alias during
the transition to `/api/admin/settings`. It uses the same validation,
optimistic-version checks, audit logging, and delete `expectedVersion` guard.
New integrations should use `/api/admin/settings`.

## Rollback boundary

Deactivating a public content key or rolling back its frontend consumer does not
modify order pricing snapshots. Historical orders continue to use their stored
`pricingSnapshotStatus` and aggregate legacy discount value.
