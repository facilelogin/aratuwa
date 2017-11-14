var oauthspa = angular.module("oauthspa", ["ngRoute","base64"]);

var APIM_PORT = '8243';
var APIM_HOST_NAME = 'localhost';

var ANGULAR_PORT = '5000';
var ANGUALR_HOST_NAME = 'localhost';


var CLIENT_ID = 'RRXJR_QK5r6famOs1F2Zsh6ibJIa';

var CALLBACK_EP = 'http://' + ANGUALR_HOST_NAME + ':' + ANGULAR_PORT + '/oauth_callback.html';
var LOGIN_PAGE = 'http://' + ANGUALR_HOST_NAME + ':' + ANGULAR_PORT + '/index.html'

var AUTHZ_EP = 'https://' + APIM_HOST_NAME + ':' + APIM_PORT + '/authorize';
var API_EP = 'https://'+ APIM_HOST_NAME +':' + APIM_PORT+ '/blocks/1.0.0/latestblock';

oauthspa.config(function($routeProvider) {

  sessionStorage.setItem("login_page",LOGIN_PAGE);

  $routeProvider
    .when("/login", {
          templateUrl : "templates/login.html",
          controller: 'AppController'
    })
    .when("/", {
          templateUrl : "templates/login.html",
          controller: 'AppController'
    })
    .when("/error", {
          templateUrl : "templates/error.html",
          controller: 'AppController'
    })
    .when("/app", {
        templateUrl : "templates/app.html",
        controller: 'AppController'
    });
});

oauthspa.controller('AppController', function($scope, $http, $base64) {

   $scope.accessToken = sessionStorage.getItem("access_token");

   $scope.login = function() {
       // use an angular js library to generate a UUID instead of the following.
       var d = new Date();
       var n = d.getTime();
       window.location.href = AUTHZ_EP + "?client_id=" + CLIENT_ID + "&response_type=id_token token&scope=openid&nonce="+ n +"&redirect_uri=" + CALLBACK_EP
   }

   $scope.showIDToken = function() {
       $scope.decoded_jwt = sessionStorage.getItem("id_token");
   }

   $scope.getLatestBlock = function() {
   var token = 'Bearer ' + $scope.accessToken;
   $http.get(API_EP , {headers: {'Authorization': token}}).
        then(function successCallback(response) {
            $scope.block = response.data;
        }, function errorCallback(response) {
            window.location.href = LOGIN_PAGE + "#!error=" + response.data;
        }
      );
    }
});
