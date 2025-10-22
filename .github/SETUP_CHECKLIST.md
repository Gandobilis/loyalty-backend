# GitHub Actions Setup Checklist

Follow this checklist to enable automatic version incrementing for your repository.

## Prerequisites

- [ ] Repository is hosted on GitHub
- [ ] You have admin access to the repository
- [ ] Project uses Maven (pom.xml exists)

## Repository Setup

### 1. Enable GitHub Actions Permissions

1. Go to your repository on GitHub
2. Navigate to: **Settings** → **Actions** → **General**
3. Scroll to **Workflow permissions**
4. Select: **Read and write permissions** ✅
5. Check: **Allow GitHub Actions to create and approve pull requests** ✅
6. Click **Save**

### 2. Verify Branch Name

The workflow triggers on pushes to `master` or `main` branch.

Check your default branch:
```bash
git branch --show-current
```

If your branch is named differently, update `.github/workflows/version-increment.yml`:
```yaml
on:
  push:
    branches:
      - your-branch-name  # Change this
```

### 3. Initial Setup Commit

Commit and push the GitHub Actions files:

```bash
git add .github/
git commit -m "ci: add automatic version increment workflow"
git push origin master  # or main
```

## Testing the Setup

### Test 1: Make a Small Change

```bash
# Make a simple change
echo "# Test" >> TEST.md
git add TEST.md
git commit -m "test: verify version increment"
git push
```

**Expected Result:**
1. GitHub Actions workflow runs
2. Version increments from `0.0.1` to `0.0.2`
3. New commit appears: `chore: bump version to 0.0.2 [skip ci]`
4. Git tag `v0.0.2` is created

### Test 2: Check the Workflow

1. Go to your repository on GitHub
2. Click the **Actions** tab
3. You should see:
   - "Auto Version Increment" workflow (completed)
   - "CI Build" workflow (completed)

### Test 3: Verify Version and Tags

```bash
# Check pom.xml version
cat pom.xml | grep "<version>"

# Check git tags
git fetch --tags
git tag -l

# You should see tags like:
# v0.0.2
# v0.0.3
# etc.
```

## Troubleshooting

### ❌ Workflow Not Running

**Check:**
- [ ] GitHub Actions are enabled in repository settings
- [ ] Workflow file is in `.github/workflows/` directory
- [ ] Branch name matches the trigger in workflow file
- [ ] Commit doesn't contain `[skip ci]` in message

### ❌ Permission Denied Errors

**Fix:**
- Go to Settings → Actions → General
- Enable "Read and write permissions"
- Try pushing again

### ❌ Script Not Executable

**Fix:**
```bash
chmod +x .github/scripts/increment-version.sh
git add .github/scripts/increment-version.sh
git commit -m "ci: make version script executable"
git push
```

### ❌ xmlstarlet Not Found

The workflow installs `xmlstarlet` automatically. If you need to run locally:

**macOS:**
```bash
brew install xmlstarlet
```

**Ubuntu/Debian:**
```bash
sudo apt-get install xmlstarlet
```

**Windows (WSL):**
```bash
sudo apt-get install xmlstarlet
```

## Verification Checklist

After setup, verify:

- [ ] GitHub Actions workflow completed successfully
- [ ] `pom.xml` version was incremented
- [ ] Git tag was created (e.g., `v0.0.2`)
- [ ] Commit message includes `[skip ci]`
- [ ] No infinite loop of commits
- [ ] CI Build workflow also ran successfully

## Advanced Configuration

### Exclude Additional Paths

To prevent version increments for certain file changes, edit `.github/workflows/version-increment.yml`:

```yaml
on:
  push:
    branches:
      - master
    paths-ignore:
      - '.github/**'
      - 'README.md'
      - 'docs/**'
      - 'tests/**'        # Add this
      - '*.md'            # Add this
```

### Change Version Increment Logic

To change when the minor version increments, edit `.github/scripts/increment-version.sh`:

```bash
# Change this line (currently 15):
if [ "$PATCH" -ge 15 ]; then

# To any number you want, e.g., 20:
if [ "$PATCH" -ge 20 ]; then
```

### Disable Auto-Versioning Temporarily

To skip version increment for specific commits:

```bash
git commit -m "your message [skip ci]"
```

Or disable the workflow:
1. Go to Actions tab
2. Click "Auto Version Increment"
3. Click "..." → Disable workflow

## Need Help?

- Check workflow logs in the Actions tab
- Review `.github/VERSIONING.md` for detailed documentation
- Review `.github/VERSION_EXAMPLES.md` for version examples
- Check `.github/README.md` for workflow overview

## Success! 🎉

Once everything is working, you should see:
- Automatic version increments with each commit
- Git tags created for each version
- Clean commit history with version bumps
- No manual version management needed
