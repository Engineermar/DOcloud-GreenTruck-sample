angular.module('trucks').controller('AdminController', ['$scope', '$routeParams', '$location', '$http',
    function($scope, $routeParams, $location, $http) {
        $scope.initializeDemo = function() {
            $http.post('/rest/v1/trucking/initialize', {});
        }
        
        $scope.deleteAllJobs = function() {
            $http.post('/rest/v1/trucking/deleteAllJobs', {});
        }
    }
]);