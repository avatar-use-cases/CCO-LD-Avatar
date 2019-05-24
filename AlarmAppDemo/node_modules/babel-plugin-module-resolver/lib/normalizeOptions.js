"use strict";

exports.__esModule = true;
exports.default = void 0;

require("core-js/modules/es7.array.includes");

require("core-js/modules/es6.regexp.replace");

require("core-js/modules/es6.regexp.split");

require("core-js/modules/es6.regexp.constructor");

var _fs = _interopRequireDefault(require("fs"));

var _path = _interopRequireDefault(require("path"));

var _reselect = require("reselect");

var _findBabelConfig = _interopRequireDefault(require("find-babel-config"));

var _glob = _interopRequireDefault(require("glob"));

var _pkgUp = _interopRequireDefault(require("pkg-up"));

var _utils = require("./utils");

var _resolvePath = _interopRequireDefault(require("./resolvePath"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var defaultExtensions = ['.js', '.jsx', '.es', '.es6', '.mjs'];
var defaultTransformedFunctions = ['require', 'require.resolve', 'System.import', // Jest methods
'jest.genMockFromModule', 'jest.mock', 'jest.unmock', 'jest.doMock', 'jest.dontMock', 'jest.setMock', 'require.requireActual', 'require.requireMock'];

function isRegExp(string) {
  return string.startsWith('^') || string.endsWith('$');
}

var specialCwd = {
  babelrc: function babelrc(startPath) {
    return _findBabelConfig.default.sync(startPath).file;
  },
  packagejson: function packagejson(startPath) {
    return _pkgUp.default.sync(startPath);
  }
};

function normalizeCwd(optsCwd, currentFile) {
  var cwd;

  if (optsCwd in specialCwd) {
    var startPath = currentFile === 'unknown' ? './' : currentFile;
    var computedCwd = specialCwd[optsCwd](startPath);
    cwd = computedCwd ? _path.default.dirname(computedCwd) : null;
  } else {
    cwd = optsCwd;
  }

  return cwd || process.cwd();
}

function normalizeRoot(optsRoot, cwd) {
  if (!optsRoot) {
    return [];
  }

  var rootArray = Array.isArray(optsRoot) ? optsRoot : [optsRoot];
  return rootArray.map(function (dirPath) {
    return _path.default.resolve(cwd, dirPath);
  }).reduce(function (resolvedDirs, absDirPath) {
    if (_glob.default.hasMagic(absDirPath)) {
      var roots = _glob.default.sync(absDirPath).filter(function (resolvedPath) {
        return _fs.default.lstatSync(resolvedPath).isDirectory();
      });

      return [].concat(resolvedDirs, roots);
    }

    return [].concat(resolvedDirs, [absDirPath]);
  }, []);
}

function getAliasTarget(key, isKeyRegExp) {
  var regExpPattern = isKeyRegExp ? key : `^${(0, _utils.escapeRegExp)(key)}(/.*|)$`;
  return new RegExp(regExpPattern);
}

function getAliasSubstitute(value, isKeyRegExp) {
  if (typeof value === 'function') {
    return value;
  }

  if (!isKeyRegExp) {
    return function (_ref) {
      var match = _ref[1];
      return `${value}${match}`;
    };
  }

  var parts = value.split('\\\\');
  return function (execResult) {
    return parts.map(function (part) {
      return part.replace(/\\\d+/g, function (number) {
        return execResult[number.slice(1)] || '';
      });
    }).join('\\');
  };
}

function normalizeAlias(optsAlias) {
  if (!optsAlias) {
    return [];
  }

  var aliasArray = Array.isArray(optsAlias) ? optsAlias : [optsAlias];
  return aliasArray.reduce(function (aliasPairs, alias) {
    var aliasKeys = Object.keys(alias);
    aliasKeys.forEach(function (key) {
      var isKeyRegExp = isRegExp(key);
      aliasPairs.push([getAliasTarget(key, isKeyRegExp), getAliasSubstitute(alias[key], isKeyRegExp)]);
    });
    return aliasPairs;
  }, []);
}

function normalizeTransformedFunctions(optsTransformFunctions) {
  if (!optsTransformFunctions) {
    return defaultTransformedFunctions;
  }

  return [].concat(defaultTransformedFunctions, optsTransformFunctions);
}

var _default = (0, _reselect.createSelector)( // The currentFile should have an extension; otherwise it's considered a special value
function (currentFile) {
  return currentFile.includes('.') ? _path.default.dirname(currentFile) : currentFile;
}, function (_, opts) {
  return opts;
}, function (currentFile, opts) {
  var cwd = normalizeCwd(opts.cwd, currentFile);
  var root = normalizeRoot(opts.root, cwd);
  var alias = normalizeAlias(opts.alias);
  var transformFunctions = normalizeTransformedFunctions(opts.transformFunctions);
  var extensions = opts.extensions || defaultExtensions;
  var stripExtensions = opts.stripExtensions || extensions;
  var resolvePath = opts.resolvePath || _resolvePath.default;
  return {
    cwd,
    root,
    alias,
    transformFunctions,
    extensions,
    stripExtensions,
    resolvePath
  };
});

exports.default = _default;