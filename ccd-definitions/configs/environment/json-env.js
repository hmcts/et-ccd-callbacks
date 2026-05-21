const { spawn } = require('child_process');

const envs = require('./env.json');
const environment = process.argv[2];
const scriptToRun = process.argv[3];

if (!environment || !scriptToRun) {
  console.error('Please provide the environment and the script to run as arguments.');
  process.exit(1);
}

if (!envs.hasOwnProperty(environment)) {
  console.error(`Environment "${environment}" not found in the JSON file.`);
  process.exit(1);
}

const cmd = `ET_ENV=${environment} CCD_DEF_BASE_URL=${envs[environment]} yarn ${scriptToRun}`;
const subprocess = spawn(cmd, { shell: true, stdio: 'inherit' });

subprocess.on('exit', code => {
  process.exit(code);
});
