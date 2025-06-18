# ET CCD Definitions - Merged Repository

This repository contains consolidated configuration definitions for ET (Employment Tribunals) CCD (Case and Content Data) across all jurisdictions.

## Repository Structure

This merged repository combines three previously separate repositories:
- `et-ccd-definitions-admin` - Administrative configurations
- `et-ccd-definitions-englandwales` - England & Wales specific configurations  
- `et-ccd-definitions-scotland` - Scotland specific configurations

### Directory Structure

```
├── jurisdictions/
│   ├── admin/           # Administrative configurations
│   │   ├── json/        # JSON configuration files
│   │   └── xlsx/        # Excel configuration files
│   ├── england-wales/   # England & Wales configurations
│   │   ├── data/        # Data files
│   │   ├── definitions/ # Definition files
│   │   ├── json/        # JSON configuration files
│   │   ├── src/         # Source code
│   │   ├── xlsx/        # Excel configuration files
│   │   ├── package.json # Jurisdiction-specific package config
│   │   ├── README.md    # Jurisdiction-specific documentation
│   │   └── env.json     # Environment configuration
│   └── scotland/        # Scotland configurations
│       ├── data/        # Data files
│       ├── definitions/ # Definition files
│       ├── json/        # JSON configuration files
│       ├── src/         # Source code
│       ├── xlsx/        # Excel configuration files
│       ├── package.json # Jurisdiction-specific package config
│       ├── README.md    # Jurisdiction-specific documentation
│       └── env.json     # Environment configuration
├── ccd-definition-processor/ # Definition processing tools
├── bin/                 # Build and deployment scripts
├── config/              # Global configuration
├── src/                 # Common source code
└── ...
```

## Overview

This repository generates CCD definition files to enable ET functionality across all jurisdictions. It includes:

- **Admin functionality** for Employment Case Management (ECM)
- **England & Wales** specific case configurations
- **Scotland** specific case configurations
- Configuration to create full-stack CCD environments for testing

Please see [Preview Environment](docs/preview-environment.md) for more details.

## ccd-definition-processor

This repo makes use of [CCD Definition Processor](https://github.com/hmcts/ccd-definition-processor) as a sub-module to generate the CCD definitions Excel files.

## Prerequisites

* yarn
* Node.js >= 14.19.1

## Setup (Windows users)

Windows users must ensure they use Windows Subsystem for Linux (WSL 2) for local development:
- Install WSL 2: https://docs.microsoft.com/en-us/windows/wsl/install
- Install nvm, node.js, and npm: https://docs.microsoft.com/en-us/windows/dev-environment/javascript/nodejs-on-wsl
- Install nvm: https://github.com/nvm-sh/nvm
- Install required node version: `nvm install`

### Install

Run `yarn setup` and `yarn install` to install the dependencies for this project.

## Features

### Variable substitution

A `json2xlsx` processor replaces variable placeholders defined in JSON definition files with values read from
environment variables as long as variable name starts with `CCD_DEF` prefix.

For example `CCD_DEF_BASE_URL=http://localhost` environment variable gets injected into a fragment of the following CCD
definition:

```json
[
  {
    "LiveFrom": "2017-01-01",
    "CaseTypeID": "DRAFT",
    "ID": "initiateCase",
    "CallBackURLSubmittedEvent": "${CCD_DEF_BASE_URL}/callback"
  }
]
```

to become:

```json
[
  {
    "LiveFrom": "2017-01-01",
    "CaseTypeID": "DRAFT",
    "ID": "initiateCase",
    "CallBackURLSubmittedEvent": "http://localhost/callback"
  }
]
```

## Usage

The following commands are available:

### Convert JSON to Excel

**For all environments:**

`yarn generate-excel-all` - Generate excel configs for all environments (Local, Demo, AAT, Prod, Perftest)

The generated excel files will be in `definitions/xlsx/` or `jurisdictions/[jurisdiction]/xlsx/`.

**For a specific environment:**

`yarn generate-excel-(local|demo|aat|prod)`

For example: `yarn generate-excel-aat`

### Convert Excel to JSON

If you prefer to make changes directly on the excel file, and then convert back to JSON:

1. Generate a fresh base Excel file using `yarn generate-excel`
2. The generated excel file will be in `definitions/xlsx/et-admin-ccd-config-base.xlsx` and will contain placeholder URLs
3. Make changes to the Excel file but **ensure you don't have any environment-specific URLs** (use placeholders instead)
4. Convert back to JSON using `yarn generate-json`
5. Review the JSON file changes to ensure all your changes are correct

## Working with Jurisdictions

Each jurisdiction has its own specific configurations and can be worked on independently:

### England & Wales
See `jurisdictions/england-wales/README.md` for jurisdiction-specific documentation.

### Scotland  
See `jurisdictions/scotland/README.md` for jurisdiction-specific documentation.

### Admin
Administrative configurations are stored in `jurisdictions/admin/`.

## Environment Configuration

### Environment variables

The following environment variables are required:

| Name | Default | Description |
|------|---------|-------------|
| CCD_DEF_CASE_SERVICE_BASE_URL | - | Base URL for CCD Data Store API |
| CCD_DEF_CCD_ADMIN_WEB_INGRESS_URL | - | Admin web URL |
| IDAM_ADMIN_WEB_SERVICE_KEY | - | Admin web Idam service-to-service secret key |
| IDAM_ADMIN_SECRET | - | Admin web Idam secret |
| S2S_SECRET | - | Shared secret for S2S authentication |

## Building and Deploying

### Building

```bash
yarn install
yarn run build
```

### Deploying to AAT

```bash
yarn run deploy:aat
```

### Deploying to production

```bash
yarn run deploy:prod
```

### Azure Key Vault

Azure Key Vault is used to store secret configuration, e.g., the IDAM client secret.

## Development Setup

### Configure environment variables

Copy `.env.example` to `.env` and populate the required configuration.

### Preview the configuration

```bash
yarn run preview:local
```

## Migration Notes

This repository was created by merging three separate repositories:
1. **et-ccd-definitions-admin** (base repository)
2. **et-ccd-definitions-englandwales** 
3. **et-ccd-definitions-scotland**

All git history from the original repositories is preserved.

## Licensing

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
