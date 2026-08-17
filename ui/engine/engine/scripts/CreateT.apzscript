createTransaction = () => {

    let amount = apz.getElmValue(
        "engine__CreateT__el_inp_3"
    );

    let toUserId = apz.getElmValue(
        "engine__CreateT__el_inp_1"
    );

    if (!amount || !toUserId) {

        apz.dispMsg({
            type: "E",
            message: "Please enter amount and receiver user ID"
        });

        return;
    }

    apz.server.callServer({

        appId: apz.app.appId,

        ifaceName: "CreateT",

        req: {

            userId: apz.app.userId,

            amount: amount,

            type: "TRANSACTION",

            toUserId: toUserId

        },

        callBack: createTransactionCB

    });

};


createTransactionCB = (resp) => {

    console.log("Transaction Response :", resp);

    if (resp.errors) {

        apz.dispMsg({
            type: "E",
            message: resp.errors[0].errorMessage
        });

        return;
    }

    apz.dispMsg({
        type: "S",
        message: "Transaction Successful"
    });

    let params = {};

    params.scr = "Home";

    apz.launchScreen(params);

};
