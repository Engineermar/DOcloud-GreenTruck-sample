
angular.module('trucks').controller('ShipmentsController', ['$scope', '$routeParams', '$location', '$http',
    function($scope, $routeParams, $location, $http) {


    $scope.shipments = [];
    $scope.editedShipment = null; // The shipment being edited
    $scope.editedShipmentCopy = null; // To restore original values if editing is cancelled
                                      // If null and editedShipment not null, the editedShipment is newly created
    $scope.validSpokes = [];

    $http.get("rest/v1/trucking/shipments")
        .success(function(data, status, headers, config) {
            $scope.shipments = data;
        }). error(function(data, status, headers, config) {
            console.log(status);
        });

     $http.get("rest/v1/trucking/spokes")
         .success(function(data, status, headers, cfg) {
             data.map(function( spoke ) {
                 $scope.validSpokes.push(spoke._id);
             });
        }).error(function(data, status, headers, cfg) {
             console.log(status);
        });

     $scope.startEditShipment = function(shipment, isNew) {
         $scope.editedShipment = shipment;
         $scope.editedShipmentCopy = isNew? null : angular.copy(shipment);
     };

    $scope.cancelShipmentEdit = function(shipment) {
        if (!$scope.editedShipmentCopy) {
            // If cancelling the editing of a newly created shipment, remove it from the list
            $scope.shipments.splice($scope.shipments.indexOf($scope.editedShipment), 1);
        }
        else {
            // Restore values of the shipment before it was edited
            angular.copy($scope.editedShipmentCopy, shipment);
            $scope.editedShipmentCopy = null;
        }
        $scope.editedShipment = null;
    };

    $scope.applyShipmentEdit = function(shipment) {
        if (!$scope.editedShipmentCopy) {
            // If it is a new shipment, add it
            $http.post("rest/v1/trucking/shipments", shipment)
                .success(function(newShipment, status, headers, config) {
                    $scope.shipments[$scope.shipments.indexOf(shipment)] = newShipment;
                    $scope.editedShipment = null;
                    // The shipment is already listed in $scope.shipments, nothing else to do
                }). error(function(data, status, headers, config) {
                    $scope.cancelShipmentEdit(shipment);
                    console.log(status);
                });
        }
        else {
            $http.put("rest/v1/trucking/shipments/" + shipment._id, shipment)
                .success(function (data, status, headers, config) {
                    $scope.editedShipment = $scope.editedShipmentCopy = null;
                }).error(function (data, status, headers, config) {
                    $scope.cancelShipmentEdit(shipment);
                    console.log(status);
                });
        }
    };

    $scope.isEditingShipment = function(shipment) {
        return $scope.editedShipment === shipment;
    };

    $scope.getEditedShipment = function() {
        return $scope.editedShipment;
    };

    $scope.saveShipment = function(shipment) {
        angular.copy($scope.editedShipmentCopy, shipment);
    };

    $scope.restoreShipment = function(shipment) {
        angular.copy($scope.$scope.editedShipment, shipment);
    };

    $scope.removeShipment = function(shipment, index) {
        $http.delete("rest/v1/trucking/shipments/" + shipment._id)
            .success(function(data, status, headers, config) {
                $scope.shipments.splice(index, 1);
            }). error(function(data, status, headers, config) {
                console.log(status);
            });
    };

    $scope.addShipment = function() {
        var shipment = { totalVolume: Math.floor((Math.random() * 10)) + 1 };
        function incSpokeIndex(index) { return (index + 1) == $scope.validSpokes.length? 0 : (index + 1); }
        // Look for a new valid shipment route
        for (var origCount = 0, orig = Math.floor((Math.random() * $scope.validSpokes.length)), match = false;
                    !match && (origCount < $scope.validSpokes.length); origCount++) {
            shipment.origin = $scope.validSpokes[ orig = incSpokeIndex(orig) ];
            for (var destCount = 0, dest = orig; !match && (destCount < ($scope.validSpokes.length - 1)); destCount++) { // When destCount == $scope.validSpokes.length - 1, dest == orig which leads to invalid route
                shipment.destination = $scope.validSpokes[ dest = incSpokeIndex(dest) ];
                match = !$scope.isDuplicateRoute(shipment);
            }
        }
        $scope.shipments.push(shipment);
        // Scroll to the bottom of the page
        window.scrollTo(0, document.body.scrollHeight);
        $scope.startEditShipment(shipment, true);
    };

    /*  Shipment editing validation  */
    $scope.isValidSpoke = function(spoke) {
        return $scope.validSpokes.indexOf(spoke) >= 0;
    };

    $scope.isDuplicateRoute = function(shipment) {
        for (var i = $scope.shipments.length - 1; i >= 0; i--) {
            var shipIter = $scope.shipments[i];
            if ( (shipment != shipIter) && (shipment.origin == shipIter.origin)
                            && (shipment.destination == shipIter.destination)) {
                return true;
            }
        }
        return false;
    };

    $scope.isValidVolume = function(volume) {
        return volume > 0;
    };

    }
]);