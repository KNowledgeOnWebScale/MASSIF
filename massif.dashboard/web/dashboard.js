angular.module('dashboardApp', ['ngMaterial']).config(function ($mdThemingProvider) {
    $mdThemingProvider.theme('default').dark().primaryPalette('grey');
}).controller('ListServiceController', function ($scope, $http, $mdDialog, $mdMedia) {
    $scope.customFullscreen = $mdMedia('xs') || $mdMedia('sm');
    $scope.reload = function () {
        if (!$scope.dialogOpen) {
            $http.get('/dashboard').then(function (response) {
                //success
                $scope.services = response.data['services'];
                $scope.sensors = response.data['sensors']['state'];
                //handle requests
                console.log(response);
                for (var requestId in response.data['sensors']['requests']) {
                    var request = response.data['sensors']['requests'][requestId];
                    var newValue = request['newValue'];
                    $scope.sendValue(request, newValue);
                }
                setTimeout($scope.reload, 1000);
            }, function (response) {
                $scope.services = [
                    {
                        adaptable: true
                        , componentName: 'Pressence.Service'
                        , serviceId: '38'
                        , filterRules: ['[SubClassOf(<http://massif.example.owl#CallFilter> ObjectSomeValuesFrom(<http://massif.example.owl#hasContext> <http://massif.example.owl#Call>))]'
                             , '[SubClassOf(<http://massif.example.owl#CallFilter2> ObjectSomeValuesFrom(<http://massif.example.owl#hasContext> <http://massif.example.owl#Call>))]']
                        , queries: ['PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nCONSTRUCT{<http://IBCNServices.github.io/Accio-Ontology/SSNiot#Notification> <http://IBCNServices.github.io/Accio-Ontology/SSNiot#hasContext> <http://IBCNServices.github.io/Accio-Ontology/SSNiot#TestObservation>}\nWHERE { ?subject ?property ?object }', 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nCONSTRUCT{<http://IBCNServices.github.io/Accio-Ontology/SSNiot#Notification> <http://IBCNServices.github.io/Accio-Ontology/SSNiot#hasContext> <http://IBCNServices.github.io/Accio-Ontology/SSNiot#TestObservation>}\nWHERE { ?subject ?property ?object }']
                }
            , {
                        adaptable: true
                        , componentName: 'Employee.Service'
                        , serviceId: '37'
                        , filterRules: ['[SubClassOf(<http://massif.example.owl#CallFilter> ObjectSomeValuesFrom(<http://massif.example.owl#hasContext> <http://massif.example.owl#Call>))]'
                             , '[SubClassOf(<http://massif.example.owl#CallFilter2> ObjectSomeValuesFrom(<http://massif.example.owl#hasContext> <http://massif.example.owl#Call>))]']
                        , queries: ['Query1', 'Query2']
                }


            ];
                $scope.sensors = [
                    {
                        sensorType: 'BLE'
                        , sensorId: 10
                        , virtual: true
                        , type: 'button'
                        , icon: 'col-xs-6 icon icon-temperature down'
                        , value: '20:E3:4R:F4:3D'
                        , online: true
            }
            , {
                        sensorType: 'BLE'
                        , sensorId: 13
                        , virtual: true
                        , type: 'button'
                        , icon: 'col-xs-6 icon icon-temperature down'
                        , value: '20:E3:4R:F4:RR'
                        , online: false
            }
            , {
                        sensorType: 'Temperature'
                        , sensorId: 11
                        , virtual: false
                        , type: 'temperature'
                        , icon: 'col-xs-6 icon icon-button up'
                        , value: '37'
                        , online: true
            }
            , {
                        sensorType: 'Button'
                        , sensorId: 12
                        , virtual: false
                        , type: 'button'
                        , icon: 'col-xs-6 icon icon-button up'
                        , value: 'false'
                        , online: true
            }

        ];
                console.log($scope);
                setTimeout($scope.reload, 5000);
                $scope.greeting = 'Hello';
            });
        }
        else {
            setTimeout($scope.reload, 5000);
        }
    }
    $scope.reload();
    $scope.addQuery = function (queryId) {
        console.log($scope);
        console.log($scope.services.activeService);
        var dataObj = {
            serviceID: $scope.services[queryId].serviceId
            , newQuery: $scope.services[queryId].newQuery
        };
        console.log(dataObj);
        $http.post('/dashboard/addQuery', dataObj).success(function (response) {
            $scope.services = response.data['services'];
            $scope.updateDialog(queryId);
        });
    }
    $scope.removeQuery = function (serviceId, removeQuery) {
        var dataObj = {
            serviceID: $scope.services[serviceId].serviceId
            , removeQuery: removeQuery
        };
        console.log(dataObj);
        $http.post('/dashboard/removeQuery', dataObj).success(function (response) {
            $scope.services = response.data['services'];
            $scope.updateDialog(serviceId);
        });
    }
    $scope.addFilter = function (serviceId) {
        var dataObj = {
            serviceID: $scope.services[serviceId].serviceId
            , newFilter: $scope.services[serviceId].newFilter.filter
            , filterName: $scope.services[serviceId].newFilter.name
        };
        console.log(dataObj);
        $http.post('/dashboard/addFilter', dataObj).success(function (response) {
            $scope.services = response.data['services'];
            $scope.updateDialog(serviceId);
        });
    }
    $scope.removeFilter = function (serviceId, removeFilter) {
        var dataObj = {
            serviceID: $scope.services[serviceId].serviceId
            , removeFilter: removeFilter
        };
        console.log(dataObj);
        $http.post('/dashboard/removeFilter', dataObj).success(function (response) {
            $scope.services = response.data['services'];
            $scope.updateDialog(serviceId);
        });
    }
    $scope.addService = function () {
        var dataObj = {
            name: $scope.services.newService.name
            , description: $scope.services.newService.description
            , input: $scope.services.newService.newFilters
            , query: $scope.services.newService.newQueries
            , ontology: $scope.services.newService.ontology
        };
        console.log(dataObj);
        $http.post('/dashboard/newService', dataObj).success(function (response) {
            $scope.services = response.data['services'];
        });
        $scope.hide();
    }
    $scope.newColor = function (employee, newColor) {
        var dataObj = color;
        var newMap = $scope.test(dataObj, employee);
        newMap["dul:hasDataValue"] = newColor;
        console.log(newMap);
        $http.post('/gateway', newMap).success(function (response) {
            // $scope.services = response.data['services'];
        });
        $scope.hide();
    }
    $scope.update = function () {
        console.log("double click");
        var modalInstance = $modal.open({
            templateUrl: 'index.html'
            , controller: 'ListServiceController'
            , resolve: {
                thing: function () {
                    return "test";
                }
            }
        });
    }
    $scope.addNewFilter = function () {
        $scope.services.newService.newFilters.push({});
    }
    $scope.addNewQuery = function () {
        $scope.services.newService.newQueries.push({});
    }
    $scope.showAlert2 = function (ev) {
        // Appending dialog to document.body to cover sidenav in docs app
        // Modal dialogs should fully cover application
        // to prevent interaction outside of dialog
        $mdDialog.show($mdDialog.alert().parent(angular.element(document.querySelector('#popupContainer'))).clickOutsideToClose(true).title('This is an alert title').textContent('You can specify some description text in here.').ariaLabel('Alert Dialog Demo').ok('Got it!').targetEvent(ev));
    }
    $scope.showAlert = function (ev, serviceId) {
        // Appending dialog to document.body to cover sidenav in docs app
        // Modal dialogs should fully cover application
        // to prevent interaction outside of dialog
        $scope.services.activeService = $scope.services[serviceId];
        $scope.services.activeServiceId = serviceId;
        $scope.dialogOpen = true;
        $mdDialog.show({
            targetEvent: ev
            , locals: {
                parent: $scope
            }
            , controller: angular.noop
            , controllerAs: 'ctrl'
            , bindToController: true
            , templateUrl: 'serviceinfo.html'
            , clickOutsideToClose: true
        });
    };
    $scope.showSensorInfo = function (ev, sensorId) {
        // Appending dialog to document.body to cover sidenav in docs app
        // Modal dialogs should fully cover application
        // to prevent interaction outside of dialog
        console.log(sensorId)
        $scope.sensors.activeSensor = $scope.sensors[sensorId];
        console.log($scope.sensors.activeSensor)
        $scope.sensors.activeSensorId = sensorId;
        $scope.dialogOpen = true;
        $mdDialog.show({
            targetEvent: ev
            , locals: {
                parent: $scope
            }
            , controller: angular.noop
            , controllerAs: 'ctrl'
            , bindToController: true
            , templateUrl: $scope.toSensorFile($scope.sensors.activeSensor.type)
            , clickOutsideToClose: false
        });
    };
    $scope.profileDialog = function (ev) {
        $scope.colors = ["Red", "Blue", "Green", "Yellow", "Pink", "Grey", "Brown", "White"];
        $mdDialog.show({
            targetEvent: ev
            , locals: {
                parent: $scope
            }
            , controller: angular.noop
            , controllerAs: 'ctrl'
            , bindToController: true
            , templateUrl: 'profile.html'
            , clickOutsideToClose: false
        });
    }
    $scope.addServiceDialog = function (ev) {
        $scope.services.newService = [];
        $scope.services.newService.newFilters = [];
        $scope.services.newService.newFilters.push({
            name: 'Insert Name'
            , input: 'Define input'
        });
        $scope.services.newService.newQueries = [];
        $scope.services.newService.newQueries.push({
            query: 'Add Query'
        });
        $scope.dialogOpen = true;
        $mdDialog.show({
            targetEvent: ev
            , locals: {
                parent: $scope
            }
            , controller: angular.noop
            , controllerAs: 'ctrl'
            , bindToController: true
            , templateUrl: 'addService.html'
            , clickOutsideToClose: false
        });
    };
    $scope.hide = function () {
        $mdDialog.hide();
        $scope.dialogOpen = false;
    };
    $scope.cancel = function () {
        $mdDialog.cancel();
        $scope.dialogOpen = false;
    };
    $scope.answer = function (answer) {
        $mdDialog.hide(answer);
        $scope.dialogOpen = false;
    };
    $scope.updateDialog = function (serviceCounter) {
        $scope.services.activeService = $scope.services[serviceCounter];
        $scope.services.activeServiceId = serviceCounter;
    };
    $scope.replaceNewLine = function (input) {
        var newline = String.fromCharCode(13, 10);
        return input.replace(/\n/g, newline);
    };
    $scope.toIcon = function (sensorId) {
        var activeSensor = $scope.sensors[sensorId];
        var type = activeSensor.type;
        var value = activeSensor.value;
        var active = 'down';
        if (value == 'false') {
            active = 'up';
        }
        if (!activeSensor.online) {
            active = 'up';
        }
        return 'col-xs-6 icon icon-' + type + ' ' + active;
    };
    $scope.toSensorFile = function (type) {
        var newType = 'sensorinfo';
        if (type == 'button') {
            newType = 'button';
        }
        return 'sensors/' + newType + '.html'
    }
    $scope.sendValue = function (activeSensor, newValue) {
        var dataObj = button;
        if (activeSensor.type == 'temperature') {
            dataObj = temperature;
        }
        else if (activeSensor.type == 'contact') {
            dataObj = contact;
        }
        else if (activeSensor.type == 'lamp') {
            dataObj = lamp;
        }
        else if (activeSensor.type == 'locked') {
            dataObj = lock;
        }
        else if (activeSensor.type == 'person') {
            dataObj = person;
        }
        else if (activeSensor.type == 'window') {
            dataObj = windowObj;
        }
        var newMap = $scope.test(dataObj, activeSensor.sensorId);
        newMap["ssn:observationResult"]["ssn:hasValue"]["dul:hasDataValue"] = newValue;
        newMap["ssn:observedBy"]["upper:hasID"] = activeSensor.sensorId;
        console.log(newMap);
        $http.post('/gateway', newMap).success(function (response) {
            // $scope.services = response.data['services'];
        });
        $scope.hide();
    }
    $scope.test = function (dataObj, id) {
        if (typeof dataObj === 'string') {
            var newObj = dataObj;
            return newObj.replace('@id@', id);
        }
        else if (Array.isArray(dataObj)) {
            var newObj = [];
            var len = dataObj.length;
            for (i = 0; i < len; i++) {
                newObj[i] = $scope.test(dataObj[i], id);
            }
            return newObj;
        }
        else {
            var newObj = {};
            for (x in dataObj) {
                newObj[x] = $scope.test(dataObj[x], id);
            }
            return newObj;
        }
    }
});