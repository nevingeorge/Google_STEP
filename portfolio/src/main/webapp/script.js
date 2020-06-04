// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

google.charts.load("current", {'packages':["timeline", "corechart"]});

function drawTimeline() {
    const data = new google.visualization.DataTable();

    data.addColumn({type: 'string', id: 'Location'});
    data.addColumn({type: 'date', id: 'Start'});
    data.addColumn({type: 'date', id: 'End'});
    data.addRows([
      ['Singapore', new Date(2000, 9), new Date(2000, 10)],
      ['Katy, TX', new Date(2000, 10), new Date(2019, 7)],
      ['New Haven, CT', new Date(2019,7), new Date()]
    ]);

    const chart = new google.visualization.Timeline(document.getElementById('location-timeline'));
    chart.draw(data);

    console.log("Drew timeline.");
}

function drawNextProject() {
    fetch('/next-project').then(response => response.json()).then(projectVotes => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Project');
        data.addColumn('number', 'Votes');
        data.addColumn({role: 'style'});
        data.addRow(["Website", projectVotes[0], 'rgb(50, 98, 209)']);
        data.addRow(["iOS App", projectVotes[1], 'rgb(202, 45, 45)']);
        data.addRow(["Machine Learning", projectVotes[2], 'rgb(236, 201, 47)']);

        const options = {
            title: 'Votes',
            legend: {position: "none"},
        };

        const chart = new google.visualization.ColumnChart(document.getElementById('next-project'));
        chart.draw(data, options);
    });

    console.log("Drew next project graph.");
}

function getProfile() {
    fetch('/user-info').then(response => response.json()).then(userInfo => {
        const userProfileContainer = document.getElementById("user-profile-container");
        const loginMessageContainer = document.getElementById("login-message-container");

        // only display the user profile section if the user is logged in
        if(userInfo[0].localeCompare("logged-in") == 0) { 
            console.log('User is logged in.');
            userProfileContainer.style.display = "block";
            loginMessageContainer.innerHTML = '';

            getComments();
            drawNextProject();
            getUserInfo(userInfo);
        }
        else {
            console.log('User is not logged in.');
            userProfileContainer.style.display = "none";
            loginMessageContainer.innerHTML = '<p>Login <a href=\"' + userInfo[1] + '\">here</a> to view and post comments and vote on what should be my next project!</p>';
        }
    });
}

function getComments() {
    // retrieve limit number of comments from the server
    var limit = document.getElementById("limit").value;

    fetch('/data?limit=' + limit).then(response => response.json()).then(commentsHistory => {
        const commentsEltList = document.getElementById('comments-container');

        if(Object.keys(commentsHistory).length == 0) {
            commentsEltList.innerHTML = 'Be the first to leave a comment!';
        }
        else {
            commentsEltList.innerHTML = '';
            commentsHistory.forEach(commentInfo => {
                commentsEltList.appendChild(createListElement(commentInfo[1] + " " + commentInfo[2] + ": " + commentInfo[0]));
            });
        }
    });
    console.log('Got comments.');
}

function getUserInfo(userInfo) {
    const userInfoContainer = document.getElementById('user-info-container');

    var firstName = userInfo[2];
    var lastName = userInfo[3];
    userInfoContainer.innerHTML = '';
    if(firstName.localeCompare("") != 0) {
        userInfoContainer.innerHTML += '<h2>Hi ' + firstName + '!</h2>';
    }
    else if(lastName.localeCompare("") != 0) {
        userInfoContainer.innerHTML += '<h2>Hi ' + lastName + '!</h2>';
    }
    userInfoContainer.innerHTML += '<p>Logout <a href=\"' + userInfo[1] + '\">here</a>.</p>';
    console.log('Got user info.');
}

// creates an <li> element containing text
function createListElement(text) {
    const liElt = document.createElement('li');
    liElt.innerText = text;
    return liElt;
}

async function deleteComments() {
    await fetch("/delete-data", {method: 'POST'});
    console.log('Deleted comments.');

    getComments();
}