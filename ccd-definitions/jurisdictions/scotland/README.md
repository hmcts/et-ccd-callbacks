# Scotland CCD Definitions

## Overview

Employment Tribunal CCD configuration definitions for Scotland jurisdiction. This package is part of the unified ET CCD Definitions workspace.

## Quick Start

```bash
# From workspace root
yarn generate-excel:local    # Build all packages
node tools/build-package.js scotland --env=local  # Build this package only

# From this package directory
yarn build --env local
```

## Workspace Integration

This package is part of the unified workspace structure. See the [main README](../../README.md) for complete setup instructions.

### Prerequisites

- **Node.js** >= 18.0.0
- **Yarn** >= 3.0.0
- **WSL 2** (Windows users)

### Installation

From the workspace root:
```bash
yarn install
```

## Build Commands

### Environment-Specific Builds

```bash
# Build for specific environments
yarn build:local    # Local development
yarn build:demo     # Demo environment
yarn build:aat      # AAT environment  
yarn build:prod     # Production
yarn build:preview  # Preview environment
```

### Output Files

Generated Excel files are placed in:
- `xlsx/` - Environment-specific Excel files
- `../../dist/<env>/` - Workspace-level consolidated output

### Converting Excel to JSON

If you need to make changes via Excel and convert back to JSON:

```bash
# 1. Generate base Excel file
yarn build:local

# 2. Edit the Excel file (ensure no environment-specific URLs)

# 3. Convert back to JSON
yarn generate-json

# 4. Review changes
git diff
```

## Package Structure

```
scotland/
├── json/                 # JSON definition files
│   ├── AuthorisationCaseEvent/
│   ├── AuthorisationCaseField/
│   ├── CaseEvent/
│   ├── CaseField/
│   └── ...
├── xlsx/                 # Generated Excel files
├── data/                 # Template and data files
└── package.json         # Package configuration
```

**Note:** Test files are now centralized in `../../tests/scotland/`

## Testing

```bash
# Run tests for this package
yarn test

# From workspace root
yarn test:scotland
```

## Variable Substitution

The build process replaces variables in JSON files with environment values. Variables must start with `CCD_DEF` prefix:

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

## Development

### Linting

```bash
yarn lint        # Check for issues
yarn lint:fix    # Fix issues automatically
```

### Cleaning

```bash
yarn clean       # Remove generated files
```

## Integration

This package integrates with:
- **Admin package** - Shared administrative configurations
- **England & Wales package** - Similar structure for England & Wales jurisdiction
- **Workspace tools** - Centralized build and development tools
- **Preview environment** - Automated testing and deployment

For more information, see the [workspace documentation](../../README.md).
