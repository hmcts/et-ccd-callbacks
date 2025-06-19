const path = require('path');

module.exports = {
  // Base paths for the workspace
  paths: {
    root: path.resolve(__dirname, '..'),
    packages: path.resolve(__dirname, '../packages'),
    tools: path.resolve(__dirname, '../tools'),
    processor: path.resolve(__dirname, '../tools/ccd-definition-processor'),
    output: path.resolve(__dirname, '../dist')
  },

  // Environment-specific configurations
  environments: {
    local: {
      suffix: 'local',
      excludePatterns: ['*-prod.json']
    },
    demo: {
      suffix: 'demo', 
      excludePatterns: ['*-prod.json']
    },
    aat: {
      suffix: 'aat',
      excludePatterns: ['*-prod.json']
    },
    prod: {
      suffix: 'prod',
      excludePatterns: ['*-nonprod.json']
    },
    preview: {
      suffix: 'preview',
      excludePatterns: []
    }
  },

  // Package configurations
  packages: {
    admin: {
      name: 'admin',
      outputPrefix: 'et-admin-ccd-config'
    },
    'england-wales': {
      name: 'england-wales',
      outputPrefix: 'et-englandwales-ccd-config'
    },
    scotland: {
      name: 'scotland',
      outputPrefix: 'et-scotland-ccd-config'
    }
  },

  // Shared dependencies
  sharedDependencies: {
    '@hmcts/nodejs-healthcheck': '^1.8.0',
    '@hmcts/properties-volume': '^1.0.0',
    'config': '^3.3.7',
    'express': '^4.18.2',
    'glob': '^10.2.7',
    'lodash': '^4.17.21',
    'minimist': '^1.2.8',
    'moment': '^2.29.2',
    'prettier': '^2.7.1',
    'xlsx': '^0.18.5',
    'xlsx-populate': '^1.21.0'
  },

  // Shared dev dependencies
  sharedDevDependencies: {
    'eslint': '^8.9.0',
    'eslint-plugin-mocha': '^10.0.3',
    'mocha': '^10.0.0',
    'chai': '^4.2.0'
  }
};

