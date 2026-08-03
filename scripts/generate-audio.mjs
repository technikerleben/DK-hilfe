import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';
import { spawnSync } from 'node:child_process';
import { pathToFileURL } from 'node:url';

const projectRoot = path.resolve(import.meta.dirname, '..');
const htmlPath = path.join(projectRoot, 'app/src/main/assets/index.html');
const outputDir = path.join(projectRoot, 'app/src/main/assets/audio');
const espeakModulePath = process.argv[2];

if (!espeakModulePath) {
  throw new Error('Pfad zu espeak-ng.js als erstes Argument angeben.');
}

const html = fs.readFileSync(htmlPath, 'utf8');
const match = html.match(/const phrases=\[[\s\S]*?\n\];/);
if (!match) throw new Error('Phrasenliste wurde nicht gefunden.');

const sandbox = {};
vm.runInNewContext(`${match[0]}\nthis.phrases = phrases;`, sandbox);
const phrases = sandbox.phrases;
const { default: ESpeakNg } = await import(pathToFileURL(espeakModulePath));

fs.mkdirSync(outputDir, { recursive: true });

for (let index = 0; index < phrases.length; index += 1) {
  const text = phrases[index][1];
  const id = String(index).padStart(2, '0');
  const wavPath = path.join(outputDir, `phrase_${id}.wav`);
  const mp3Path = path.join(outputDir, `phrase_${id}.mp3`);
  const virtualWav = `/phrase_${id}.wav`;

  const espeak = await ESpeakNg({
    arguments: ['-v', 'da', '-s', '145', '-p', '48', '-a', '165', '-w', virtualWav, text],
  });
  fs.writeFileSync(wavPath, espeak.FS.readFile(virtualWav));

  const encoded = spawnSync('ffmpeg', [
    '-v', 'error', '-y', '-i', wavPath,
    '-ac', '1', '-ar', '22050', '-b:a', '48k',
    '-map_metadata', '-1', mp3Path,
  ], { encoding: 'utf8' });
  if (encoded.status !== 0) throw new Error(encoded.stderr);
  fs.rmSync(wavPath);
  console.log(`${id}: ${text}`);
}
