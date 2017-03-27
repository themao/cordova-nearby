var exec = require('cordova/exec');

//exports.start = function(arg0, success, error) {
exports.start = function(success, error) {
//    exec(success, error, "nearby", "start", [arg0]);
    exec(success, error, "nearby", "start");
};
