//exports.start = function(arg0, success, error) {
exports.start = function(success, error) {
//    exec(success, error, "nearby", "start", [arg0]);
    cordova.exec(success, error, "nearby", "start");
};
