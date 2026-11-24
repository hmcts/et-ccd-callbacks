import { existsSync, readFileSync } from 'node:fs';
import path from 'node:path';

function normalizeResourcePath(resourcePath: string): string {
  const trimmed = resourcePath.replace(/^[/\\]+/, '');
  return trimmed.replace(/^resources[/\\]+/, '');
}

function resolvePackageResourcesRoot(): string {
  // Optional explicit override for consumers/CI
  const envRoot = process.env.ET_SHARED_TEST_LIBRARY_RESOURCES_ROOT;
  if (envRoot && existsSync(envRoot)) return envRoot;

  // 1) Consumer repo override paths
  const cwdCandidates = [
    path.resolve(process.cwd(), 'et-shared-test-library', 'resources'),
    path.resolve(process.cwd(), 'node_modules', '@hmcts', 'et-shared-test-library', 'resources'),
    path.resolve(process.cwd(), 'resources'),
  ];
  for (const candidate of cwdCandidates) {
    if (existsSync(candidate)) return candidate;
  }

  // deterministic fallback
  return cwdCandidates[2];
}

const packageResourcesRoot = resolvePackageResourcesRoot();

export function resolveResourcePath(resourcePath: string): string {
  if (path.isAbsolute(resourcePath)) return resourcePath;

  const normalized = normalizeResourcePath(resourcePath);

  const candidates = [
    path.resolve(process.cwd(), normalized), // explicit caller override
    path.resolve(packageResourcesRoot, normalized), // shared library defaults
    path.resolve(process.cwd(), 'resources', normalized), // consumer repo resources
  ];

  for (const candidate of candidates) {
    if (existsSync(candidate)) return candidate;
  }

  return candidates[2];
}

export function resolveResourceJson(resourcePath: string) {
  const resolvedPath = resolveResourcePath(resourcePath);
  return JSON.parse(readFileSync(resolvedPath, 'utf8'));
}
