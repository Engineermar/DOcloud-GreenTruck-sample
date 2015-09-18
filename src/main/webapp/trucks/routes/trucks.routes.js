
angular.module('trucks').config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/hubs', {
            templateUrl: 'trucks/views/hubs.html'
        }).when('/plans', {
            templateUrl: 'trucks/views/plans.html'
        }).when('/shipments', {
            templateUrl: 'trucks/views/shipments.html'
        }).when('/spokes', {
            templateUrl: 'trucks/views/spokes.html'
        }).when('/trucktypes', {
            templateUrl: 'trucks/views/trucktypes.html'
        }).when('/admin', {
            templateUrl: 'trucks/views/admin.html'
        }).when('/', {
            templateUrl: 'trucks/views/home.html'
        });
    }
]).controller('MenuController', ['$scope', '$location',
    function($scope, $location) {
        $scope.isPageSelected = function(page) {
            return page === $location.path().substring(1);
        };
    }
]);