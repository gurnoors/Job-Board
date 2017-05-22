/**
 * Created by Ramkumar on 5/14/2017.
 */



function ajaxCall(method, url, data, callback) {

    
    
    var httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = handleResponse;
    httpRequest.open(method, url);
    httpRequest.setRequestHeader("Content-Type", "application/json");
    
    httpRequest.send(JSON.stringify(data));

    function handleResponse() {
        if (httpRequest.readyState == 4) {
            callback(httpRequest.status, httpRequest.responseText);
        }
    }
}


function search(e) {

    e.preventDefault();

    var searchForm = e.target;
    var inputArray = searchForm.getElementsByTagName("input");

var FreeTextSearch,SearchByCompany,SearchByLocation,SearchBySalary;

    for (var i = 0; i < inputArray.length; i++) {
        var input = inputArray[i];

        if (input.getAttribute("name") === "FreeTextSearch") {

               if(input.value === undefined || input.value === "")
                   FreeTextSearch = "null";
               else
                   FreeTextSearch = input.value;
        }

        if (input.getAttribute("name") === "SearchByCompany") {
            if(input.value === undefined || input.value === "")
                SearchByCompany = "null";
            else
                SearchByCompany = input.value;
        }

        if (input.getAttribute("name") === "SearchByLocation") {
            if(input.value === undefined || input.value === "")
                SearchByLocation = "null";
            else
                SearchByLocation = input.value;
        }

        if (input.getAttribute("name") === "SearchBySalary") {
            if(input.value === undefined || input.value === "")
                SearchBySalary = "0";
            else
                SearchBySalary = input.value;
        }

    }

    var url = "/jobs/search/"+FreeTextSearch+"/"+SearchByCompany+"/"+SearchByLocation+"/"+SearchBySalary;
    
    console.log(url);
    
    var searchRequestObj = {};
    
    ajaxCall("GET", url, null , function (status, body) {
       
    	if (status == 200 || status == 201) {
            var searchResponseObj = JSON.parse(body);
            console.log(JSON.stringify(searchResponseObj));
            console.log(searchResponseObj);
            viewJobs(e,searchResponseObj);
        }
        else {
            console.log("Search not done : " + status);
        }

    	
    });
    	 

    
   
    	

 
    	


}


function viewJobs(e,searchResponseObj) {
	
	
	$('#searchJobResults').empty();
	
	
	var headerResult = '<div class="panel-title text-center"><h1 class="title"> Search Results </h1></div>';
	
	$(headerResult).appendTo("#searchJobResults");
	
	for (var i = 0; i < searchResponseObj.length; i++) {

        var jobid = searchResponseObj[i]["jobid"];
        var jobtitle = searchResponseObj[i]["jobtitle"];
        var skill = searchResponseObj[i]["skill"];
        var description = searchResponseObj[i]["description"];
        var location = searchResponseObj[i]["location"];
        var salary = searchResponseObj[i]["salary"];
         var company = searchResponseObj[i]["company"];



         var searchList = '<div class="main-login main center">' +
                   '<a href="#" id = "jobTitleId" onclick = "redirectjobviewPage(' +jobid +')"><b>'+  (i+1) + ") " +
                     jobtitle+"-"+jobid + '</b></a></div>';
        
        	$(searchList).appendTo("#searchJobResults").ready(function(){
        	
        		var jobid = document.getElementById("jobTitleId");
        		
        	

                
        });    
    }
}

function loadAppliedViewPage() {


    $('#appliedJobResults').empty();

    var url = "user/getAppliedJobs";

    var appliedRequestObj = {};
    var appliedResponseObj = {};
    ajaxCall("GET", url, appliedRequestObj, function (status, body) {
        if (status == 200) {
          appliedResponseObj = JSON.parse(body);
          
          console.log(appliedResponseObj);
          
          for (var i = 0; i < appliedResponseObj.length; i++) {

              var jobId = appliedResponseObj[i]["job"]["jobid"];
              var jobTitle = appliedResponseObj[i]["job"]["jobtitle"];
              var applicationStatus = appliedResponseObj[i]["status"];
          
              var appliedJobsList = '<div class="form-group">'+
                  '<div class="cols-sm-10" style="text-align: left;margin-left: 8px">'+
                  '<p id = "jobTitle-'+jobId+'"><b>'+jobTitle+'</b></p>'+
                  '<p id = "'+jobId+'">Job ID: <b>'+jobId+'</b></p>'+
                  '</div><div class="cols-sm-10" style="text-align: left;margin-left: 8px">'+
                  'Job status : '+
                  '<p id = "jobStatus-'+jobId+'"><b>'+applicationStatus+'</b></p> ' +
                  '</div>'+
                  '<div class="form-group">'+
                 '<button type="button" id = "Accept-' +jobId+'"class="btn btn-primary btn-group-horizontal login-button">'+
                 'Accept</button>&nbsp'+
                  '<button type="button" id = "Reject-' +jobId+'"class="btn btn-primary btn-group-horizontal login-button">'+
                  'Reject</button>&nbsp'+
                  '<button type="button" id = "Cancel-' +jobId+'"class="btn btn-primary btn-group-horizontal login-button">'+
                  'Cancel</button>&nbsp'+
                  '</div></div>';

              $(appliedJobsList).appendTo("#appliedJobResults");
              
              console.log("Changing button colors");
              
              var checktag = 'p#jobStatus-'+jobId;
              
              console.log(checktag);
              
              console.log($(checktag).text());
              
              if($('p#jobStatus-'+jobId).text() == "PENDING")
            	{
            	  	document.getElementById("Accept-"+jobId).disabled = true;
	        		document.getElementById("Reject-"+jobId).disabled = true;
	        		document.getElementById("Cancel-"+jobId).disabled = false;
            	}  
              else if($('p#jobStatus-'+jobId).text() == "OFFERED")
            	{
            	  	document.getElementById("Accept-"+jobId).disabled = false;
	        		document.getElementById("Reject-"+jobId).disabled = false;
	        		document.getElementById("Cancel-"+jobId).disabled = true;
            	}  
              
              else if($('p#jobStatus-'+jobId).text() == "OFFER_ACCEPTED")
          	{
          	  	document.getElementById("Accept-"+jobId).disabled = false;
	        		document.getElementById("Reject-"+jobId).disabled = false;
	        		document.getElementById("Cancel-"+jobId).disabled = false;
          	}
              else if($('p#jobStatus-'+jobId).text() == "OFFER_REJECTED")
            	{
            	  	document.getElementById("Accept-"+jobId).disabled = false;
  	        		document.getElementById("Reject-"+jobId).disabled = false;
  	        		document.getElementById("Cancel-"+jobId).disabled = false;
            	}
              else if($('p#jobStatus-'+jobId).text() == "CANCELLED" || $('p#jobStatus-'+jobId) == "FILLED" )
          	{
          	  	    document.getElementById("Accept-"+jobId).disabled = true;
	        		document.getElementById("Reject-"+jobId).disabled = true;
	        		document.getElementById("Cancel-"+jobId).disabled = true;
          	}
                
              
              
          }

        }
         });


    


    }


function loadInterestedViewPage() {


    $('#interestedJobResults').empty();

    var url = "user/getInterestedJobs";

    var interestedRequestObj = {};
    var interestedResponseObj = {};
    ajaxCall("GET", url, interestedRequestObj, function (status, body) {
        if (status == 200) {
        	interestedResponseObj = JSON.parse(body);
          
          console.log(interestedResponseObj);
          
          for (var i = 0; i < interestedResponseObj.length; i++) {

              var jobId = interestedResponseObj[i]["job"]["jobid"];
              var jobTitle = interestedResponseObj[i]["job"]["jobtitle"];
              var applicationStatus = interestedResponseObj[i]["status"];

              var appliedJobsList = '<div class="form-group" style="display: flex;flex-direction: column">'+
                  '<div class="cols-sm-10">'+
                  
                  '<p id = "jobTitle-'+jobId+'"><b>'+jobTitle+'</b></p>'+
                  '<p id = "'+jobId+'">Job ID: '+jobId+'</p>'+
                  '</div><div class="cols-sm-10">'+
                  'Job status :'+
                  '<p id = "jobStatus-'+jobId+'"><b> '+ applicationStatus+ '</b></p> ' +
                  '</div>'+
                  '<div class="form-group">'+
                 '<button type="button" id = "Accept-' +jobId+'"class="btn btn-primary btn-group-vertical login-button">'+
                 'Accept</button>&nbsp'+
                  '<button type="button" id = "Reject-' +jobId+'"class="btn btn-primary btn-group-vertical login-button">'+
                  'Reject</button>&nbsp'+
                  '<button type="button" id = "Cancel-' +jobId+'"class="btn btn-primary btn-group-vertical login-button">'+
                  'Cancel</button>&nbsp'+
                  '</div></div>';

              $(appliedJobsList).appendTo("#interestedJobResults");
          }

        }
        
        else if (status == 404)
        {       	
        	 document.getElementById("error").innerHTML = "Users currently don't have interested jobs.";
          	 document.getElementById("error").innerHTML += "<br><br><b> P.S Interested Jobs are jobs that are not applied by the user but marked interested </b>";
             document.getElementById("error").style.display = "block";
             return;
        	}
         });


    


    }




//load view of a particular job
function redirectjobviewPage(jobid)
{

    window.location = "/JobView.html";
    localStorage.setItem("jobId",jobid);
    
}

function loadjobviewPage()
{


    var jobid = localStorage.getItem("jobId");
    

    var url = "/jobs/view/"+jobid;
    
    var jobViewRequestObj = {};
    
    ajaxCall("GET", url , jobViewRequestObj, function (status, body) {

        if (status == 200) {

            var responseObj = JSON.parse(body);
           
            console.log(responseObj);
            
            var jobid = responseObj["jobid"];
            var jobtitle = responseObj["jobtitle"];
            var skill = responseObj["skill"];
            var description = responseObj["description"];
            var location = responseObj["location"];
            var salary = responseObj["salary"];
            var status = responseObj["status"];
            var company = responseObj["company"];
                       
            $('p#jobTitle').text("JobTitle : "+jobtitle);
            $('p#skill').text("Skills : "+skill);
            $('p#description').text("Description : "+description);
            $('p#location').text("Location : "+location);
            $('p#salary').text("Salary : "+salary);
            $('p#company').text("Company : "+ company["name"]);
            
        } 
    });
    

    
   

}


function apply(e) {

	alert("IN apply function ");
    e.preventDefault();
    var jobID;

    var applyRequestObj = {};
    
    jobID = localStorage.getItem("jobId");
    
    var url = "/jobs/view/" + Number(jobID) + "/apply";
    
    applyRequestObj["applicationType"] = "applied";
    applyRequestObj["Resume"] = "";
    
    console.log(url);
    
    ajaxCall("POST", url, applyRequestObj, function (status, body) {

        if (status == 200) {

          alert("Job successfully applied");

        }

        else if (status == 403)
        {

            alert("Job application failed");
        }
    });
}


function interested(e) {


    e.preventDefault();
    var jobID;
    jobID = localStorage.getItem("jobId");
    
    if(checkStatus(jobID))
     { 
    	var applyRequestObj = {};
    
    	applyRequestObj["applicationType"] = "interested";
        applyRequestObj["Resume"] = "";
    
    var url = "/jobs/view/" + jobID + "/apply";
    
    console.log(url);
    
    
    
    
    
    
    ajaxCall("POST", url, applyRequestObj, function (status, body) {
        if (status == 200) {
            alert("Job successfully mark interested");
        }
        else if (status == 403)
        {
            alert("Failed to mark the job as interested");
        }
    });
     }
    
    else
    	{
    	alert("Try again!!!");
    	}
}


function checkStatus(jobReq) {
	
	 var checkStatus = "user/"+jobReq+"/getApplicationStatus";
	  var checkStatusRequestObj = {};
	    
	    ajaxCall("GET", checkStatus, checkStatusRequestObj, function (status, body) {
	        if (status == 200) {
	            
	        	var checkStatusResponseObj = JSON.parse(body);
	        	//document.getElementById("Button").disabled = true;
	        	if(checkStatusResponseObj["type"] == "APPLIED")
	        	{
	        		document.getElementById("applybtn").disabled = true;
	        		document.getElementById('applybtn').innerHTML = "Applied"
	        		document.getElementById("markinterestedbtn").disabled = true;
	        		document.getElementById('applybtn').innerHTML = "Interested"
	        		
	        	}
	        	else if (checkStatusResponseObj["type"] == "INTERESTED")
	        	{
	        		document.getElementById('applybtn').innerHTML = "Interested"
	        		document.getElementById("applybtn").disabled = false;
	        	}
	        	else if(checkStatusResponseObj["type"] == "null")
	        	{
	        		
	            		document.getElementById("markinterestedbtn").disabled = false;
	            		document.getElementById("applybtn").disabled = false;
	            	
	        	}
	        	
	            
	        }
	        else if (status == 403)
	        {
	            alert("Unable to get the status!!");
	            return false;
	        }
	    });
	    
	    return true;
}


function editProfileEvent(e)
{
	
    e.preventDefault();
    
    alert("In edit Profile");

    var editProfile = e.target;
    var inputArray = editProfile.getElementsByTagName("input");
var searchRequestObj = {};
    for (var i = 0; i < inputArray.length; i++) {
        var input = inputArray[i];

        if (input.getAttribute("name") === "fName") {
            searchRequestObj["First Name"] = input.value;
        }

        if (input.getAttribute("name") === "lName") {
            searchRequestObj["Last Name"] = input.value;
        }

        if (input.getAttribute("name") === "selfIntro") {
            searchRequestObj["Self-introduction"] = input.value;
        }

        if (input.getAttribute("name") === "workEx") {
            searchRequestObj["Work Experience"] = input.value;
        }

        if (input.getAttribute("name") === "education") {
            searchRequestObj["Education"] = input.value;
        }

        if (input.getAttribute("name") === "skills") {
            searchRequestObj["Skills"] = input.value;
        }

    }

    var url = "/userprofile/create";

    ajaxCall("POST", url, searchRequestObj, function (status, body) {

        if (status == 201) {
            alert("Profile is successfully created");
            window.location.href = "/Dashboard.html";
        }

        else if (status == 200) {
            alert("Profile is successfully updated");
            window.location.href = "/Dashboard.html";
        }

});

}






