# Version Increment Examples

This document shows examples of how the automatic version increment system works.

## Starting Version: 0.0.1

### Patch Increments (0.0.1 to 0.0.15)

| Commit # | Previous Version | New Version | Action |
|----------|-----------------|-------------|---------|
| 1 | 0.0.1 | 0.0.2 | Patch +1 |
| 2 | 0.0.2 | 0.0.3 | Patch +1 |
| 3 | 0.0.3 | 0.0.4 | Patch +1 |
| 4 | 0.0.4 | 0.0.5 | Patch +1 |
| 5 | 0.0.5 | 0.0.6 | Patch +1 |
| 6 | 0.0.6 | 0.0.7 | Patch +1 |
| 7 | 0.0.7 | 0.0.8 | Patch +1 |
| 8 | 0.0.8 | 0.0.9 | Patch +1 |
| 9 | 0.0.9 | 0.0.10 | Patch +1 |
| 10 | 0.0.10 | 0.0.11 | Patch +1 |
| 11 | 0.0.11 | 0.0.12 | Patch +1 |
| 12 | 0.0.12 | 0.0.13 | Patch +1 |
| 13 | 0.0.13 | 0.0.14 | Patch +1 |
| 14 | 0.0.14 | 0.0.15 | Patch +1 |

### Minor Increment (When Patch = 15)

| Commit # | Previous Version | New Version | Action |
|----------|-----------------|-------------|---------|
| 15 | 0.0.15 | **0.1.0** | **Minor +1, Patch reset to 0** |

### Next Cycle (0.1.0 to 0.1.15)

| Commit # | Previous Version | New Version | Action |
|----------|-----------------|-------------|---------|
| 16 | 0.1.0 | 0.1.1 | Patch +1 |
| 17 | 0.1.1 | 0.1.2 | Patch +1 |
| ... | ... | ... | ... |
| 29 | 0.1.14 | 0.1.15 | Patch +1 |
| 30 | 0.1.15 | **0.2.0** | **Minor +1, Patch reset to 0** |

### Multiple Minor Increments

| Commit # | Previous Version | New Version | Action |
|----------|-----------------|-------------|---------|
| 30 | 0.1.15 | 0.2.0 | Minor +1, Patch reset |
| 45 | 0.2.15 | 0.3.0 | Minor +1, Patch reset |
| 60 | 0.3.15 | 0.4.0 | Minor +1, Patch reset |
| 75 | 0.4.15 | 0.5.0 | Minor +1, Patch reset |
| ... | ... | ... | ... |
| 150 | 0.9.15 | 0.10.0 | Minor +1, Patch reset |

## Manual Major Version Increment

To manually increment the major version (e.g., for a major release):

1. Edit `pom.xml` manually:
   ```xml
   <version>1.0.0-SNAPSHOT</version>
   ```

2. Commit and push:
   ```bash
   git add pom.xml
   git commit -m "chore: bump to major version 1.0.0"
   git push
   ```

3. Future automatic increments will continue from this version:
   - 1.0.0 → 1.0.1 → ... → 1.0.15 → 1.1.0

## Git Tags Created

For each version, a Git tag is created (without `-SNAPSHOT` suffix):

```
v0.0.1
v0.0.2
v0.0.3
...
v0.0.15
v0.1.0
v0.1.1
...
```

## In pom.xml

The version in `pom.xml` always includes the `-SNAPSHOT` suffix:

```xml
<version>0.0.1-SNAPSHOT</version>
<version>0.0.2-SNAPSHOT</version>
<version>0.0.15-SNAPSHOT</version>
<version>0.1.0-SNAPSHOT</version>
```

The Git tags use the version without `-SNAPSHOT`:

```
v0.0.1
v0.0.2
v0.1.0
```

## Workflow Behavior

### Normal Commit
```bash
git commit -m "feat: add new feature"
git push
```
**Result**: Version increments automatically, workflow runs, creates tag

### Version Commit (Auto-generated)
```bash
git commit -m "chore: bump version to 0.0.2 [skip ci]"
```
**Result**: No workflow trigger (due to `[skip ci]`)

### Excluded Path Changes
```bash
git commit -m "docs: update README"
git push
```
**Result**: No version increment (README.md is excluded)
