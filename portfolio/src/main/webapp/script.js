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
google.charts.setOnLoadCallback(drawTimeline);

function drawTimeline() {
    const data = new google.visualization.DataTable();

    data.addColumn({type: 'string', id: 'Location'});
    data.addColumn({type: 'date', id: 'Start'});
    data.addColumn({type: 'date', id: 'End'});
    data.addRows([
      ['Singapore', new Date(2000, 9), new Date(2000, 10)],
      ['Katy, TX', new Date(2000, 10), new Date(2019, 7)],
      ['New Haven, CT', new Date(2019,7), new Date()]]);

    const chart = new google.visualization.Timeline(document.getElementById('location-timeline'));
    chart.draw(data);

    console.log("Drew timeline.");
}

function drawNextProject() {
    fetch('/next-project').then(response => response.json()).then(projectVotes => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Project');
        data.addColumn('number', 'Votes');
        Object.keys(projectVotes).forEach(project => {
            data.addRow([project, projectVotes[project]]);
        });

        const options = {
            'title': 'Votes',
        };

        const chart = new google.visualization.ColumnChart(document.getElementById('next-project'));
        chart.draw(data, options);
    });

    console.log("Drew next project graph.");
}

function getComments() {
    // only display the comments section if the user is logged in
    fetch('/user-info').then(response => response.json()).then(userInfo => {
        console.log('Got user info.');
        
        const commentsSection = document.getElementById("comments-section");
        const userInfoContainer = document.getElementById('user-info-container');

        if(userInfo[0].localeCompare("logged-in") == 0) { 
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

                commentsSection.style.display = "block";

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
                console.log('User logged in - displayed comments.');
            });

            drawNextProject();
        }
        else {
            commentsSection.style.display = "none";
            userInfoContainer.innerHTML = '<p>Login <a href=\"' + userInfo[1] + '\">here</a> to view and post comments.</p>';
            console.log('User not logged in - did not display comments.');
        }
    });
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