# ET CCD Definitions - Unified Workspace

This repository contains consolidated CCD (Case and Content Data) definitions for Employment Tribunals (ET) across all jurisdictions in a modern, maintainable workspace structure.

## 🚀 **Quick Start**

```bash
# Install dependencies
yarn install

# Build all definitions for local environment
yarn generate-excel:local

# Build all definitions for AAT environment  
yarn generate-excel:aat

# Build all definitions for production
yarn generate-excel:prod
```

## 📁 **Repository Structure**

This repository uses a **unified workspace architecture** that combines three previously separate repositories:

- `et-ccd-definitions-admin` - Administrative configurations
- `et-ccd-definitions-englandwales` - England & Wales specific configurations  
- `et-ccd-definitions-scotland` - Scotland specific configurations

### **Directory Layout**

```
ccd-definitions/
├── jurisdictions/               # Main content area - CCD definitions by jurisdiction
│   ├── admin/                   # Administrative configurations
│   │   ├── json/                # JSON definition files
│   │   ├── xlsx/                # Generated Excel files
│   │   └── package.json         # Package configuration
│   ├── england-wales/           # England & Wales configurations
│   │   ├── json/                # JSON definition files
│   │   ├── xlsx/                # Generated Excel files
│   │   ├── data/                # Template data files
│   │   └── package.json         # Package configuration
│   └── scotland/                # Scotland configurations
│       ├── json/                # JSON definition files
│       ├── xlsx/                # Generated Excel files
│       ├── data/                # Template data files
│       └── package.json         # Package configuration
├── tools/                       # Centralized build tools
│   ├── build-package.js         # Individual package builder
│   ├── build-workspace.js       # Workspace-wide builder
│   └── ccd-definition-processor/ # CCD processing engine
├── tests/                       # All test files
│   ├── functional/              # Functional tests
│   ├── smoke/                   # Smoke tests
│   ├── unit/                    # Unit tests
│   ├── england-wales/           # England & Wales specific tests
│   └── scotland/                # Scotland specific tests
├── scripts/                     # Organized scripts
│   ├── deployment/              # Deployment and setup scripts
│   ├── environment/             # Environment configuration scripts
│   └── legacy/                  # Legacy scripts (for migration)
├── bin/                         # Executable scripts
│   └── preview/                 # Preview environment API setup scripts
├── configs/                     # All configuration files
│   ├── build.config.js          # Build configuration
│   ├── eslint.config.js         # ESLint configuration
│   ├── prettier.config.js       # Prettier configuration
│   ├── default.yaml             # Default server configuration
│   ├── renovate.json            # Renovate configuration
│   ├── sonar-project.properties # SonarQube configuration
│   └── yarn-audit-known-issues  # Known security issues
├── dist/                        # Build outputs (by environment)
│   ├── local/                   # Local environment builds
│   ├── demo/                    # Demo environment builds
│   ├── aat/                     # AAT environment builds
│   └── prod/                    # Production environment builds
├── docs/                        # Documentation
├── Dockerfile                   # Container configuration
├── Jenkinsfile_CNP              # CI/CD pipeline configuration
├── LICENSE                      # License file
└── package.json                 # Workspace root configuration
```

## 🛠️ **Prerequisites**

- **Node.js** >= 18.0.0
- **Yarn** >= 3.0.0

### **Windows Users**

Windows users must use **Windows Subsystem for Linux (WSL 2)** for local development:
- Install WSL 2: https://docs.microsoft.com/en-us/windows/wsl/install
- Install Node.js via nvm: https://docs.microsoft.com/en-us/windows/dev-environment/javascript/nodejs-on-wsl

## 📦 **Installation**

```bash
# Initial setup
yarn install

# Or use the convenience command
yarn setup
```

## 🏗️ **Building CCD Definitions**

### **Environment-Specific Builds**

The workspace supports multiple environments with optimized configurations:

```bash
# Local development
yarn generate-excel:local

# Demo environment
yarn generate-excel:demo

# AAT (Acceptance Testing)
yarn generate-excel:aat

# Production
yarn generate-excel:prod

# Preview environment
yarn generate-excel:preview
```

### **Individual Package Builds**

Build specific jurisdiction packages individually:

```bash
# Direct build tool (recommended for development)
node tools/build-package.js admin --env=local
node tools/build-package.js england-wales --env=aat
node tools/build-package.js scotland --env=prod

# Yarn workspace commands
yarn workspace admin build --env local
yarn workspace scotland build --env aat
```

### **Output Files**

Generated Excel files are organized by environment in the `dist/` directory:

```
dist/
├── local/
│   ├── et-admin-ccd-config-local.xlsx
│   ├── et-englandwales-ccd-config-local.xlsx
│   └── et-scotland-ccd-config-local.xlsx
├── aat/
│   ├── et-admin-ccd-config-aat.xlsx
│   ├── et-englandwales-ccd-config-aat.xlsx
│   └── et-scotland-ccd-config-aat.xlsx
└── prod/
    ├── et-admin-ccd-config-prod.xlsx
    ├── et-englandwales-ccd-config-prod.xlsx
    └── et-scotland-ccd-config-prod.xlsx
```

## 🔧 **Development**

### **Code Quality**

```bash
# Lint all packages
yarn lint

# Fix linting issues
yarn lint:fix

# Format code
yarn format

# Check formatting
yarn format:check
```

### **Testing**

```bash
# Run all tests
yarn test

# Test specific packages
yarn test:admin
yarn test:england-wales
yarn test:scotland

# Functional tests
yarn test:functional

# Smoke tests
yarn test:smoke
```

### **Converting Excel to JSON**

If you need to make changes via Excel and convert back to JSON:

```bash
# 1. Generate base Excel file
yarn generate-excel:local

# 2. Edit the Excel file (ensure no environment-specific URLs)

# 3. Convert back to JSON
yarn generate-json

# 4. Review changes
git diff
```

## ⚙️ **Environment Configuration**

### **Variable Substitution**

The `json2xlsx` processor replaces variables in JSON files with environment values. Variables must start with `CCD_DEF` prefix:

**Example:**
```json
{
  "CallBackURLSubmittedEvent": "${CCD_DEF_BASE_URL}/callback"
}
```

With `CCD_DEF_BASE_URL=http://localhost`, becomes:
```json
{
  "CallBackURLSubmittedEvent": "http://localhost/callback"
}
```

### **Required Environment Variables**

| Variable | Default | Description |
|----------|---------|-------------|
| `CCD_DEF_CASE_SERVICE_BASE_URL` | - | Base URL for CCD Data Store API |
| `CCD_DEF_CCD_ADMIN_WEB_INGRESS_URL` | - | Admin web URL |
| `IDAM_ADMIN_WEB_SERVICE_KEY` | - | Admin web IDAM service-to-service secret |
| `IDAM_ADMIN_SECRET` | - | Admin web IDAM secret |
| `S2S_SECRET` | - | Shared secret for S2S authentication |

## 🐳 **Preview Environment**

Set up a complete local CCD environment using the robust API-based preview scripts:

```bash
# Setup preview environment
./bin/preview/setup-preview-environment.sh

# Individual setup steps
./bin/preview/create-ccd-roles.sh
./bin/preview/import-ccd-definitions.sh
./bin/preview/create-admin-case.sh
./bin/preview/import-ref-data.sh
./bin/preview/add-org-roles-to-users.sh
```

See [Preview Environment Documentation](docs/preview-environment.md) for detailed setup instructions.

## 🚀 **Deployment**

### **CI/CD Integration**

The workspace is designed for modern CI/CD pipelines:

```bash
# Build for specific environment
ET_ENV=aat yarn generate-excel:aat

# Deploy to AAT
yarn deploy:aat

# Deploy to production
yarn deploy:prod
```

### **Azure Key Vault**

Secrets are stored in Azure Key Vault. Configure your pipeline to inject these as environment variables.

## 🏗️ **Architecture**

### **Workspace Benefits**

- **Centralized Build System**: Unified build tools for all packages
- **Shared Configuration**: ESLint, Prettier, and build configs
- **Environment Management**: Consistent environment handling
- **Dependency Management**: Shared dependencies and tooling
- **Scalable Structure**: Easy to add new jurisdictions

### **Build Tools**

1. **`tools/build-package.js`**: Builds individual packages with environment-specific configurations
2. **`tools/build-workspace.js`**: Orchestrates builds across all packages
3. **`tools/ccd-definition-processor/`**: Core CCD definition processing engine

### **Package Management**

Each package (`admin`, `england-wales`, `scotland`) maintains:
- Independent `package.json` with jurisdiction-specific scripts
- Own test suites and configurations
- Shared build tools and configurations from workspace root

## 🧹 **Maintenance**

### **Cleanup Commands**

```bash
# Clean build artifacts
yarn clean

# Clean dependencies
yarn clean:deps

# Reset workspace
yarn reset
```

### **Adding New Jurisdictions**

1. Create new jurisdiction directory at the root level
2. Add package configuration to `configs/build.config.js`
3. Create `package.json` with build scripts
4. Add JSON definition files in `json/` subdirectory
5. Update workspace scripts in root `package.json`

## 🔍 **Troubleshooting**

### **Common Issues**

1. **Yarn workspace resolution errors**: Run `yarn install` to refresh lockfile
2. **Build failures**: Check environment variables and package configurations
3. **Permission errors**: Ensure scripts have execute permissions
4. **Template warnings**: These are normal - JSON files without Excel templates are skipped

### **Debug Mode**

```bash
# Verbose build output
DEBUG=* yarn generate-excel:local

# Individual package debugging
node tools/build-package.js admin --env=local
```

## 📚 **Documentation**

- [Preview Environment Setup](docs/preview-environment.md)
- [Migration Guide](docs/migration.md)
- [API Reference](docs/api.md)
- [Contributing Guidelines](docs/contributing.md)

## 🤝 **Contributing**

1. Ensure all tests pass: `yarn test`
2. Follow code style: `yarn lint:fix && yarn format`
3. Update documentation for significant changes
4. Test builds for all environments

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏛️ **HMCTS Integration**

This repository is part of the HMCTS (HM Courts & Tribunals Service) digital transformation initiative, providing modern case management capabilities for Employment Tribunals across England, Wales, and Scotland.

---

**Need Help?** Check the troubleshooting section above or raise an issue in the repository.
