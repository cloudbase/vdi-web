var vdiClient = {};
vdiClient.session = {};

vdiClient.login = function(username, password, project, callbackSuccess){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            vdiClient.loginSuccesful(request.getResponseHeader("X-Subject-Token"));
            if(callbackSuccess != undefined)callbackSuccess();
        } else { 
            vdiClient.loginFailed();
        }
    }
    request.open("POST", "auth/login", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.username = username;
    jsonBody.password = password;
    jsonBody.project = project;
    request.send(JSON.stringify(jsonBody));
}

vdiClient.logout = function(){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            //vdiClient.logoutSuccesful();
        } else { 
            //vdiClient.logoutFailed();
        }
    }
    request.open("GET", "auth/logout", true);
    request.setRequestHeader("X-Auth-Token", vdiClient.session.token);
    request.send();
    vdiClient.logoutSuccesful();
}

vdiClient.loginSuccesful = function(token){
    vdiClient.session.token = token;
    vdiClient.session.logged = true;
    vdiClient.session.pools = [];
}

vdiClient.logoutSuccesful = function(token){
    vdiClient.session = {};
}

vdiClient.loginFailed = function(){
    console.log("TBI login failed");
}

vdiClient.getPools = function(){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            vdiClient.getPoolsSuccesful(JSON.parse(request.responseText).pools);
        } else {
            //vdiClient.getPoolsFailed();
        }
    }
    request.open("GET", "app/pools", true);
    request.setRequestHeader("X-Auth-Token", vdiClient.session.token);
    request.send();
}

vdiClient.getPoolsSuccesful = function(pools){
    for(i=0;i<pools.length;i++){
        if(vdiClient.session.pools[pools[i].id] == undefined){
            vdiClient.session.pools[pools[i].id] = {};
        }
        vdiClient.session.pools[pools[i].id].id = pools[i].id;
        vdiClient.session.pools[pools[i].id].name = pools[i].name;
        vdiClient.session.pools[pools[i].id].description = pools[i].description;
    }
}

vdiClient.getSessions = function(){
    for(var id in vdiClient.session.pools){
        vdiClient.getSessionsForPool(id);
    }
}

vdiClient.getSessionsForPool = function(pool_id){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            vdiClient.getSessionsForPoolSuccesful(pool_id, JSON.parse(request.responseText).sessions);
        } else {
            //vdiClient.getSessionsForPool();
        }
    }
    request.open("GET", "app/session", true);
    request.setRequestHeader("X-Auth-Token", vdiClient.session.token);
    request.setRequestHeader("Pool-Id", pool_id);
    request.send();
}

vdiClient.getSessionsForPoolSuccesful = function(pool_id, sessions){
    vdiClient.session.pools[pool_id].sessions = sessions;
}

vdiClient.createSession = function(pool_id){
    var request = new XMLHttpRequest();
    request.onload = function(){
        //retry
        if(request.status == 449){
            console.log("Retry!");
        } else if (request.status == 200){
            vdiClient.getSessions();
            vdiClient.session.last_session = {};
            vdiClient.session.last_session.token = vdiClient.session.token;
            vdiClient.session.last_session.pool_id = pool_id;
            vdiClient.session.last_session.session_id = JSON.parse(request.responseText).session_id;
        } else {
        }
    }
    request.open("POST", "app/session", true);
    request.setRequestHeader("X-Auth-Token", vdiClient.session.token);
    request.setRequestHeader("Pool-Id", pool_id);
    request.send();
}

//GUACAMOLE CLIENT CODE
vdiClient.connectTunnel = function(connection_info){
    if(connection_info == undefined)return;
    guac.connect(JSON.stringify(connection_info));
}

// Instantiate client, using an HTTP tunnel for communications.
var guac = new Guacamole.Client(
    new Guacamole.HTTPTunnel("tunnel")
);


// Error handler
guac.onerror = function(error) {
    alert(error);
};

// Get display div from document
var display = null;

vdiClient.init = function(){
    display = document.getElementById("display");

    // Add client to display div
    display.appendChild(guac.getDisplay().getElement());
}

// Disconnect on close
window.onunload = function() {
    guac.disconnect();
}

// Mouse
var mouse = new Guacamole.Mouse(guac.getDisplay().getElement());

mouse.onmousedown = 
mouse.onmouseup   =
mouse.onmousemove = function(mouseState) {
    guac.sendMouseState(mouseState);
};

// Keyboard
var keyboard = new Guacamole.Keyboard(document);
