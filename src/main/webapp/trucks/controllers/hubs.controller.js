
angular.module('trucks').controller('HubsController', ['$scope', '$routeParams', '$location', '$http',
    function($scope, $routeParams, $location, $http) {


    $scope.hubs = [];

    $http.get("rest/v1/trucking/hubs")
        .success(function(data, status, headers, config) {
            $scope.hubs = data;
        }). error(function(data, status, headers, config) {
            console.log(status);
        });
   
}]).filter('formatRoutes',function(){
	return function (hub) {
		var data = hub.routes;
	    var s = "";
	    for (var i = 0; i < data.length; i++) {
	      if (i > 0) s += ",";
	      s += data[i].spoke + "(" + data[i].distance + ")";
	    }
	    return s;
	}
}).filter('formatLoadtimes',function(){
	return function (hub) {
		var data = hub.loadtimes;
        var s = "";
        for (var i = 0; i < data.length; i++) {
          if (i > 0) s += ",";
          s += data[i].truckType + "(" + data[i].loadTime + ")";
        }
        return s;
	}
})