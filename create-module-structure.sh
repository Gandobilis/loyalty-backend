#!/bin/bash

# Script to create the recommended module structure
# Run this before starting the refactoring process

BASE="src/main/java/com/multi/loyaltybackend"

echo "Creating improved module structure..."

# Auth module
mkdir -p "$BASE/auth"/{controller,service,dto}

# User module
mkdir -p "$BASE/user"/{controller,service,repository,model,dto}

# Event module
mkdir -p "$BASE/event"/{controller,service,repository,model,dto}

# Storage module
mkdir -p "$BASE/storage"/{controller,service}

# Security infrastructure
mkdir -p "$BASE/security"/{jwt,oauth2,config}

# Exception organization
mkdir -p "$BASE/exception"/{handler,auth,user,event,company,voucher,storage,common}

echo "✅ Directory structure created!"
echo ""
echo "Next steps:"
echo "1. Use your IDE's Refactor → Move functionality to move classes"
echo "2. IDE will automatically update imports"
echo "3. Follow the STRUCTURE_IMPROVEMENT_GUIDE.md for detailed instructions"
echo ""
echo "Recommended order:"
echo "  1. Move exceptions (lowest risk)"
echo "  2. Move security classes"
echo "  3. Move auth module"
echo "  4. Move user module"
echo "  5. Move event module"
echo "  6. Move storage module"
