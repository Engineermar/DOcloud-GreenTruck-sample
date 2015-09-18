
angular.module('trucks').controller('PlansController', ['$scope', '$routeParams', '$location', '$http', '$q', '$websocket',
    function($scope, $routeParams, $location, $http, $q, $websocket) {

		var websocketproto = "ws:";
	    if (window.location.protocol == "https:") {
		    websocketproto = "wss:";
		}
		var url = websocketproto + '//' + window.document.location.host + '/StatusEventEndpoint';
	
	    // Open a WebSocket connection
		console.log("Opening websocket at "+url);
	    var dataStream = $websocket(url,null,{reconnectIfNotNormalClose:true });
	    dataStream.onMessage(function(message) {
	    	console.log("Received message: "+message.data);
	        $scope.status = JSON.parse(message.data);
	        	        
	        if ($scope.status.jobStatus=="PROCESSED" && 
	        		($scope.solution==null || $scope.solution.jobid!=$scope.status.jobid)){
	        	console.log("New solution available "+$scope.status.jobid);
	        	$http.get("rest/v1/trucking/solution")
	        	  .then(function (response){
		    		  $scope.solution = response.data;
		    	   }).catch(function (error){
		    		  console.log(error);
		    	   });
	        }
	        
	        if ($scope.status.jobStatus=="FAILED"){
	        	alert("Job "+$scope.status.jobid+" failed, "+$scope.status.message);
	        }
	    });
	    
	    dataStream.onOpen(function(){
	    	console.log("Websoket open");
	    });
	    
	    dataStream.onClose(function(){
	    	console.log("Websoket close");
	    });
	    
	    dataStream.onError(function(){
	    	console.log("Websoket error");
	    })
	    
	    $scope.$on('$destroy', function(){
	    	dataStream.close(true);
	    })
	
	    $scope.status= null;
	    $scope.solution = null;
	    $scope.running=false;

		// Load solution and master data
		$q.all([
			$http.get('rest/v1/trucking/hubs'),
			$http.get('rest/v1/trucking/spokes'),
			$http.get("rest/v1/trucking/truckTypes"),
			$http.get('rest/v1/trucking/solution')
		])
		.then(function(responses) {
			$scope.hubs = responses[0].data;
			$scope.spokes = responses[1].data;
			$scope.trucktypes = responses[2].data;
			$scope.solution = responses[3].data;
		}, function(reason) {
			console.log(reason);
		});

		$scope.createGraphModel = function (){
			// Store nodes and node indices
			var graphNodes = $scope.graph.nodes = [];
			var indices = {};
			var color = d3.scale.category20();
			[$scope.hubs, $scope.spokes].map(function(nodes, i) {
				nodes.map(function(node) {
					indices[node._id] = graphNodes.length;
					graphNodes.push({ name: node._id, color: color(i), size: i && 16 || 26 });
				});
			});

			// Create distance "matrix"
			var distanceMatrix = {};
			$scope.hubs.map(function(hub) {
				hub.routes.map(function(route) {
					distanceMatrix[route.spoke] = distanceMatrix[route.spoke] || {};
					distanceMatrix[route.spoke][hub._id] = route.distance;
				});
			});
			function getDistance(spoke, hub) {
				return distanceMatrix[spoke] && distanceMatrix[spoke][hub] || 0;
			}

			// Create links
			var graphLinks = $scope.graph.links = [];
			if($scope.solution && $scope.solution.trucks) {
				$scope.solution.trucks.map(function (truckRoute) {
					graphLinks.push({
						source: indices[truckRoute.hub],
						target: indices[truckRoute.spoke],
						distance: getDistance(truckRoute.spoke, truckRoute.hub),
						nbTruck: truckRoute.nbTruck,
						strokeWidth: truckRoute.truckType == 'BigTruck'? 3 : 1,
						truckType: truckRoute.truckType
					});
				});
			}
		};

		$scope.updateGraph = function() {
			// displayedTrucks as the displayed rows for the Angular Smart tables
			$scope.displayedTrucks = $scope.solution && $scope.solution.trucks && [].concat( $scope.solution.trucks ) || [];
			if ($scope.hubs) {
				$scope.createGraphModel();
			}
			else {
				$scope.graph.nodes = $scope.graph.links = [];
			}
			var force = d3.layout.force()
				.charge(-300)
				/*.charge(-800)*/
				.linkDistance(function(link) {
					return link.distance;
				})
				.size([$scope.graph.width, $scope.graph.height]);
			force
				.nodes($scope.graph.nodes)
				.links($scope.graph.links)
				.on("tick", function(){$scope.$apply()})
				.start();
		};

		// Update the graph each time the solution changes
		$scope.graph = { width : 500, height: 300 };
		$scope.$watch('solution', $scope.updateGraph);
	
	    $scope.deletePlan = function (){
	    	console.log("Plan deletion requested");
	    	 $http.delete("rest/v1/trucking/solution")
		        .success(function(data, status, headers, config) {
		            $scope.solution = null;
		            console.log("Plan deletion done");
		        }). error(function(data, status, headers, config) {
		        	console.log("Plan deletion error "+ status);
		        });
	    };
	    
	    $scope.optimize = function (){
	    	console.log("Optimization requested");
	    	 $http.post("rest/v1/trucking/solveAsync")
	    	     .success(function(data, status, headers, config) {		           
		            console.log("Optimization requested submitted");
		        }). error(function(data, status, headers, config) {
		        	console.log("Optimization request error error "+ status + " " + data.message);
		        });
	    	
	    	 	    	 
	    }
    }
]);