"use strict";

exports.__esModule = true;
exports.default = resolvePath;

var _path = _interopRequireDefault(require("path"));

var _log = require("./log");

var _mapToRelative = _interopRequireDefault(require("./mapToRelative"));

var _normalizeOptions = _interopRequireDefault(require("./normalizeOptions"));

var _utils = require("./utils");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function getRelativePath(sourcePath, currentFile, absFileInRoot, opts) {
  var realSourceFileExtension = _path.default.extname(absFileInRoot);

  var sourceFileExtension = _path.default.extname(sourcePath);

  var relativePath = (0, _mapToRelative.default)(opts.cwd, currentFile, absFileInRoot);

  if (realSourceFileExtension !== sourceFileExtension) {
    relativePath = (0, _utils.replaceExtension)(relativePath, opts);
  }

  return (0, _utils.toLocalPath)((0, _utils.toPosixPath)(relativePath));
}

function findPathInRoots(sourcePath, _ref) {
  var extensions = _ref.extensions,
      root = _ref.root;
  // Search the source path inside every custom root directory
  var resolvedSourceFile;
  root.some(function (basedir) {
    resolvedSourceFile = (0, _utils.nodeResolvePath)(`./${sourcePath}`, basedir, extensions);
    return resolvedSourceFile !== null;
  });
  return resolvedSourceFile;
}

function resolvePathFromRootConfig(sourcePath, currentFile, opts) {
  var absFileInRoot = findPathInRoots(sourcePath, opts);

  if (!absFileInRoot) {
    return null;
  }

  return getRelativePath(sourcePath, currentFile, absFileInRoot, opts);
}

function checkIfPackageExists(modulePath, currentFile, extensions) {
  var resolvedPath = (0, _utils.nodeResolvePath)(modulePath, currentFile, extensions);

  if (resolvedPath === null) {
    (0, _log.warn)(`Could not resolve "${modulePath}" in file ${currentFile}.`);
  }
}

function resolvePathFromAliasConfig(sourcePath, currentFile, opts) {
  var aliasedSourceFile;
  opts.alias.find(function (_ref2) {
    var regExp = _ref2[0],
        substitute = _ref2[1];
    var execResult = regExp.exec(sourcePath);

    if (execResult === null) {
      return false;
    }

    aliasedSourceFile = substitute(execResult);
    return true;
  });

  if (!aliasedSourceFile) {
    return null;
  }

  if ((0, _utils.isRelativePath)(aliasedSourceFile)) {
    return (0, _utils.toLocalPath)((0, _utils.toPosixPath)((0, _mapToRelative.default)(opts.cwd, currentFile, aliasedSourceFile)));
  }

  if (process.env.NODE_ENV !== 'production') {
    checkIfPackageExists(aliasedSourceFile, currentFile, opts.extensions);
  }

  return aliasedSourceFile;
}

var resolvers = [resolvePathFromAliasConfig, resolvePathFromRootConfig];

function resolvePath(sourcePath, currentFile, opts) {
  if ((0, _utils.isRelativePath)(sourcePath)) {
    return sourcePath;
  }

  var normalizedOpts = (0, _normalizeOptions.default)(currentFile, opts); // File param is a relative path from the environment current working directory
  // (not from cwd param)

  var absoluteCurrentFile = _path.default.resolve(currentFile);

  var resolvedPath = null;
  resolvers.some(function (resolver) {
    resolvedPath = resolver(sourcePath, absoluteCurrentFile, normalizedOpts);
    return resolvedPath !== null;
  });
  return resolvedPath;
}