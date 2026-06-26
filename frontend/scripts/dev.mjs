import http from 'node:http';
import { readFile, stat, writeFile, mkdir } from 'node:fs/promises';
import { watch } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import esbuild from 'esbuild';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const dist = path.join(root, 'dist');
const port = Number(process.env.PORT || 3000);

async function build() {
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
    jsx: 'automatic'
  });

  await mkdir(dist, { recursive: true });
  await writeFile(path.join(dist, 'index.html'), await readFile(path.join(root, 'index.html')));
}

function contentType(filePath) {
  if (filePath.endsWith('.html')) return 'text/html; charset=utf-8';
  if (filePath.endsWith('.js')) return 'text/javascript; charset=utf-8';
  if (filePath.endsWith('.css')) return 'text/css; charset=utf-8';
  if (filePath.endsWith('.json')) return 'application/json; charset=utf-8';
  return 'application/octet-stream';
}

async function serveFile(res, filePath) {
  const body = await readFile(filePath);
  res.writeHead(200, { 'Content-Type': contentType(filePath) });
  res.end(body);
}

await build();

const server = http.createServer(async (req, res) => {
  const requestPath = req.url === '/' ? '/index.html' : req.url || '/index.html';
  const filePath = path.join(dist, requestPath);

  try {
    const stats = await stat(filePath);
    if (stats.isFile()) {
      await serveFile(res, filePath);
      return;
    }
  } catch {
    // fall through to index.html
  }

  await serveFile(res, path.join(dist, 'index.html'));
});

server.listen(port, () => {
  console.log(`Frontend dev server running at http://localhost:${port}`);
});

const watcher = new AbortController();
process.on('SIGINT', () => watcher.abort());

watch(path.join(root, 'src'), { recursive: true, signal: watcher.signal }, async () => {
  try {
    await build();
    console.log('Rebuilt frontend');
  } catch (error) {
    console.error(error);
  }
});

watch(path.join(root, 'index.html'), { signal: watcher.signal }, async () => {
  try {
    await build();
    console.log('Rebuilt frontend');
  } catch (error) {
    console.error(error);
  }
});
