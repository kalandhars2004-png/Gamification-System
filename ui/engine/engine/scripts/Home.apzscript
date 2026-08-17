CreateT = function () {

    let params = {};

    params.scr = "CreateT";

    apz.launchScreen(params);

};

RewardPage = function () {

    let params = {};

    params.scr = "Reward";

    apz.launchScreen(params);

};

apz.app.onShown_Home = () =>{
    
    
    
    
    apz.server.callServer({

        appId: apz.app.appId,

        ifaceName: "GetUser",

        paintResp: "Y",
        
        req : {
            userId : apz.app.userId
            
        },

       
});

    apz.server.callServer({

        appId: apz.app.appId,

        ifaceName: "GetGoal",

        paintResp: "Y",
        
        req: {
            userId :apz.app.userId
        }

       
    });


}
