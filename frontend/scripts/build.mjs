import { rm, mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import esbuild from 'esbuild';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const dist = path.join(root, 'dist');

await rm(dist, { recursive: true, force: true });
await mkdir(dist, { recursive: true });

await esbuild.build({
  entryPoints: [path.join(root, 'src', 'main.jsx')],
  bundle: true,
  format: 'esm',
  platform: 'browser',
  target: ['es2022'],
  outdir: dist,
  entryNames: 'assets/app',
  assetNames: 'assets/[name]',
  sourcemap: true,
  jsx: 'automatic',
  minify: false
});

await writeFile(path.join(dist, 'index.html'), await readFile(path.join(root, 'index.html')));
