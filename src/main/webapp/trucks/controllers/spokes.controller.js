
angular.module('trucks').controller('SpokesController', ['$scope', '$routeParams', '$location', '$http',
    function($scope, $routeParams, $location, $http) {

    $scope.spokes = [];

    $http.get("rest/v1/trucking/spokes")
        .success(function(data, status, headers, config) {
            $scope.spokes = data;
        }). error(function(data, status, headers, config) {
            console.log(status);
        });

    
    }
]);