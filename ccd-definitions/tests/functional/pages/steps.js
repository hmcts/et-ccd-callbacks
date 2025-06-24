const requireDirectory = require('require-directory');

const steps = requireDirectory(module);

const actions = {};

function setActorActions(data) {
  for (const k in data) {
    if (data.hasOwnProperty(k)) {
      actions[k] = data[k];
    }
  }
}

function setActorActionsRecursively(object) {
  const objKeys = Object.keys(object);

  for (const key in objKeys) {
    if (Object.keys(object[objKeys[key]]).length === 0) {
      setActorActions(object);
    } else {
      setActorActionsRecursively(object[objKeys[key]]);
    }
  }
}

module.exports = function () {
  setActorActionsRecursively(steps);
  return actor(actions);
};
