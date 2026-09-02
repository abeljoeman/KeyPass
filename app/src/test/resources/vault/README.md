# Known KDBX test vault

`known-vault.kdbx` contains synthetic, test-only data generated specifically for
the KeyPass project with its `app.keemobile:kotpass:0.13.0` dependency. No
third-party KDBX fixture file was copied or redistributed.

Test master password: `test-password`

Expected credentials:

- `123e4567-e89b-12d3-a456-426614174000` — Example Account — alice@example.com
- `223e4567-e89b-12d3-a456-426614174001` — Work Portal — alice.work

The fixture is intended for automated tests T037, T038, and later repository
persistence tests.
