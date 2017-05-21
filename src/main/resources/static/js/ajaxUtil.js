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
    
    alert(url);
    
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
	
	
	
	for (var i = 0; i < searchResponseObj.length; i++) {

        var jobid = searchResponseObj[i]["jobid"];
        var jobtitle = searchResponseObj[i]["jobtitle"];
        var skill = searchResponseObj[i]["skill"];
        var description = searchResponseObj[i]["description"];
        var location = searchResponseObj[i]["location"];
        var salary = searchResponseObj[i]["salary"];
         var company = searchResponseObj[i]["company"];



         
        var searchList = '<br><br><p></p><div class="main-login main-center">' +
        					'<div class="row">'+
        	'<a href="#" id = "jobTitleId" onclick = "redirectjobviewPage(' +jobid +')">'+ (i+1) + ") " +
            jobtitle+"-"+jobid + '</a></div></div>';
    
        
        	$(searchList).appendTo("#searchJobResults").ready(function(){
        	
        		var jobid = document.getElementById("jobTitleId");

                
        });    
    }
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
            $('p#company').text("Company : "+JSON.stringify(company));
            
        } 
    });
    

    
   

}


function apply(e) {

	alert("IN apply function ");
    e.preventDefault();
    var jobID;
/*    var jobDescriptionForm = e.target;
    var inputArray = jobDescriptionForm.getElementsByTagName("p");

    for (var i = 0; i < inputArray.length; i++) {

        if (input.getAttribute("name") === "jobid") {
            jobID = input.value;
        }

    }*/

    var applyRequestObj = {};
    
    jobID = localStorage.getItem("jobId");
    
    var url = "/jobs/view/" + jobID + "/apply";
    
    applyRequestObj["applicationType "] = "applied";
    console.log(url);
    
    ajaxCall("GET", url, applyRequestObj, function (status, body) {

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
    //var jobDescriptionForm = e.target;
    /*var inputArray = jobDescriptionForm.getElementsByTagName("p");

    for (var i = 0; i < inputArray.length; i++) {

        if (input.getAttribute("name") === "jobid") {
            jobID = input.value;
        }

    }*/

    var applyRequestObj = {};
    applyRequestObj["applicationType "] = "interested";
    jobID = localStorage.getItem("jobId");
    
    var url = "/jobs/view/" + jobID + "/apply";
    
    console.log(url);
    
    
    ajaxCall("GET", url, applyRequestObj, function (status, body) {

        if (status == 200) {

            alert("Job successfully mark interested");

        }

        else if (status == 403)
        {

            alert("Failed to mark the job as interested");
        }
    });
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






