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

function getComments() {
    // only display the comments section if the user is logged in
    fetch('/user-info').then(response => response.json()).then(loginStatus => {
        console.log('Got login status.');
        
        const commentsSection = document.getElementById("comments-section");
        const userInfoContainer = document.getElementById('user-info-container');

        if(loginStatus[0].localeCompare("logged-in") == 0) { 
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
                userInfoContainer.innerHTML = '<p>Logout <a href=\"' + loginStatus[1] + '\">here</a>.</p>';
                console.log('User logged in - displayed comments.');
            });
        }
        else {
            commentsSection.style.display = "none";
            userInfoContainer.innerHTML = '<p>Login <a href=\"' + loginStatus[1] + '\">here</a> to view and post comments.</p>';
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