#!/bin/bash

# Project Structure Refactoring Script
# This script reorganizes the project into a clean modular structure

BASE_DIR="src/main/java/com/multi/loyaltybackend"

echo "Creating new directory structure..."

# Create auth module
mkdir -p $BASE_DIR/auth/{controller,service,dto}

# Create user module
mkdir -p $BASE_DIR/user/{controller,service,repository,model,dto}

# Create event module
mkdir -p $BASE_DIR/event/{controller,service,repository,model,dto}

# Create storage module
mkdir -p $BASE_DIR/storage/{controller,service}

# Create security package
mkdir -p $BASE_DIR/security/{jwt,oauth2}

# Create organized exception structure
mkdir -p $BASE_DIR/exception/{handler,auth,user,event,company,voucher,storage,common}

echo "Directory structure created successfully!"
echo ""
echo "Next steps (manual):"
echo "1. Move files to appropriate modules"
echo "2. Update package declarations"
echo "3. Update imports"
echo "4. Test compilation"
