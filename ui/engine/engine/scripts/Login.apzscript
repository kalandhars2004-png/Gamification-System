Register = function () {

    let params = {};

    params.scr = "Register";

    apz.launchScreen(params);

};


login = () => {

    let email = apz.getElmValue(
        "engine__Login__el_inp_2"
    );

    let password = apz.getElmValue(
        "engine__Login__el_inp_1"
    );

    apz.server.callServer({

        appId: apz.app.appId,

        ifaceName: "Login",

        buildReq: "N",

        paintResp: "N",

        req: {

            email: email,

            password: password

        },

        callBack: loginCB

    });

};

loginCB = (resp) => {

    console.log("Login :", resp);

    if (resp.errors) {

        apz.dispMsg({
            type: "E",
            message: resp.errors[0].errorMessage
        });

        return;
    }

    // Store logged-in user ID globally
    apz.app.userId = resp.res.engine__Login_Res.userId

    console.log("Logged User ID :", apz.app.userId);

    apz.dispMsg({
        type: "S",
        message: "Login Successful"
    });

    let params = {};

    params.scr = "Home";

    apz.launchScreen(params);

};
