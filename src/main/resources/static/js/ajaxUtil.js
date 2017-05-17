/**
 * Created by Ramkumar on 5/14/2017.
 */



function ajaxCall(method, url, data, callback) {

    console.log("In ajax call");
    console.log(JSON.stringify(data));
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
                SearchBySalary = 0;
            else
                SearchBySalary = input.value;
        }

    }

    var url = "/jobs/search/"+FreeTextSearch+"/"+SearchByCompany+"/"+SearchByLocation+"/"+SearchBySalary;

    var searchRequestObj = {};
    ajaxCall("GET", url, searchRequestObj, function (status, body) {
        if (status == 200) {

            var searchResponseObj = JSON.parse(body);

            for (var i = 0; i < searchResponseObj.length; i++) {

                var jobid = searchResponseObj["jobid"];
                var jobtitle = searchResponseObj["jobtitle"];
                var skill = searchResponseObj["skill"];
                var description = searchResponseObj["description"];
                var location = searchResponseObj["location"];
                var salary = searchResponseObj["salary"];
                var status = searchResponseObj["status"];
                var company = searchResponseObj["company"];


                var searchList = '<div style="display: flex;margin-bottom: 20px;padding: 20px;position:' +
                    ' relative" class="boxShadowSmall"> <a href="#" id = "jobTitleId"> ' +
                    jobtitle + ' </a></div> ' +
                    '<div style="display: flex;margin-bottom: 20px;padding: 20px;position:' +
                    ' relative" class="boxShadowSmall">' +
                    skill + '</div>' +
                '<div style="display: flex;margin-bottom: 20px;padding: 20px;position:' +
                'relative" class="boxShadowSmall">' +
                description + '</div>' +
                '<div style="display: flex;margin-bottom: 20px;padding: 20px;position:' +
                'relative" class="boxShadowSmall">' +
                location + '</div>' +
                '<div style="display: flex;margin-bottom: 20px;padding: 20px;position:' +
                'relative" class="boxShadowSmall">' +
                salary + '</div>';


             
                
                $(searchList).appendTo("#searchJobResults");
                
                /* var jobtitleClick = document.getElementById(jobTitleId);
                jobtitleClick.onclick = viewJob(jobid);*/
            }
        }
        else {
            console.log("Search not done : " + status);
        }

    });

}


function viewJob(jobid) {
    localStorage.setItem("jobId",jobid);
    window.location = "JobView.html";
}


//load view of a particular job
function loadjobviewPage()
{


    var jobId = localStorage.getItem("jobId");

    var url = "/jobs/view/"+jobId;
    var jobViewRequestObj = {};
    ajaxCall("GET", url , jobViewRequestObj, function (status, body) {

        if (status == 200) {

            var responseObj = JSON.parse(body);

            var jobid = responseObj["jobid"];
            var jobtitle = responseObj["jobtitle"];
            var skill = responseObj["skill"];
            var description = responseObj["description"];
            var location = responseObj["location"];
            var salary = responseObj["salary"];
            var status = responseObj["status"];
            var company = responseObj["company"];

            if (jobid!==undefined && jobid!=null) {
                window.location.href = "viewJob.html?jobid="+jobid+"&jobtitle=" + jobtitle+"&skill="+skill+"&description="+description+"&location="
                +location+"&salary="+salary;
                return;
            }
        } else {
            alert("In loadjobviewPage()");

            window.location.href = "Dashboard.html";
        }
    });

}


function apply(e) {


    e.preventDefault();
    var jobID;
    var jobDescriptionForm = e.target;
    var inputArray = jobDescriptionForm.getElementsByTagName("p");

    for (var i = 0; i < inputArray.length; i++) {

        if (input.getAttribute("name") === "jobid") {
            jobID = input.value;
        }

    }

    var applyRequestObj = {};
    var url = "/jobs/view/" + jobID + "/apply";
    applyRequestObj["applicationType "] = "applied";

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
    var jobDescriptionForm = e.target;
    var inputArray = jobDescriptionForm.getElementsByTagName("p");

    for (var i = 0; i < inputArray.length; i++) {

        if (input.getAttribute("name") === "jobid") {
            jobID = input.value;
        }

    }

    var applyRequestObj = {};
    applyRequestObj["applicationType "] = "interested";
    var url = "/jobs/view/" + jobID + "/apply";
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


function editProfile(e)
{
    e.preventDefault();
    alert("In edit Profile");

    e.preventDefault();

    var editProfile = e.target;
    var inputArray = editProfile.getElementsByTagName("input");

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






