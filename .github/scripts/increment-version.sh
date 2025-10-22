#!/bin/bash

# Script to increment version in pom.xml
# Logic: Increment by 0.0.1 up to x.x.15, then increment minor version (x.x.15 -> x.(x+1).0)

set -e

POM_FILE="pom.xml"

# Extract current version from pom.xml
CURRENT_VERSION=$(xmlstarlet sel -t -v '/_:project/_:version' "$POM_FILE" | sed 's/-SNAPSHOT//')

echo "Current version: $CURRENT_VERSION"

# Split version into major, minor, patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Increment logic
if [ "$PATCH" -ge 15 ]; then
  # If patch is 15 or greater, increment minor and reset patch to 0
  MINOR=$((MINOR + 1))
  PATCH=0
  echo "Patch version reached 15, incrementing minor version"
else
  # Otherwise, increment patch
  PATCH=$((PATCH + 1))
  echo "Incrementing patch version"
fi

# Construct new version
NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"

echo "New version: $NEW_VERSION"

# Update pom.xml with new version (keeping -SNAPSHOT suffix)
xmlstarlet ed --inplace \
  -N x=http://maven.apache.org/POM/4.0.0 \
  -u "/x:project/x:version" \
  -v "${NEW_VERSION}-SNAPSHOT" \
  "$POM_FILE"

echo "Version updated to ${NEW_VERSION}-SNAPSHOT in pom.xml"

# Output for GitHub Actions
echo "new_version=${NEW_VERSION}" >> $GITHUB_OUTPUT
