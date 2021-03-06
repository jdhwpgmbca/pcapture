    // Rather than hard-coding the a <script/> tag with keycloak.js location, I'm
    // dynamically building a <script/> tag, using information I'm fetching
    // from /api/res/configjson.
    var keycloak = null;
    var timer = null;
    var autoRefresh = null;
    fetch('/api/res/configjson')
    .then( response => response.json() )
    .then( data => {
        var authurl = data['auth-server-url'];
        var script = document.createElement('script');
        script.onload = () => {
            keycloak = new Keycloak('/api/res/configjson');
            keycloak.init({onLoad: 'login-required'})
            .then( () => {
                let username = keycloak.idTokenParsed.preferred_username;
                console.log('User ' + username + ' is now authenticated.');
                let usernameDiv = document.getElementById('username');
                usernameDiv.innerHTML = 'Logged in as: ' + username;
                updateAllUsersButton();
                timer = setInterval( () => listCaptures(), 2000 );
                autoRefresh = true;
                generateStartButtons();
            })
            .catch( () => {
                // window.location.reload();
            });
            keycloak.updateToken(30)
            .then( refreshed => {
                if (refreshed) {
                    console.log("Token refreshed");
                }
            })
            .catch( () => {
                console.log('Failed to refresh token, or the session has expired.');
            } );
        };
        script.src = authurl + '/js/keycloak.js';
        // This next line loads the dynamically created script tag that's used to 
        // load Keycloak into the DOM - which causes it's onload method to run.
        document.head.appendChild(script);
    } )
    .catch( error => console.log(error) );
    function logout() {
        keycloak.logout();
    }
    function generateTableHead(table, data) {
        let thead = table.createTHead();
        let row = thead.insertRow();
        if (!data[0]) return;
        for (let key of Object.keys(data[0])) {

            let th = document.createElement('th');
            let text = document.createTextNode(key);
            th.appendChild(text);
            row.appendChild(th);
        }
        let th = document.createElement('th');
        th.setAttribute('colspan',"3");
        let text = document.createTextNode('Controls');
        th.appendChild(text);
        row.appendChild(th);
    }
    function generateTable(table, data) {
        for (let element of data) {
            let row = table.insertRow();
            let status = null;
            for (key in element) {
                let cell = row.insertCell();
                if (key === 'creationTime' || key == 'lastModifiedTime') {
                    let date = new Date(element[key]);
                    var options = { weekday: undefined, year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric' };
                    let text = document.createTextNode( date.toLocaleDateString() + ' ' + date.toLocaleTimeString() );
                    cell.appendChild(text);
                } else if (key === 'length') {
                    let div = document.createElement('div');
                    let text = document.createTextNode(element[key]);
                    div.appendChild(text);
                    cell.appendChild(div);
                } else if (key === 'status') {
                    status = element[key];
                    let text = document.createTextNode(element[key]);
                    cell.appendChild(text);
                } else {
                    let text = document.createTextNode(element[key]);
                    cell.appendChild(text);
                }
            }

            let id = element['id'];

            let downloadCell = row.insertCell();
            let downloadBtn = document.createElement('button');
            downloadBtn.setAttribute("type", "button") ;
            downloadBtn.innerHTML = "Download";
            downloadBtn.setAttribute( "onclick", "downloadCapture('" + id + "')"); 
            downloadCell.appendChild(downloadBtn);

            let stopCell = row.insertCell();
            let stopBtn = document.createElement('button');
            stopBtn.setAttribute('type','button');
            stopBtn.innerHTML = "Stop";
            stopBtn.setAttribute( "onclick", "stopCapture('" + id + "')"); 
            stopCell.appendChild(stopBtn);

            if (status === 'running') {
                stopBtn.disabled = false;
            } else {
                stopBtn.disabled = true;
            }

            let deleteCell = row.insertCell();
            let deleteBtn = document.createElement('button');
            deleteBtn.setAttribute('type','button');
            deleteBtn.innerHTML = "Delete";
            deleteBtn.setAttribute('onclick',"deleteCapture('" + id + "')");
            deleteCell.appendChild(deleteBtn);

            if (status === 'running') {
                deleteBtn.disabled = true;
            } else {
                deleteBtn.disabled = false;
            }
        }
    }
    // Load start buttons from API.
    function generateStartButtons() {
        axios({
            method: 'get',
            url: '/api/filter',
            headers: {
                'Authorization': 'Bearer ' + keycloak.token
            }
        })
        .then( response => {
            var startButtonsParent = document.getElementById('startButtonsParent');
            response.data.forEach(element => {
                var suffix = element.urlSuffix;
                var label = element.label;
                var btn = document.createElement('button');
                btn.innerHTML = label;
                btn.setAttribute('onclick',"startCapture('" + suffix + "')");
                startButtonsParent.appendChild(btn);
            });
        } )
        .except( error => console.log(error) );
    }
    function toggleAllUsers() {
        axios({
            method: 'put',
            url: '/api/user/toggleAllUsers',
            headers: {
                'Authorization': 'Bearer ' + keycloak.token
            }
        })
        .then( response => {
            console.log('All Users preference toggled for user', keycloak.idTokenParsed.preferred_username );
        } )
        .catch( error => {
            console.log( error );
        } );
    }
    function refreshClick() {
        var checkbox = document.getElementById('refreshCheckbox');
        var refreshButton = document.getElementById('refreshButton');

        if (checkbox.checked == true) {
            timer = setInterval( () => listCaptures(), 2000 );
            autoRefresh = true;
            refreshButton.hidden = true;
        } else {
            clearInterval(timer);
            autoRefresh = false;
            refreshButton.hidden = false;
        }
    }
    function updateAllUsersButton() {
        axios({
            method: 'get',
            url: '/api/user/isAdmin',
            headers: {
                'Authorization': 'Bearer ' + keycloak.token
            }
        })
        .then( response => {
            let allUsersButton = document.getElementById('allUsersButton');
            allUsersButton.hidden = !response.data;
        } )
        .catch( error => {
            console.log( error );
        } );
    }
    function listCaptures() {
        axios({
            method: 'get',
            url: '/api/capture',
            headers: {
                'Authorization': 'Bearer ' + keycloak.token
            }
        })
        .then( response => {
            const mytable = document.querySelector('table');
            mytable.innerHTML = '';
            generateTableHead(mytable,response.data);
            generateTable(mytable,response.data);
        })
        .catch( error => {
            if (error.response) {
                console.log('Refreshing authorization token');
                keycloak.updateToken(5).then( () => {
                    console.log('Token refreshed');
                }).catch( () => {
                    console.log('Failed to refresh authorization token, reloading window.');
                    window.location.reload();
                });
            } else if (error.request) {
                console.log('No response received ', error.request);
            } else {
                console.log('Other error', error);
            }
        });
}
function startCapture(type) {
    axios({
        method: 'post',
        url: '/api/capture/' + type,
        headers: {
            'Authorization': 'Bearer ' + keycloak.token
        }
    })
    .then( response => {
        console.log("Start", response.config.method, response.config.url, response.data);
        if( autoRefresh == false ) listCaptures();
    })
    .catch( error => console.log( error ) );
}
function stopCapture(id) {
    axios({
        method: 'put',
        url: "/api/capture/" + id,
        headers: {
            'Authorization': 'Bearer ' + keycloak.token
        }
    })
    .then( response => {
        console.log('Stop', response.config.method, response.config.url);
        if( autoRefresh == false ) listCaptures();
    })
    .catch( error => console.log(error) )
}
function downloadCapture(id) {
    axios({
        method: 'get',
        url: '/api/capture/' + id,
        headers: {
            'Authorization': 'Bearer ' + keycloak.token,
            'Accept': 'application/octet-stream'
        },
        responseType: 'blob'
    })
    .then( response => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download','capture.pcapng');
        document.body.appendChild(link);
        link.click();
        console.log('Download', response.config.method, response.config.url);
    })
    .catch( error => console.log( error ) );
}
function deleteCapture(id) {
    axios({
        method: 'delete',
        url: '/api/capture/' + id,
        headers: {
            'Authorization': 'Bearer ' + keycloak.token
        }
    })
    .then( response => {
        console.log('Delete', response.config.method, response.config.url);
        if( autoRefresh == false ) listCaptures();
    })
    .catch( error => console.log(error) )
}
