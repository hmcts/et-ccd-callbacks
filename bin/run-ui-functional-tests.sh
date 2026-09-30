#!/usr/bin/env bash
# Runs the Playwright UI functional suite. The Gradle `functional` task starts this
# in the background on Jenkins (ET_UI_FUNCTIONAL_IN_PARALLEL=true) so the UI suite
# overlaps the API functional tests instead of running after them.
#
# Mirrors the Node setup the Jenkins library's YarnBuilder uses, so the suite runs
# on the same Node version it did when the Jenkinsfile hook invoked it directly.

set -uo pipefail

if [[ -f .nvmrc && -s /opt/nvm/nvm.sh ]]; then
  export NVM_DIR='/home/jenkinsssh/.nvm'
  # shellcheck disable=SC1091
  . /opt/nvm/nvm.sh || true
  nvm install
fi
export PATH="${HOME}/.local/bin:${PATH}"

if [[ ! -f node_modules/.yarn-state.yml && ! -f .pnp.cjs ]]; then
  echo "Installing yarn dependencies for the UI functional tests"
  corepack enable || true
  yarn install --immutable || exit $?
fi

exec yarn "${UI_FUNCTIONAL_YARN_TASK:-test:functional-chromium}"
