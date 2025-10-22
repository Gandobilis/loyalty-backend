# Quick Start - Auto Version Increment

Get automatic version incrementing working in under 5 minutes!

## 🚀 Quick Setup (3 Steps)

### Step 1: Enable Workflow Permissions (2 minutes)

1. Go to: `https://github.com/YOUR_USERNAME/loyalty-backend/settings/actions`
2. Under **Workflow permissions**, select:
   - ✅ **Read and write permissions**
3. Click **Save**

### Step 2: Push the Workflow Files (1 minute)

```bash
# Commit all the GitHub Actions files
git add .github/
git commit -m "ci: add automatic version increment workflow"
git push origin master
```

### Step 3: Verify It Works (2 minutes)

```bash
# Make a test commit
echo "# Test Auto-versioning" >> TEST.md
git add TEST.md
git commit -m "test: verify auto-versioning"
git push
```

**Check Results:**
1. Go to GitHub Actions tab: You should see the workflow running
2. After completion, check: `git pull && cat pom.xml | grep version`
3. Version should be `0.0.2-SNAPSHOT` (or higher)
4. Check tags: `git fetch --tags && git tag -l` (should see `v0.0.2`)

---

## ✅ That's It!

Every commit to `master`/`main` will now automatically:
- ✅ Increment version in `pom.xml`
- ✅ Create a git tag
- ✅ Push changes back to repository

---

## 📚 Version Increment Rules

```
Patch Increments:
0.0.1 → 0.0.2 → 0.0.3 → ... → 0.0.15

When Patch = 15:
0.0.15 → 0.1.0 (minor +1, patch reset to 0)

Continues:
0.1.0 → 0.1.1 → ... → 0.1.15 → 0.2.0
```

---

## 🛠️ Common Tasks

### Skip version increment for a commit
```bash
git commit -m "docs: update README [skip ci]"
```

### Manually set version
```bash
# Edit pom.xml, change version to whatever you want
git add pom.xml
git commit -m "chore: set version to 1.0.0"
git push
# Auto-increment continues from new version
```

### View all versions
```bash
git tag -l
```

### Checkout a specific version
```bash
git checkout v0.1.5
```

---

## 📖 More Documentation

- **Full Setup Guide**: `.github/SETUP_CHECKLIST.md`
- **Version Examples**: `.github/VERSION_EXAMPLES.md`
- **How It Works**: `.github/VERSIONING.md`
- **Workflow Details**: `.github/README.md`

---

## 🔍 Troubleshooting

| Problem | Solution |
|---------|----------|
| Workflow not running | Check Settings → Actions → Enable "Read and write permissions" |
| Permission errors | Same as above |
| Version didn't increment | Check Actions tab for errors |
| Script not executable | Run: `chmod +x .github/scripts/increment-version.sh` |

---

## 💡 Tips

- The workflow WON'T trigger for:
  - Changes only to `.github/**` files
  - Changes only to `README.md`
  - Changes only to `docs/**` files
  - Commits with `[skip ci]` in the message

- Each commit creates a git tag (e.g., `v0.0.2`, `v0.0.3`)
- Version in `pom.xml` includes `-SNAPSHOT`
- Git tags use version without `-SNAPSHOT`

---

**Questions?** Check the detailed docs in `.github/` directory!
