'use strict';

var mainApplicationModuleName = 'greentruck';

var mainApplicationModule = angular.module(mainApplicationModuleName, ['ngRoute','trucks','smart-table','ngWebSocket' ]);

// Configure the hashbang URLs using the $locationProvider services 
mainApplicationModule.config(['$locationProvider',
    function($locationProvider) {
        $locationProvider.hashPrefix('!');
    }
]);


// Fix Facebook's OAuth bug
if (window.location.hash === '#_=_') window.location.hash = '#!';

// Manually bootstrap the AngularJS application
angular.element(document).ready(function() {
    angular.bootstrap(document, [mainApplicationModuleName]);
});

mainApplicationModule.config(['$httpProvider',
function($httpProvider) {
	$httpProvider.interceptors.push(function ($q, $location, $rootScope) {
		return {
			'response': function(response) {
				  $rootScope.apierror= null;
			      return response;
			    },
			'responseError': function (response) {
				console.log("Received error "+response.status);
				$rootScope.apierror = response;
				return $q.reject(response);
  				}
  			};
  		});
  	}
  ]);