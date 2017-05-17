/**
 * Created by Ramkumar on 5/14/2017.
 */

var searchRequestObj = {};

function ajaxCall(method, url, data, callback) {

    console.log("In ajax call");
    console.log(JSON.stringify(data));
    var httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = handleResponse;
    httpRequest.open(method, url);
    
    httpRequest.setRequestHeader("content-type",
                "application/json");
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

    for (var i = 0; i < inputArray.length; i++) {
        var input = inputArray[i];

        if (input.getAttribute("name") === "FreeTextSearch") {
            searchRequestObj["FreeTextSearch"] = input.value;
        }

        if (input.getAttribute("name") === "SearchByCompany") {
            searchRequestObj["SearchByCompany"] = input.value;
        }

        if (input.getAttribute("name") === "SearchByLocation") {
            searchRequestObj["SearchByLocation"] = input.value;
        }

        if (input.getAttribute("name") === "SearchBySalary") {
            searchRequestObj["SearchBySalary"] = input.value;
        }

    }

    var url = "/jobs/search";

    var logoutRequestObj = {};

    ajaxCall("GET", url, searchRequestObj, function (status, body) {
        if (status == 200) {

           var jobResponseObj = JSON.parse(body);

        } else {
            console.log("Search not done : " + status);
        }
    });

}
