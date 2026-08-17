apz.app.onShown_Reward = () => {

    console.log("Current User ID :", apz.app.userId);

    apz.server.callServer({

        appId: apz.app.appId,

        ifaceName: "GetReward",
        
        paintResp: "Y",

        req: {

            userId: apz.app.userId

        },

        callBack: getRewardCB

    });

};


getRewardCB = (resp) => {

    console.log("GetReward Response :", resp);

    if (resp.errors) {

        console.log("GetReward Error :", resp.errors);

        apz.dispMsg({
            type: "E",
            message: resp.errors[0].errorMessage
        });

        return;
    }

    console.log("GetReward Success :", resp);

};
