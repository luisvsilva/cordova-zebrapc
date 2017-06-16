
var exec = require("cordova/exec");
	
var ZebraPC = function() {};
var classname = "ZebraPc";

//options: template:"jonas.pnl", data:{"Label":"HeyDude"}

ZebraPC.prototype.print = function(success,error,options)
{
	if (options == null || options.template==null){
		throw "template needs to be specified in options";
	}

	var templateName = options.template;
	var data = options.data;
	if (data == null){
		data = {};
	}

	return exec(success,error, classname, "print", [templateName, data]);
};

ZebraPC.prototype.getStatus = function(success,error)
{
	return exec(success, error, classname, "status");
};

var zebraPc = new ZebraPC();
module.exports = zebraPc;
