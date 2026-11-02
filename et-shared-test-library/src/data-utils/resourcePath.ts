import { existsSync } from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';

const require = createRequire(process.cwd() + '/');

function resolvePackageResourcesRoot(): string {
  try {
    const marker = require.resolve('@et-shared-test-library/core/resources/payload/user-details.json');
    return path.resolve(marker, '..', '..'); // .../resources
  } catch {
    // local workspace fallback
    return path.resolve(process.cwd(), 'et-shared-test-library', 'resources');
  }
}

const packageResourcesRoot = resolvePackageResourcesRoot();

function normalizeResourcePath(resourcePath: string): string {
  const trimmed = resourcePath.replace(/^[/\\]+/, '');
  // Prevent ".../resources/resources/..." when callers already include "resources/" prefix
  return trimmed.replace(/^resources[/\\]+/, '');
}

export function resolveResourcePath(resourcePath: string): string {
  if (path.isAbsolute(resourcePath)) return resourcePath;

  const normalized = normalizeResourcePath(resourcePath);

  const candidates = [
    path.resolve(process.cwd(), normalized),            // consumer override
    path.resolve(process.cwd(), 'resources', normalized), // consumer repo resources/
    path.resolve(packageResourcesRoot, normalized),     // package default
  ];

  for (const candidate of candidates) {
    if (existsSync(candidate)) return candidate;
  }

  // deterministic fallback to package path
  return candidates[2];
}
