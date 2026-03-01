#!/usr/bin/env node

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');
const buildConfig = require('../configs/build.config');

/**
 * Unified workspace build tool for ET CCD Definitions
 * Replaces the scattered build scripts with a centralized approach
 */

const args = process.argv.slice(2);
const environment = args.find(arg => arg.startsWith('--env='))?.split('=')[1] || 
                   args.find(arg => arg.startsWith('--env '))?.split(' ')[1] ||
                   process.env.ET_ENV ||
                   'local';
const watch = args.includes('--watch');
const packageFilter = args.find(arg => arg.startsWith('--package='))?.split('=')[1];
const outputSuffix = args.find(arg => arg.startsWith('--output-suffix='))?.split('=')[1];

console.log(`🚀 Building ET CCD Definitions workspace...`);
console.log(`📦 Environment: ${environment}`);
if (packageFilter) {
  console.log(`🎯 Package filter: ${packageFilter}`);
}

const { paths, environments, packages } = buildConfig;

// Validate environment
if (!environments[environment]) {
  console.error(`❌ Invalid environment: ${environment}`);
  console.error(`Available: ${Object.keys(environments).join(', ')}`);
  process.exit(1);
}

// Determine which packages to build
const packagesToBuild = packageFilter 
  ? [packageFilter]
  : Object.keys(packages);

console.log(`📋 Building packages: ${packagesToBuild.map(pkg => packages[pkg].name).join(', ')}`);

// Create output directory
const outputDir = path.join(paths.root, 'dist', environment);
if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

// Build each package
for (const packageName of packagesToBuild) {
  const packageConfig = packages[packageName];
  if (!packageConfig) {
    console.error(`❌ Unknown package: ${packageName}`);
    continue;
  }

  console.log(`\n🔨 Building ${packageConfig.name}...`);
  
  try {
    const buildCommand = `node "${path.join(paths.tools, 'build-package.js')}" "${packageName}" --env=${environment}`;
    
    console.log(`   Running: ${buildCommand}`);
    execSync(buildCommand, { 
      stdio: 'inherit',
      cwd: paths.root
    });
    
    console.log(`   ✅ ${packageConfig.name} built successfully`);
  } catch (error) {
    console.error(`   ❌ Failed to build ${packageConfig.name}:`, error.message);
    process.exit(1);
  }
}

// Copy generated files to output directory
const finalOutputSuffix = outputSuffix || environment;
console.log(`\n📂 Copying files to dist/${finalOutputSuffix}/...`);

// Create the final output directory
const finalOutputDir = path.join(paths.root, 'dist', finalOutputSuffix);
if (!fs.existsSync(finalOutputDir)) {
  fs.mkdirSync(finalOutputDir, { recursive: true });
}

for (const packageName of packagesToBuild) {
  const packageConfig = packages[packageName];
  const packagePath = path.join(paths.root, packageConfig.name);
  const xlsxPath = path.join(packagePath, 'xlsx');
  
  if (fs.existsSync(xlsxPath)) {
    const files = fs.readdirSync(xlsxPath)
      .filter(file => file.endsWith('.xlsx') && file.includes(environment));
    
    for (const file of files) {
      const sourcePath = path.join(xlsxPath, file);
      
      // If we have a custom output suffix, rename the file
      let destFileName = file;
      if (outputSuffix && outputSuffix !== environment) {
        destFileName = file.replace(`-${environment}.xlsx`, `-${outputSuffix}.xlsx`);
      }
      
      const destPath = path.join(finalOutputDir, destFileName);
      
      try {
        fs.copyFileSync(sourcePath, destPath);
        console.log(`   ✅ Copied ${destFileName}`);
      } catch (error) {
        console.warn(`   ⚠️  Failed to copy ${destFileName}:`, error.message);
      }
    }
  }
}

if (watch) {
  console.log(`\n👀 Watching for changes... (Press Ctrl+C to stop)`);
  // Simple watch implementation - in production, you might want to use chokidar
  const watchPaths = packagesToBuild.map(pkg => 
    path.join(paths.root, packages[pkg].name, 'json')
  );
  
  // For now, just log that watch mode is enabled
  console.log(`   Watching: ${watchPaths.join(', ')}`);
  console.log(`   🔄 Run build manually when files change`);
}

console.log(`\n🎉 Workspace build completed!`);
console.log(`📁 Output directory: ${finalOutputDir}`);
console.log(`📋 Generated files:`);

// List generated files
try {
  const files = fs.readdirSync(finalOutputDir);
  files.forEach(file => {
    const stats = fs.statSync(path.join(finalOutputDir, file));
    const sizeKB = Math.round(stats.size / 1024);
    console.log(`   📄 ${file} (${sizeKB}KB)`);
  });
} catch (error) {
  console.warn(`⚠️  Could not list output files:`, error.message);
}

