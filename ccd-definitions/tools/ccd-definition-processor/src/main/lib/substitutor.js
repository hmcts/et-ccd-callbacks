const ENVIRONMENT_VARIABLE_PREFIXES = ['CCD_DEF', 'ET_COS', 'ET_ENV'];

class Substitutor {
  static injectEnvironmentVariables (value) {
    // Load environment variables from env.json based on ET_ENV
    const etEnv = process.env.ET_ENV || 'local';
    try {
      const envConfig = require('../../../../../scripts/environment/env.json');
      if (envConfig[etEnv]) {
        // Set ET environment variables from the config
        Object.keys(envConfig[etEnv]).forEach(key => {
          if (!process.env[key]) {
            process.env[key] = envConfig[etEnv][key];
          }
        });
      }
    } catch (error) {
      console.warn('Could not load environment config from env.json:', error.message);
    }

    // Process all environment variables that match our prefixes
    Object.keys(process.env)
      .filter(environmentVariableName =>
        ENVIRONMENT_VARIABLE_PREFIXES.some(prefix =>
          environmentVariableName.startsWith(prefix)
        )
      )
      .forEach(environmentVariableName => {
        const environmentVariableValue = process.env[environmentVariableName];
        if (environmentVariableValue) {
          value = value.replace(new RegExp('\\$\\{' + environmentVariableName + '\\}', 'g'), environmentVariableValue);
        }
      });
    return value;
  }
}

module.exports = { Substitutor };
