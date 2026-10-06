import path from 'node:path';

export function resolveResourcePath(resourcePath: string): string {
  if (path.isAbsolute(resourcePath)) return resourcePath;

  const normalized = resourcePath.replace(/^[/\\]+/, '');

  // Resolve from process cwd (repo root in your runs)
  // and support both shared-lib and consumer paths.
  const candidates = [
    path.resolve(process.cwd(), normalized),
    path.resolve(process.cwd(), 'et-shared-test-library', normalized),
  ];

  for (const p of candidates) {
    // lazy require to avoid ESM/CJS fs import friction
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const fs = require('node:fs');
    if (fs.existsSync(p)) return p;
  }

  // fallback first candidate
  return candidates[0];
}

