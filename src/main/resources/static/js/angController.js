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
            		 emailID: $scope.user.email ,
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
	
	$scope.viewJob = function (job){
		console.log(job);
		$window.localStorage.setItem("jobToEdit", JSON.stringify(job));
		$window.location.href = "/viewPosting.html";
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


app.controller('viewJobCtrl', function($scope, $http, $window) {
	
	$scope.job = JSON.parse($window.localStorage.getItem("jobToEdit"));
	console.log($window.localStorage.getItem("jobToEdit"));	
	console.log($scope.job);
	
	$scope.updatePositionContent = function (){ 		
		$window.location.href = "/editJob.html";
	};
	

	$scope.cancelJob = function (job){ 
		console.log("cancelled");
		console.log($scope.job);
		$http({
	        url: '/jobs/updateStatus',
	        method: 'PUT',
	        data: {	
	        		"id": job.jobid,
	        		"Status": "CANCELLED"
	        	},
	        transformResponse: function (data, headersGetter, status) 
	        					{ 
	        						if(status=='404')
	        						{	
	        							console.log("error");
	        							return JSON.parse(data).badRequest.msg;
	        						}
	        						else if(status=='200')
	        						{
	        							console.log(data);
	        							return data;
	        						}
	        						else{
	        							data= JSON.parse(data);
	        							return data;
	        						}; 
	        					
	        					}
	    }).then(function successCallback(data) 
	    		{ 
	    		console.log(data);
	    		$scope.job.status = "CANCELLED";
	    		}, 
	    		function err(data) 
	    		{
	    		 console.log("error");
	    		 console.log(data.data);
	    		});
	};
	
	$scope.fillJob = function (job){ 
		console.log("filled");
		console.log($scope.job);
		$http({
	        url: '/jobs/updateStatus',
	        method: 'PUT',
	        data: {	
	        		"id": job.jobid,
	        		"Status": "FILLED"
	        	},
	        transformResponse: function (data, headersGetter, status) 
	        					{ 
	        						if(status=='404')
	        						{	
	        							console.log("error");
	        							return JSON.parse(data).badRequest.msg;
	        						}
	        						else if(status=='200')
	        						{
	        							console.log(data);
	        							
	        							return data;
	        						}
	        						else{
	        							data= JSON.parse(data);
	        							return data;
	        						}; 
	        					
	        					}
	    }).then(function successCallback(data) 
	    		{ 
	    		console.log(data);
	    		$scope.job.status = "FILLED";
	    		}, 
	    		function err(data) 
	    		{
	    		 console.log("error");
	    		 console.log(data.data);
	    		});
	};
	
	$scope.offerJob = function (job, id){ 
		console.log("offered");
		console.log(job);
		console.log(id);
	};
	
	$scope.rejectApplication = function (job, id){ 
		console.log("rejected");
		console.log(job);
		console.log(id);
	};
	
	$scope.viewApplicant = function (job, id){ 
		console.log("viewedProfile");
		console.log(job);
		console.log(id);
		$window.location.href = "/applicantProfile.html";
		
	};
	
	
	$http({
        url: '/jobApplicants/' + $scope.job.jobid,
        method: 'GET',
        transformResponse: function (data, headersGetter, status) 
        					{ 
        						if(status=='404')
        						{	
        							console.log("error");
        							return JSON.parse(data).badRequest.msg;
        						}
        						else if(status=='200')
        						{
        							data= JSON.parse(data);
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
    		$scope.applicants = data.data;
    		var i;
    		for(i=0;i<$scope.applicants.length ; i++)
    			{
    			console.log($scope.applicants[i]);
    			if($scope.applicants[i].status == "FILLED")
    				{
    				$scope.disableCancel=true;
    				$scope.disableFilled=true;
    				}
    			};
    		}, 
    		function err(data) 
    		{
    		 console.log("error");
    		 console.log(data.data);
    		 $scope.applicants = [
 				{"id":"1", "status":"pending", "firstname":"anudeep", "lastname": "chinta"},
 				{"id":"2", "status":"pending", "firstname":"edava", "lastname": "chinta"}
 		];
    		});
});

app.controller('editJobCtrl', function($scope, $http, $window) {
	
	$scope.job = JSON.parse($window.localStorage.getItem("jobToEdit"));
	console.log($window.localStorage.getItem("jobToEdit"));	
	console.log($scope.job);
	
	$scope.updateContent = function (job){
		console.log("updating job");
		$http({
	        url: '/jobs/updateContent',
	        method: 'PUT',
	        data: {	
	        		"id": job.jobid,
	        		"Title": job.jobtitle,
	        		"Description": job.description,
	        		"Responsibilities": job.skill,
	        		"Office Location": job.location,
	        		"Salary": job.salary
	        	},
	        transformResponse: function (data, headersGetter, status) 
	        					{ 
	        						if(status=='404')
	        						{	
	        							console.log("error");
	        							return data;
	        						}
	        						else if(status=='200')
	        						{
	        							console.log(data);
	        							return data;
	        						}
	        						else{
	        							data= JSON.parse(data);
	        							return data;
	        						}; 
	        					
	        					}
	    }).then(function successCallback(data) 
	    		{ 
	    		console.log(data);
	    		$window.localStorage.setItem("jobToEdit",JSON.stringify(job));
	    		$window.location.href = "/editJobSuccess.html";
	    		}, 
	    		function err(data) 
	    		{
	    		 console.log("error");
	    		 console.log(data);
	    		});
		
	};
	
	
});

app.controller('editJobSuccessCtrl', function($scope, $http, $window) {
	
	$scope.job = JSON.parse($window.localStorage.getItem("jobToEdit"));
	console.log($window.localStorage.getItem("jobToEdit"));	
	console.log($scope.job);
	
	$scope.gotoDashboard = function (){ 
		$window.location.href = "/EmployerDashboard.html";
	}
});

app.controller('editCompanyCtrl', function($scope, $http, $window) {
	
	$scope.company = {};
	$scope.job = JSON.parse($window.localStorage.getItem("jobToEdit"));
	console.log($scope.job);
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
