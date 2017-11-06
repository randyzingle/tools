// load the config data

var configApp = angular.module("configApp", []);

configApp.controller("ConfigCtrl", function($scope, $http, $location) {
	var baseUrl = $location.$$absUrl;
	var contextPath = '/' + $location.$$absUrl.split('/')[3];
	console.log(baseUrl);
	console.log(contextPath);
    $scope.getProps = function() {
    $http({
      method: 'GET',
      url: contextPath + '/props'
    }).then(function successCallback(response) {
        $scope.props = response.data;
        console.log($scope.props);
      }, function errorCallback(response) {
        console.log('failed to get properties from server');
    });
  }
  $scope.getProps();

});
