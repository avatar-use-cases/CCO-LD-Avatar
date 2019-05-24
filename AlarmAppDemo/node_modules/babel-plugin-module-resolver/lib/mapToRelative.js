"use strict";

exports.__esModule = true;
exports.default = mapToRelative;

var _path = _interopRequireDefault(require("path"));

var _utils = require("./utils");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function mapToRelative(cwd, currentFile, module) {
  var from = _path.default.dirname(currentFile);

  var to = _path.default.normalize(module);

  from = _path.default.resolve(cwd, from);
  to = _path.default.resolve(cwd, to);

  var moduleMapped = _path.default.relative(from, to);

  return (0, _utils.toPosixPath)(moduleMapped);
}