var oauthspa = angular.module("oauthspa", ['ui.router']);

var APIM_PORT = '8245';
var APIM_HOST_NAME = 'localhost';

var ANGULAR_PORT = '5000';
var ANGUALR_HOST_NAME = 'localhost';


var CLIENT_ID = 'fjvAzGOw_0ednPKwxgOtP35505oa';

var CALLBACK_EP = 'http://' + ANGUALR_HOST_NAME + ':' + ANGULAR_PORT + '/oauth_callback.html';
var LOGIN_PAGE = 'http://' + ANGUALR_HOST_NAME + ':' + ANGULAR_PORT + '/index.html'

var AUTHZ_EP = 'https://' + APIM_HOST_NAME + ':' + APIM_PORT + '/authorize';
var API_EP = 'https://'+ APIM_HOST_NAME +':' + APIM_PORT+ '/blocks/1.0.0/latestblock';

oauthspa.config(function($stateProvider, $urlRouterProvider) {

    window.localStorage.setItem("login_page",LOGIN_PAGE);

    $stateProvider
        .state('login', {
            url: '/login',
            templateUrl: 'templates/login.html',
            controller: 'OAuthController'
        })
        .state('app', {
            url: '/app',
            templateUrl: 'templates/app.html',
            controller: 'AppController'
        });
    $urlRouterProvider.otherwise('/login');
});

oauthspa.controller("OAuthController", function($scope) {
    $scope.login = function() {
        window.location.href = AUTHZ_EP + "?client_id=" + CLIENT_ID + "&response_type=token&redirect_uri=" + CALLBACK_EP
    }
});

oauthspa.controller('AppController', function($scope, $http) {

   $scope.accessToken = window.localStorage.getItem("access_token");

   $scope.getLatestBlock = function() {
   var token = 'Bearer ' + $scope.accessToken;
   $http.get(API_EP , {headers: {'Authorization': token}}).
        then(function successCallback(response) {
            $scope.block = response.data;
        }, function errorCallback(response) {
            window.location.href = LOGIN_PAGE + "#error=" + response.data;
        }
      );
    }
});
