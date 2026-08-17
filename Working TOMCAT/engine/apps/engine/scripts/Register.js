
Register = () => {

    let name = apz.getElmValue(
        "engine__Register__el_inp_2"
    );

    let email = apz.getElmValue(
        "engine__Register__el_inp_3"
    );

    let password = apz.getElmValue(
        "engine__Register__el_inp_5"
    );

    let accountNo = apz.getElmValue(
        "engine__Register__el_inp_4"
    );

    let balance = apz.getElmValue(
        "engine__Register__el_inp_7"
    );

    let referredBy = apz.getElmValue(
        "engine__Register__el_inp_1"
    );
    
  

    apz.server.callServer({

        appId: apz.app.appId,

        ifaceName: "Register",

        buildReq: "N",

        paintResp: "N",

        req: {

            name: name,

            email: email,

            password: password,

            accountNo: accountNo,

            balance: balance,

            referredBy: referredBy

        },

        callBack: registerCB

    });

};

registerCB = (resp) => {

    console.log("Register :", resp);

    if (resp.errors) {

        apz.dispMsg({
            type: "E",
            message: resp.errors[0].errorMessage
        });

        return;
    }

    // Store newly created user ID
    apz.app.userId = resp.res.engine__Register_Res.userId

    console.log("Registered User ID :", apz.app.userId);

    apz.dispMsg({
        type: "S",
        message: "Registration Successful"
    });

    let params = {};

    params.scr = "Home";

    apz.launchScreen(params);

};
