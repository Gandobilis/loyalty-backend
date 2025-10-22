# GitHub Actions Documentation Index

Welcome to the GitHub Actions setup for automatic version management!

## 📋 Start Here

**New to this setup?** Start with:
1. **[QUICK_START.md](QUICK_START.md)** - Get running in 5 minutes
2. **[SETUP_CHECKLIST.md](SETUP_CHECKLIST.md)** - Complete setup guide with troubleshooting

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| **[QUICK_START.md](QUICK_START.md)** | 5-minute setup guide |
| **[SETUP_CHECKLIST.md](SETUP_CHECKLIST.md)** | Detailed setup with troubleshooting |
| **[VERSIONING.md](VERSIONING.md)** | How the versioning system works |
| **[VERSION_EXAMPLES.md](VERSION_EXAMPLES.md)** | Real examples of version increments |
| **[README.md](README.md)** | Workflow and script overview |

## 🔧 Files

| File | Purpose |
|------|---------|
| **workflows/version-increment.yml** | Main auto-version workflow |
| **workflows/ci.yml** | Build and test workflow |
| **scripts/increment-version.sh** | Version increment logic |

## 🎯 Quick Reference

### Version Increment Pattern
```
0.0.1 → 0.0.2 → ... → 0.0.15 → 0.1.0 → 0.1.1 → ... → 0.1.15 → 0.2.0
```

### Key Commands
```bash
# View current version
cat pom.xml | grep "<version>"

# View all version tags
git tag -l

# Skip version increment
git commit -m "your message [skip ci]"

# Test script locally
./.github/scripts/increment-version.sh
```

### GitHub Setup
1. Settings → Actions → General
2. Workflow permissions → "Read and write permissions"
3. Save

## 📖 Read More

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven POM Reference](https://maven.apache.org/pom.html)
- [Semantic Versioning](https://semver.org/)

---

**Questions?** Check [SETUP_CHECKLIST.md](SETUP_CHECKLIST.md) for troubleshooting.
