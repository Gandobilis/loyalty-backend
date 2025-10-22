# Automatic Versioning System

This project uses an automatic version increment system powered by GitHub Actions.

## How It Works

Every commit pushed to the `master` or `main` branch automatically increments the version number in `pom.xml`.

## Versioning Logic

The version follows the format: `MAJOR.MINOR.PATCH`

### Increment Rules:
- **Patch increment**: Increments by 0.0.1 for each commit
- **Threshold**: When PATCH reaches 15 (e.g., `1.0.15`)
- **Minor increment**: Automatically increments MINOR and resets PATCH to 0 (e.g., `1.0.15` → `1.1.0`)

### Examples:
```
0.0.1 → 0.0.2 → 0.0.3 → ... → 0.0.15 → 0.1.0 → 0.1.1 → ... → 0.1.15 → 0.2.0
```

## Workflow Details

### Trigger
- **Events**: Push to `master` or `main` branch
- **Excluded paths**:
  - `.github/**` (to prevent infinite loops)
  - `README.md`
  - `docs/**`

### Process
1. Checkout code
2. Read current version from `pom.xml`
3. Apply increment logic (patch +1 or minor +1 with patch reset)
4. Update `pom.xml` with new version
5. Commit changes with message: `chore: bump version to X.Y.Z [skip ci]`
6. Create Git tag `vX.Y.Z` (without -SNAPSHOT suffix)
7. Push commit and tag to repository

### Skip CI
The version commit includes `[skip ci]` to prevent the workflow from triggering again on the version bump commit itself.

## Manual Version Changes

If you need to manually set a version:

1. Edit `pom.xml` and change the version
2. Commit and push
3. The automatic increment will continue from the new version

## Git Tags

Each version increment creates a corresponding Git tag (e.g., `v0.0.2`, `v0.1.0`) for easy reference and release management.

## Files Modified

- **Workflow**: `.github/workflows/version-increment.yml`
- **Script**: `.github/scripts/increment-version.sh`
- **Version source**: `pom.xml`
