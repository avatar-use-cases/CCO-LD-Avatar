"use strict";

exports.__esModule = true;
exports.default = void 0;

require("core-js/modules/es6.set");

var _normalizeOptions = _interopRequireDefault(require("./normalizeOptions"));

var _resolvePath = _interopRequireDefault(require("./resolvePath"));

exports.resolvePath = _resolvePath.default;

var _call = _interopRequireDefault(require("./transformers/call"));

var _import = _interopRequireDefault(require("./transformers/import"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// Public API for external plugins
var importVisitors = {
  CallExpression: _call.default,
  'ImportDeclaration|ExportDeclaration': _import.default
};
var visitor = {
  Program: {
    enter(programPath, state) {
      programPath.traverse(importVisitors, state);
    },

    exit(programPath, state) {
      programPath.traverse(importVisitors, state);
    }

  }
};

var _default = function _default(_ref) {
  var types = _ref.types;
  return {
    name: 'module-resolver',

    manipulateOptions(opts) {
      if (opts.filename === undefined) {
        opts.filename = 'unknown';
      }
    },

    pre(file) {
      this.types = types;
      var currentFile = file.opts.filename;
      this.normalizedOpts = (0, _normalizeOptions.default)(currentFile, this.opts); // We need to keep track of all handled nodes so we do not try to transform them twice,
      // because we run before (enter) and after (exit) all nodes are handled

      this.moduleResolverVisited = new Set();
    },

    visitor,

    post() {
      this.moduleResolverVisited.clear();
    }

  };
};

exports.default = _default;