var ready = function(){
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
    setTimeout(function(){cycle();},1000);
}
var app = {};
app.desktops = [];
app.loginStatus = "disconnected";
app.login = function(username, password){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            app.id = JSON.parse(request.responseText).session_id;
            app.loginStatus = "logged";
            app.username = username;
            setLogMsg("Logged in as " + username);
            document.getElementById("login").style.visibility='hidden';
            document.getElementById("logged").style.visibility='visible';
            document.getElementById("logged_username").innerHTML="User: " + username;
            app.getList();
        } else { 
            app.loginStatus = "error";
            setLogMsg("Wrong credentials!");
        }
    }
    request.open("POST", "auth/login", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.username = username;
    jsonBody.password = password;
    request.send(JSON.stringify(jsonBody));
}
app.logout = function(){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            app.loginStatus = "disconnected";
        } 
    }
    request.open("POST", "auth/logout", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.session_id = app.id;
    request.send(JSON.stringify(jsonBody));
}
app.getList = function(){
    if(app.loginStatus != "logged")return;
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            app.list = JSON.parse(request.responseText);
            app.populateList();
        } 
    }
    request.open("POST", "data/list", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.session_id = app.id;
    request.send(JSON.stringify(jsonBody));
}
app.clearList = function(){
    document.getElementById("application_list").innerHTML="";
}

app.populateList = function(){
    var appParent = document.getElementById("application_list");
    appParent.innerHTML = "";
    for(var i=0;i<app.list.apps.length;i++){
        var desktopID = app.hasDesktop(app.list.apps[i].id);
        var div_button = document.createElement('div');
        div_button.id = app.list.apps[i].id;
        if(desktopID == null){
            div_button.className = "app_button";
        } else {
            div_button.className = "desktop_button";
        }
        div_button.title = "Create a new '" + app.list.apps[i].name + "' application; " + app.list.apps[i].description;
        div_button.onclick = createApplication;
        appParent.appendChild(div_button);

        var div_icon = document.createElement('img');
        div_icon.className = "app_button_icon";
        div_icon.id = app.list.apps[i].id;
        if(desktopID == null){
            div_icon.src = "computer-icon.png";
        } else {
            div_icon.src = "eye-icon.png";
        }
        div_button.appendChild(div_icon);

        var div_name = document.createElement('div');
        div_name.className = "app_button_name";
        div_name.id = app.list.apps[i].id;
        div_name.innerHTML = app.list.apps[i].name;
        div_button.appendChild(div_name);
    }
}

app.createSession = function(application_id){
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            console.log(request.responseText);
        } 
        if(request.status == 449 || request.status == 500){
            setLogMsg("Creating desktop...");
            setTimeout(function(){app.createSession(application_id);},1000);
        }
    }
    request.open("POST", "data/create", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.session_id = app.id;
    jsonBody.application_id = application_id;
    request.send(JSON.stringify(jsonBody));
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

app.hasDesktop = function(app_id){
    for(var i=0;i<app.desktops.length;i++){
        if(app.desktops[i].app_id == app_id){
            return app.desktops[i].id;
        }
    }
    return null;
}

var createApplication = function(){
    var appID = event.srcElement.id;
    var desktopID = app.hasDesktop(appID);
    if(desktopID == null){
        app.createSession(appID);
    } else {
        openTunnel(desktopID);
    }
}
var openTunnel = function(desktopID){
    console.log("open guacamole for id " + desktopID); 
    var request = new XMLHttpRequest();
    request.onload = function(){
        if(request.status == 200){
            connectTunnel();
            document.getElementById("logged").style.visibility='hidden';
            document.getElementById("login").style.visibility='hidden';
            document.getElementById("session").style.visibility='visible';
            app.loginStatus = "session";
            keyboard.onkeydown = function (keysym) {
                guac.sendKeyEvent(1, keysym);
            };

            keyboard.onkeyup = function (keysym) {
                guac.sendKeyEvent(0, keysym);
            };
        } 
    }
    request.open("POST", "data/set", true);
    request.setRequestHeader("Content-Type","application/json");
    var jsonBody = {};
    jsonBody.session_id = app.id;
    jsonBody.desktop_session_id = desktopID;
    request.send(JSON.stringify(jsonBody));
}
var closeTunnel = function(){
    document.getElementById("session").style.visibility='hidden';
    document.getElementById("logged").style.visibility='visible';
    app.loginStatus = "logged";
    delete keyboard.onkeydown;
    delete keyboard.onkeyup;
}


var doLogIn = function(){
    var username = document.getElementById("username_field").value;
    var password = document.getElementById("password_field").value;
    app.login(username, password);
}
var doLogOut = function(){
    document.getElementById("password_field").value = "";
    document.getElementById("login").style.visibility='visible';
    document.getElementById("logged").style.visibility='hidden';
    closeTunnel();
    app.logout();
    app.clearList();
}

var setLogMsg = function(msg){
    document.getElementById("log_field").innerHTML = msg;
    setTimeout(function(){clearLogMsg();},3000);
}
var clearLogMsg = function(msg){
    document.getElementById("log_field").innerHTML = "";
}
