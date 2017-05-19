/**
 * 
 */
var app = angular.module('myApp', []);
app.controller('jobSeekerSignUpCtrl', function($scope, $http, $window) {
	$scope.user = {};
    $scope.submitData = function () {
    	console.log($scope.user.name);
    	console.log($scope.user.email);
    	console.log($scope.user.password);
    	$http({
            url: '/users/create',
            method: 'POST',
            transformResponse: function (data, headersGetter, status) 
            					{ 
            						if(status=='201')
            						{	
            							console.log("sample");
            							return data;
            						}
            						else{
            							data= JSON.parse(data);
            							return data;
            						}; 
            					
            					},
            data: {	 username: $scope.user.name , 
					 emailid: $scope.user.email ,
					 password: $scope.user.password
            }
        }).then(function successCallback(data) 
        		{ console.log(data);
        		  $window.localStorage.setItem("userEmail", $scope.user.email);
        		  $window.localStorage.setItem("userType", "user");
        		  $window.location.href = "/UserVerification.html";
        		}, 
        		function err(data) 
        		{
        		 console.log("error"); 
        		 console.log(data.data.badRequest.msg);
        		 $scope.serverMsg = data.data.badRequest.msg;
        		});
    	}
 });

app.controller('employerDashboardCtrl', function($scope, $http, $window) {
	
	$scope.postedJobs = {};
	$http({
        url: '/employer/jobs',
        method: 'GET',
        transformResponse: function (data, headersGetter, status) 
        					{ 
        						if(status=='403')
        						{	
        							console.log("error");
        							return data;
        						}
        						else{
        							data= JSON.parse(data);
        							return data;
        						}; 
        					
        					}
    }).then(function successCallback(data) 
    		{ 
    		console.log(data.data);
    		$scope.postedJobs = data.data;
    		}, 
    		function err(data) 
    		{
    		 console.log("error"); 
    		 console.log(data.data.badRequest.msg);
    		});
	
	$scope.editJob = function (job){
		$window.localStorage.setItem("jobToEdit", job);
		$window.location.href = "/editJob.html";
	}
	
});


app.controller('postJobsCtrl', function($scope, $http, $window) {
	
	$scope.job = {};
	$scope.submitJobData = function (){ 
		
		$http({
        url: '/jobs/post/',
        method: 'POST',
        transformResponse: function (data, headersGetter, status) 
        					{ 
        						if(status=='201')
        						{	
        							console.log("success");
        							return data;
        						}
        						else{
        							data= JSON.parse(data);
        							return data;
        						}; 
        					
        					},
		data: {	Title: $scope.job.job_title , 
				Description: $scope.job.desc, 
				Responsibilities: $scope.job.skills,
			 	'Office Location': $scope.job.location,
			 	Salary: $scope.job.salary
        }
    }).then(function successCallback(data) 
    		{ 
    		console.log(data);
    		$window.location.href = "/EmployerDashboard.html";
    		}, 
    		function err(data) 
    		{
    		 console.log("error"); 
    		 console.log(data.data.badRequest.msg);
    		});
	}
});


app.controller('editJobCtrl', function($scope, $http, $window) {
	
	$scope.job = $window.localStorage.getItem("jobToEdit");
	$scope.submitJobData = function (){ 
		
		$http({
        url: '/jobs/update/',
        method: 'PUT',
        transformResponse: function (data, headersGetter, status) 
        					{ 
        						if(status=='403')
        						{	
        							console.log("error");
        							return data;
        						}
        						else{
        							data= JSON.parse(data);
        							return data;
        						}; 
        					
        					},
		data: {	
			
				id: $scope.job.job_title,
				job_title: $scope.job.job_title , 
			 	desc: $scope.job.desc, 
			 	skills: $scope.job.skills,
			 	location: $scope.job.location,
			 	salary: $scope.job.salary
        }
    }).then(function successCallback(data) 
    		{ 
    		console.log(data);
    		$window.location.href = "/EmployerDashboard.html";
    		}, 
    		function err(data) 
    		{
    		 console.log("error"); 
    		 console.log(data.data.badRequest.msg);
    		});
	}
});

app.controller('editCompanyCtrl', function($scope, $http, $window) {
	
	$scope.company = {};
	$scope.submitCompanyData = function (){ 
		
		$http({
        url: '/employers/update/',
        method: 'PUT',
        transformResponse: function (data, headersGetter, status) 
        					{ 
        						if(status=='403')
        						{	
        							console.log("error");
        							return data;
        						}
        						else{
        							data= JSON.parse(data);
        							return data;
        						}; 
        					
        					},
		data: {	'Company Name': $scope.company.name,
		    	Description: $scope.company.description, 
		    	Website: $scope.company.Website,
		    	Address_Headquarters: $scope.company.Address_Headquarters,
		    	Logo_Image_URL: $scope.company.Logo_Image_URL
        }
    }).then(function successCallback(data) 
    		{ 
    		console.log(data.data);
    		$window.location.href = "/EmployerDashboard.html";
    		}, 
    		function err(data) 
    		{
    		 console.log("error"); 
    		 console.log(data.data.badRequest.msg);
    		});
	}
});
