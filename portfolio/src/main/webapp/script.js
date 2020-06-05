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

function getLoginStatus() {
    fetch('/login-status').then(response => response.json()).then(loginStatus => {
        const loginMessageContainer = document.getElementById("login-message-container");
        if(loginStatus[0]) {
            console.log('User is logged in.');
            getProfile();
            loginMessageContainer.innerHTML = '<p>Logout <a href=\"' + loginStatus[1] + '\">here</a>.</p>';
        }
        else {
            console.log('User is not logged in.');
            hideProfile();
            loginMessageContainer.innerHTML = '<br><p>Login <a href=\"' + loginStatus[1] + '\">here</a> to post comments and vote on what should be my next project!</p>';
        }
    });
}

function getProfile() {
    fetch('/user-info').then(response => response.json()).then(userInfo => {
        // user must have set a name to access the profile
        if(userInfo.firstName.localeCompare("") == 0 && userInfo.lastName.localeCompare("") == 0) {
            hideProfile();   
            document.getElementById("login-message-container").innerHTML = '<p>Set a name <a href=/name.html>here</a> to access profile information.</p>';     
            console.log('User is logged in but has not set a name - did not display user profile.');
        }
        else {
            // display the profile
            document.getElementById("comments-form-container").style.display = "block";
            document.getElementById("user-info-container").style.display = "block";

            getVotingForm(userInfo.canVote);
            getUserInfo(userInfo);
            console.log('Displayed the user profile.');
        }
    });
}

function hideProfile() {
    document.getElementById("comments-form-container").style.display = "none";
    document.getElementById("vote-container").style.display = "none";
    document.getElementById("user-info-container").style.display = "none";
    console.log('Hid the user profile.');
}

function getComments() {
    // retrieve limit number of comments from the server
    var limit = document.getElementById("limit").value;

    fetch('/comments?limit=' + limit).then(response => response.json()).then(commentsHistory => {
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

        const chart = new google.visualization.ColumnChart(document.getElementById('next-project-container'));
        chart.draw(data, options);
    });

    console.log("Drew next project graph.");
}

function getVotingForm(canVote) {
    if(canVote) {
        document.getElementById("vote-container").style.display = "block";
        console.log('Displayed voting form.');
    }
    else {
        document.getElementById("vote-container").style.display = "none";
        console.log('Hid voting form.');
    }
}

function getUserInfo(userInfo) {
    const userInfoContainer = document.getElementById('user-info-container');
    userInfoContainer.innerHTML = '';

    if(!userInfo.canVote) {
        userInfoContainer.innerHTML += '<h4>Thanks for voting!</h4>';
    }
    userInfoContainer.innerHTML += '<br>'
    if(userInfo.firstName.localeCompare("") != 0) {
        userInfoContainer.innerHTML += '<h2>Hi ' + userInfo.firstName + '!</h2>';
    }
    else if(userInfo.lastName.localeCompare("") != 0) {
        userInfoContainer.innerHTML += '<h2>Hi ' + userInfo.lastName + '!</h2>';
    }
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