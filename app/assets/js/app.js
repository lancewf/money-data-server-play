(function() {
  var app = angular.module('sensorstation-service', []);

  app.controller('TabController', function(){
    this.tab = 1;

    this.setTab = function(tab){
      this.tab = tab;
    };

    this.isSet = function(tab){
      return (this.tab === tab);
    };
  });

  app.directive("home", [ '$http', function($http){
    return {
      restrict: 'E',
      templateUrl: "assets/html/home.html",
      controller: function($http) {
       this.bankTransactions = [];
       this.selectedBankTransaction = [];
       this.selectedIndex = 0;
       this.matchingPurchases = [];
       var self = this;
       this.add = function(){
          var f = document.getElementById('file').files[0],
              r = new FileReader();
          r.onloadend = function(e){
            var data = e.target.result;
            var data1 = {filename: "me", xml: data};
            $http({
                url:"/services/upload",
                method: "POST",
                data:data1
            }).success(function(data, status) {
                self.bankTransactions = data;
                self.selectedIndex = 0;
                self.selectedBankTransaction = self.bankTransactions[self.selectedIndex];
                self.findMatchingPurchases();
                console.log("it is back size: " + data.length);
            }).error(function (data, status, headers, config) {
                console.log("something wrong: " + data);
            });
          };
          r.readAsBinaryString(f);
       };
       this.previous = function(){
            self.selectedIndex = self.selectedIndex - 1;
            if(self.selectedIndex < 0){
                self.selectedIndex = self.bankTransactions.length -1;
            }
            self.selectedBankTransaction = self.bankTransactions[self.selectedIndex];
            self.findMatchingPurchases();
       };
        this.next = function(){
           self.selectedIndex = self.selectedIndex + 1;
            if(self.selectedIndex >= self.bankTransactions.length){
               self.selectedIndex = 0;
            }
           self.selectedBankTransaction = self.bankTransactions[self.selectedIndex];
           self.findMatchingPurchases();
        };
        this.findMatchingPurchases = function(){
          $http.get('/services/getMatchingPurchases?cost=' + self.bankTransactions[self.selectedIndex].cost + '&dateMilisec=' + self.bankTransactions[self.selectedIndex].date
           ).success(function(data){
              self.matchingPurchases = data;
          }).error(function(data, status, headers, config) {
            console.log("something wrong findMatchingPurchases: " + data);
          });
        };
      },
      controllerAs: "homeCtrl"
    };
  }]);

})();