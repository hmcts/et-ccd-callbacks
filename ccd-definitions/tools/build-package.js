#!/usr/bin/env node

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');
const buildConfig = require('../configs/build.config');

/**
 * Individual package build tool
 * Called by each package's build script
 */

const packageName = process.argv[2];
const args = process.argv.slice(3);
const environment = args.find(arg => arg.startsWith('--env'))?.split('=')[1] || 
                   args.find(arg => arg.startsWith('--env '))?.split(' ')[1] ||
                   process.env.ET_ENV ||
                   'local';

if (!packageName) {
  console.error('❌ Package name is required');
  console.error('Usage: node build-package.js <package-name> [--env=environment]');
  process.exit(1);
}

const { paths, environments, packages } = buildConfig;
const packageConfig = packages[packageName];

if (!packageConfig) {
  console.error(`❌ Unknown package: ${packageName}`);
  console.error(`Available packages: ${Object.keys(packages).join(', ')}`);
  process.exit(1);
}

if (!environments[environment]) {
  console.error(`❌ Invalid environment: ${environment}`);
  console.error(`Available environments: ${Object.keys(environments).join(', ')}`);
  process.exit(1);
}

console.log(`🔨 Building ${packageConfig.name} for ${environment}...`);

const packagePath = path.join(paths.root, packageConfig.name);
const processorPath = paths.processor;
const envConfig = environments[environment];

// Set environment variable for scripts
process.env.ET_ENV = environment;

// Build paths
const jsonPath = path.join(packagePath, 'json');
const xlsxPath = path.join(packagePath, 'xlsx');
const outputFile = `${packageConfig.outputPrefix}-${envConfig.suffix}.xlsx`;
const outputPath = path.join(xlsxPath, outputFile);

console.log(`📦 Package: ${packageConfig.name}`);
console.log(`🌍 Environment: ${environment}`);
console.log(`📂 JSON source: ${jsonPath}`);
console.log(`📄 Output: ${outputFile}`);

// Ensure directories exist
if (!fs.existsSync(xlsxPath)) {
  fs.mkdirSync(xlsxPath, { recursive: true });
}

// Check if json directory exists
if (!fs.existsSync(jsonPath)) {
  console.error(`❌ JSON directory not found: ${jsonPath}`);
  process.exit(1);
}

try {
  // Build the Excel file using the CCD definition processor
  const command = `node "${path.join(processorPath, 'bin', 'json2xlsx')}" -D "${jsonPath}" -o "${outputPath}"`;
  
  console.log(`🔧 Running: ${command}`);
  execSync(command, { 
    stdio: 'inherit',
    cwd: processorPath,  // Run from processor directory to find template
    env: { ...process.env, ET_ENV: environment }
  });
  
  // Verify the output file was created
  if (fs.existsSync(outputPath)) {
    const stats = fs.statSync(outputPath);
    const sizeKB = Math.round(stats.size / 1024);
    console.log(`✅ Successfully built ${outputFile} (${sizeKB}KB)`);
  } else {
    throw new Error(`Output file was not created: ${outputPath}`);
  }
  
} catch (error) {
  console.error(`❌ Build failed for ${packageConfig.name}:`, error.message);
  process.exit(1);
}

console.log(`🎉 Build completed for ${packageConfig.name}!`);

