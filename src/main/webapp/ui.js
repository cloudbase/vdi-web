var ready = function(){
    vdiClient.init();
    document.getElementById("login_button").onclick = doLogIn; 
    document.getElementById("logout_button").onclick = doLogOut; 
    document.getElementById("close_session").onclick = closeTunnel; 
    document.getElementById("logged").style.visibility='hidden';
    document.getElementById("session").style.visibility='hidden';
    cycle();
}
var cycle = function(){
    app.getList();
    app.listApps();
    setTimeout(cycle,1000);
}
var app = {};
app.getList = function(){
    if(!vdiClient.session.token)return;
    vdiClient.getPools();
    vdiClient.getSessions();
    app.populateList();
}

app.clearList = function(){
    document.getElementById("application_list").innerHTML="";
}

app.populateList = function(){
    var appParent = document.getElementById("application_list");
    appParent.innerHTML = "";
    for(var pool_id in vdiClient.session.pools){
        var desktopID = app.hasDesktop(pool_id);
        var div_button = document.createElement('div');
        div_button.id = pool_id;
        if(desktopID == null){
            div_button.className = "app_button";
        } else {
            div_button.className = "desktop_button";
        }
        div_button.title = "Create a new '" + vdiClient.session.pools[pool_id].name + "' application; " + vdiClient.session.pools[pool_id].description;
        div_button.onclick = createApplication;
        appParent.appendChild(div_button);

        var div_icon = document.createElement('img');
        div_icon.className = "app_button_icon";
        div_icon.id = pool_id;
        if(desktopID == null){
            div_icon.src = "img/computer-icon.png";
        } else {
            div_icon.src = "img/eye-icon.png";
        }
        div_button.appendChild(div_icon);

        var div_name = document.createElement('div');
        div_name.className = "app_button_name";
        div_name.id = pool_id;
        div_name.innerHTML = vdiClient.session.pools[pool_id].name;
        div_button.appendChild(div_name);
    }
}

app.listApps = function(){
    if(app.loginStatus != "logged")return;
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            app.desktops = JSON.parse(request.responseText).desktops;
        } 
    }
    request.open("POST", "data/desktop", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.session_id = app.id;
    request.send(JSON.stringify(jsonBody));
}

app.hasDesktop = function(pool_id){
    if(vdiClient.session.pools[pool_id].sessions)return vdiClient.session.pools[pool_id].sessions[0];
    return null;
}

var createApplication = function(){
    var appID = event.srcElement.id;
    var desktopID = app.hasDesktop(appID);
    if(desktopID == null){
        vdiClient.createSession(appID);
    } else {
        openTunnel(appID, desktopID);
    }
}
var openTunnel = function(pool_id, session_id){
    document.getElementById("logged").style.visibility='hidden';
    document.getElementById("logged").style.position='absolute';
    document.getElementById("login").style.visibility='hidden';
    document.getElementById("session").style.visibility='visible';
    app.loginStatus = "session";
    keyboard.onkeydown = function (keysym) {
        guac.sendKeyEvent(1, keysym);
    };

    keyboard.onkeyup = function (keysym) {
        guac.sendKeyEvent(0, keysym);
    };
    var jsonBody = {};
    jsonBody.token = vdiClient.session.token;
    jsonBody.session_id = session_id;
    jsonBody.pool_id = pool_id;
    vdiClient.connectTunnel(jsonBody);
}
var closeTunnel = function(){
    document.getElementById("session").style.visibility='hidden';
    document.getElementById("logged").style.visibility='visible';
    document.getElementById("logged").style.position='initial';
    delete keyboard.onkeydown;
    delete keyboard.onkeyup;
    guac.disconnect();
}


var doLogIn = function(){
    var username = document.getElementById("username_field").value;
    var password = document.getElementById("password_field").value;
    var project = document.getElementById("project_field").value;
    vdiClient.login(username, password, project, function(){
        document.getElementById("login").style.visibility='hidden';
        document.getElementById("logged").style.visibility='visible';
        document.getElementById("logged_username").innerHTML="User: " + username;
        setLogMsg("Logged in as " + username);
    });
}
var doLogOut = function(){
    closeTunnel();
    document.getElementById("password_field").value = "";
    document.getElementById("login").style.visibility='visible';
    document.getElementById("logged").style.visibility='hidden';
    vdiClient.logout();
    app.clearList();
}

var setLogMsg = function(msg){
    document.getElementById("log_field").innerHTML = msg;
    setTimeout(function(){clearLogMsg();},3000);
}
var clearLogMsg = function(msg){
    document.getElementById("log_field").innerHTML = "";
}
