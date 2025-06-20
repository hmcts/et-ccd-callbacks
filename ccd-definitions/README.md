# ET CCD Definitions - Clean Workspace

🧹 **Cleaned and Optimized Repository**

This repository contains consolidated CCD (Case and Content Data) definitions for Employment Tribunals (ET) across all jurisdictions. The project has been recently cleaned up and optimized, removing all test files, obsolete configurations, and unnecessary documentation to focus purely on CCD definition generation.

## 🚀 **Quick Start**

```bash
# Install dependencies
yarn install

# Build all definitions for local environment
yarn generate-excel:local

# Build for specific environments
yarn generate-excel:aat
yarn generate-excel:prod
yarn generate-excel:preview
```

## 📁 **Current Repository Structure**

This streamlined workspace combines three jurisdiction-specific CCD definition packages:

### **Directory Layout**

```
ccd-definitions/
├── jurisdictions/               # CCD definitions by jurisdiction
│   ├── admin/                   # Administrative configurations
│   │   ├── json/                # JSON definition files
│   │   ├── xlsx/                # Generated Excel files (gitignored)
│   │   ├── data/                # Template data files
│   │   └── package.json         # Package configuration
│   ├── england-wales/           # England & Wales configurations
│   │   ├── json/                # JSON definition files
│   │   ├── xlsx/                # Generated Excel files (gitignored)
│   │   ├── data/                # Template data files
│   │   └── package.json         # Package configuration
│   └── scotland/                # Scotland configurations
│       ├── json/                # JSON definition files
│       ├── xlsx/                # Generated Excel files (gitignored)
│       ├── data/                # Template data files
│       └── package.json         # Package configuration
├── tools/                       # Build and processing tools
│   ├── build-package.js         # Individual package builder
│   ├── build-workspace.js       # Workspace-wide builder
│   └── ccd-definition-processor/ # CCD processing engine
├── scripts/                     # Environment and deployment scripts
│   ├── deployment/              # Deployment and setup scripts
│   └── environment/             # Environment configuration
├── configs/                     # Configuration files
│   ├── build.config.js          # Build configuration
│   ├── eslint.config.js         # ESLint configuration
│   ├── prettier.config.js       # Prettier configuration
│   └── default.yaml             # Server configuration
├── dist/                        # Build outputs (by environment)
│   ├── local/                   # Local environment builds
│   ├── cftlib/                  # CFT Lib environment builds
│   ├── demo/                    # Demo environment builds
│   ├── aat/                     # AAT environment builds
│   ├── prod/                    # Production environment builds
│   └── preview/                 # Preview environment builds
├── .yarn/                       # Yarn v3 configuration
├── node_modules/                # Dependencies (gitignored)
├── Dockerfile                   # Container configuration
├── Jenkinsfile_CNP              # CI/CD pipeline configuration
├── index.js                     # Express server entry point
├── validate-builds.js           # Build validation script
├── catalog-info.yaml            # Service catalog metadata
├── LICENSE                      # MIT License
└── package.json                 # Workspace configuration
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

The workspace supports multiple environments with optimized configurations. Each environment has both **nonprod** (default) and **prod** variants that handle file exclusions differently:

#### **Nonprod Builds (Default)**
These exclude files ending with `-prod.json` and include nonprod-specific files:

```bash
# Local development (nonprod)
yarn generate-excel:local

# Demo environment (nonprod) 
yarn generate-excel:demo

# AAT environment (nonprod)
yarn generate-excel:aat

# Preview environment (nonprod)
yarn generate-excel:preview
```

#### **Prod Builds**
These exclude files ending with `-nonprod.json` and include only production-ready configurations:

```bash
# Local development (prod)
yarn generate-excel:local-prod

# Demo environment (prod)
yarn generate-excel:demo-prod

# AAT environment (prod)
yarn generate-excel:aat-prod

# Production (prod)
yarn generate-excel:prod

# Preview environment (prod)
yarn generate-excel:preview-prod
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

## 🛠️ **CLI Wrappers**

The repository provides specialized CLI wrapper scripts for direct conversion between JSON and Excel formats. These wrappers provide fine-grained control over the CCD definition processing pipeline.

### **Available Wrappers**

#### **json2xlsx - JSON to Excel Converter**

**Location:** `bin/json2xlsx`

**Purpose:** Converts JSON CCD definition files to Excel format with environment-specific variable substitution.

**Usage:**
```bash
# Basic usage
./bin/json2xlsx -D <json-directory> -o <output-excel-file>

# Example: Convert admin JSON files to Excel
./bin/json2xlsx -D "jurisdictions/admin/json" -o "dist/local/et-admin-ccd-config-local.xlsx"

# With exclusions
./bin/json2xlsx -D "jurisdictions/england-wales/json" -o "output.xlsx" -e "*test*,*temp*"
```

**Required Arguments:**
- `-D, --sheetsDir`: Path to directory containing JSON definition files
- `-o, --output`: Output Excel file path

**Optional Arguments:**
- `-e, --exclude`: Comma-separated patterns for files to exclude from processing

#### **xlsx2json - Excel to JSON Converter**

**Location:** `bin/xlsx2json`

**Purpose:** Converts Excel CCD definition files back to JSON format for editing and version control.

**Usage:**
```bash
# Basic usage
./bin/xlsx2json -i <input-excel-file> -D <output-json-directory>

# Example: Convert Excel back to JSON
./bin/xlsx2json -i "dist/local/et-admin-ccd-config-local.xlsx" -D "jurisdictions/admin/json"

# With exclusions
./bin/xlsx2json -i "config.xlsx" -D "json/" -e "*backup*,*old*"
```

**Required Arguments:**
- `-i, --input`: Input Excel file path
- `-D, --sheetsDir`: Output directory for generated JSON files

**Optional Arguments:**
- `-e, --exclude`: Comma-separated patterns for sheets to exclude from processing

### **Integration with Build System**

The CLI wrappers are integral components of the workspace build system:

#### **Build Tool Integration**

1. **Individual Package Builds** (`tools/build-package.js`):
   - Calls `json2xlsx` wrapper directly for each jurisdiction package
   - Uses environment-specific configurations from `configs/build.config.js`
   - Command: `node "${processorPath}/bin/json2xlsx" -D "${jsonPath}" -o "${outputPath}"`

2. **Workspace Builds** (`tools/build-workspace.js`):
   - Orchestrates multiple package builds using the individual build tool
   - Manages environment variables and output organization

#### **NPM Script Integration**

The wrappers are used by high-level NPM scripts defined in `package.json`:

```bash
# These scripts internally use the CLI wrappers:
yarn generate-excel:local    # Uses json2xlsx for local environment
yarn generate-excel:aat      # Uses json2xlsx for AAT environment
yarn generate-excel:prod     # Uses json2xlsx for production
yarn generate-json           # Uses xlsx2json for reverse conversion
```

#### **Environment Variable Processing**

Both wrappers respect environment variables for configuration:
- `ET_ENV`: Current environment (local, demo, aat, prod, preview)
- `CCD_DEF_*`: Variables substituted in JSON files during processing

### **Advanced Usage Examples**

#### **Custom Build Pipeline**
```bash
# 1. Convert specific jurisdiction to Excel
./bin/json2xlsx -D "jurisdictions/scotland/json" -o "custom-scotland.xlsx"

# 2. Edit Excel file manually
# ... make changes in Excel ...

# 3. Convert back to JSON
./bin/xlsx2json -i "custom-scotland.xlsx" -D "jurisdictions/scotland/json"

# 4. Review changes
git diff jurisdictions/scotland/json/
```

#### **Batch Processing**
```bash
# Process all jurisdictions for different environments
for env in local aat prod; do
  for jurisdiction in admin england-wales scotland; do
    ET_ENV=$env ./bin/json2xlsx \
      -D "jurisdictions/$jurisdiction/json" \
      -o "dist/$env/et-$jurisdiction-ccd-config-$env.xlsx"
  done
done
```

### **Maintenance Guidelines**

#### **Adding New Features to Wrappers**

1. **Core Processing Logic**: Located in `tools/ccd-definition-processor/src/main/`
   - `json2xlsx.js`: Core JSON to Excel conversion logic
   - `xlsx2json.js`: Core Excel to JSON conversion logic

2. **Wrapper Updates**: Modify the CLI wrappers in `bin/` to expose new functionality
3. **Build System Updates**: Update `tools/build-package.js` and `tools/build-workspace.js` if needed
4. **Documentation**: Update this section when adding new arguments or functionality

#### **Dependency Management**

- **Direct Dependencies**: Wrappers use `minimist` for argument parsing
- **Core Dependencies**: Processing logic dependencies managed in `tools/ccd-definition-processor/package.json`
- **Workspace Dependencies**: Shared dependencies managed in root `package.json`

#### **Error Handling**

Both wrappers include comprehensive error handling:
- Exit code 0 for success
- Exit code 1 for errors with descriptive error messages
- Validation of required arguments before processing

### **Future Maintenance**

When modifying the build system:

1. **Preserve CLI Interface**: Maintain backward compatibility for existing scripts
2. **Update Documentation**: Keep this section current with any changes
3. **Test All Environments**: Ensure wrappers work correctly across all target environments
4. **Validate Output**: Verify generated Excel files maintain proper CCD structure

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

### **Validation**

```bash
# Validate builds
node validate-builds.js

# Check code formatting
yarn format:check

# Lint code
yarn lint
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
./bin/preview/add-org-roles-to-users-new.sh
```

See the `scripts/` directory for detailed setup instructions and deployment scripts.

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

### **Project Cleanup**

This project has been cleaned up to remove obsolete and unnecessary files. The following types of files were removed:
- System files (`.DS_Store`, `Thumbs.db`)
- Temporary test artifacts in `tools/ccd-definition-processor/temp/`
- Backup files (`*.bak`, `*.backup`, `*.old`)
- Other temporary files (`*.tmp`, `*.temp`, `*~`)

### **Cleanup Commands**

```bash
# Clean build artifacts
yarn clean

# Clean dependencies
yarn clean:deps

# Reset workspace
yarn reset

# Remove system and temporary files (if they reappear)
find . -name ".DS_Store" -delete
find . -name "*.tmp" -delete
find . -name "*.temp" -delete
```

### **File Organization Best Practices**

- Keep the workspace clean by avoiding temporary files in the repository
- Use the `temp/` directories (which are gitignored) for temporary build artifacts
- System files like `.DS_Store` and `Thumbs.db` are automatically ignored
- Regular cleanup helps maintain build performance and repository cleanliness

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
5. **System files reappearing**: macOS and Windows may recreate `.DS_Store` and `Thumbs.db` files - these are automatically ignored by `.gitignore`

### **Debug Mode**

```bash
# Verbose build output
DEBUG=* yarn generate-excel:local

# Individual package debugging
node tools/build-package.js admin --env=local
```

## 📚 **Available Scripts**

- **Environment Scripts**: `scripts/environment/` - Environment configuration and setup
- **Deployment Scripts**: `scripts/deployment/` - Deployment automation and utilities
- **Build Tools**: `tools/` - Core build and processing utilities

## 🤝 **Contributing**

1. Validate builds: `node validate-builds.js`
2. Follow code style: `yarn lint:fix && yarn format`
3. Update documentation for significant changes
4. Test builds for all target environments

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏛️ **HMCTS Integration**

This repository is part of the HMCTS (HM Courts & Tribunals Service) digital transformation initiative, providing modern case management capabilities for Employment Tribunals across England, Wales, and Scotland.

---

**Need Help?** Check the troubleshooting section above or raise an issue in the repository.
