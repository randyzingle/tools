// load the config data

var model = {
    application : {
      title: "Application-Specific Configuration",
      items : [{ name: "sourceTopic", value: "sourceTopic-systemproperties", "source": "x-config" },
              { name: "configServerPingRateMs", value: "60000" , "source": "x-config"},
              { name: "configServiceUrl", value: "http://configservice-dev.cidev.sas.us:8080/", "source": "x-config" },
              { name: "consumerGroup", value: "RawEventsEnrichmentReader" , "source": "x-config"}]
    },
    global : {
      title: "Global Configuration",
      items : [{ name: "redisClusterPrimaryEndpoint", value: "localhost", "source": "y-config" },
              { name: "redisClusterPrimaryEndpointPort", value: "6379" , "source": "x-config"},
              { name: "configBucket", value: "ci-360-config-dev-us-east-1", "source": "x-config" },
              { name: "dataBucket", value: "ci-360-data-dev-us-east-1" , "source": "x-config"}]
    }
};

var configApp = angular.module("configApp", []);

configApp.controller("ConfigCtrl", function($scope) {
  $scope.config = model;
})
