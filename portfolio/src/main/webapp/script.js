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

// gets the comments from the java servlet
function getComments() {
    fetch('/data?limit=5').then(response => response.json()).then(commentsHistory => {
        const commentsEltList = document.getElementById('comments-container');
        commentsEltList.innerHTML = '';
        
        commentsHistory.forEach(comment => {
            commentsEltList.appendChild(createListElement(comment));
        });

        console.log('Added comments.');
    });
}

// creates an <li> element containing text
function createListElement(text) {
    const liElt = document.createElement('li');
    liElt.innerText = text;
    return liElt;
}