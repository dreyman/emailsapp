const app = angular.module('emailsApp', []);

app.controller('mainController', mainController);

mainController.$inject = ['$http', '$scope'];

function mainController($http, $scope) {
  const vm = this;
  let eventSource = new EventSource('/api/emails/stream');
  vm.submit = submit;
  vm.input = '';
  vm.items = [];

  eventSource.onmessage = function (event) {
    const newItem = JSON.parse(event.data);
    if (addItem(newItem)) {
      $scope.$apply();
    }
  }

  init();

  function init() {
    $http.get('/api/emails/all')
      .then(resp => vm.items = resp.data);
  }

  function submit() {
    $http.post('/api/emails/new', vm.input)
      .then(resp => { vm.input = ''; addItem(resp.data) });
  }

  function addItem(newItem) {
    if (!vm.items.find(item => item.id === newItem.id)) {
      vm.items.push(newItem);
      return true;
    }
    return false;
  }
}
