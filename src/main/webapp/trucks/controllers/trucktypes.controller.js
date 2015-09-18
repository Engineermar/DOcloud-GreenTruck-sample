
angular.module('trucks').controller('TruckTypesController', ['$scope', '$routeParams', '$location', '$http',
    function($scope, $routeParams, $location, $http) {


        $scope.trucktypes = [];

        $http.get("rest/v1/trucking/truckTypes")
            .success(function(data, status, headers, config) {
                $scope.trucktypes = data;
            }). error(function(data, status, headers, config) {
                console.log(status);
            });

    }
]);