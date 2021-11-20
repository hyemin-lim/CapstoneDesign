//require
require("dotenv").config();
const http = require('http');
const req = require('request');
const express = require('express');
const https = require('https');
const mysql = require('mysql');
const app = express.Router();

const bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));

//create DB connection
const connection = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PW,
    database: process.env.DB_NAME,
});
//test page
app.get('/', (req, res) =>{
    res.send('server made by express.js');

})

//receive data to client
app.post('/post', (req, res)=>{
    console.log('receive data');

    var inputData = req.body;
    console.log(inputData);
    let review;
    if(req.body.report == "좋음"){
        review = "TRUE";
    }
    else{
        review = "FALSE";
    }
    let lat = req.body.lat;
    let lng = req.body.lng;
    var q = "insert into restaurant_review(review, latitude, longitude) values (" + review + "," + lat.toString() + "," + lng.toString() + ");";
    sendDBquery(q);
/*
    req.on('data', (data) => {
        console.log(data);
        inputData += data.toString('utf8');
    });

    req.on('end', () => {
        console.log("email : "+inputData);


    });
*/
})

//get data from OPEN API and insert them into the DB
var request = require('request');
function getStreetLightdata(url){
    return new Promise(resolve => {
        request(url, function(error, response, body){
                if(!error && response.statusCode==200){
                    console.log(body);
                    const obj = JSON.parse(body);
                    const data_length = obj.currentCount;
                    const data = obj.data;
                    //connection.connect();

                    for(let i = 0; i < data_length; i++){
                        var query = "insert into street_light(street_light_id, street_light_x, street_light_y) values (?, ?, ?);" //쿼리
                        var params = [data[i].표찰번호, data[i].X좌표, data[i].Y좌표]; //쿼리 변수
                        connection.query(query, params, function(err, res, fields){
                            if(err){ //에러 처리
                                console.log("query request failed");
                                console.log(err);
                                return ;
                            }

                            console.log(res);
                        });
                    }
                    //connection.end();


                }

            });
    })
}
function getAPI(url){
    return new Promise(resolve => {
        request(url, function(error, response, body){
            if(!error && response.statusCode==200){
                const obj = JSON.parse(body);
                const total_length = obj.CrtfcUpsoInfo.list_total_count;
                resolve(total_length);
                const data = obj.CrtfcUpsoInfo.row;
                //connection.end();
                for(let i = 0; i < (data.length); i++){
                    var query = "insert into restaurant(restaurant_id, restaurant_x, restaurant_y, level, restaurant_name) values (?, ?, ?, ?, ?);" //쿼리
                    var params = [data[i].CRTFC_UPSO_MGT_SNO, data[i].X_CNTS, data[i].Y_DNTS, data[i].CRTFC_GBN_NM, data[i].UPSO_NM]; //쿼리 변수
                    if(data[i].CGG_CODE_NM == '동작구' && data[i].X_CNTS != '0' && data[i].Y_DNTS != '0'){
                        connection.query(query, params, function(err, res, fields){
                            if(err){ //에러 처리
                                console.log("query request failed");
                                console.log(err);
                                return ;
                            }

                            console.log(res);
                        });
                    }

                }


            }

        });
    })
}
function getAPIdata(url){
    return new Promise(resolve => {
        request(url, function(error, response, body){
            if(!error && response.statusCode==200){
                const obj = JSON.parse(body);
                resolve(obj);
            }

        });
    })
}

function putJaywalkingIntoDB(){
    var url = 'http://apis.data.go.kr/B552061/jaywalking/getRestJaywalking';
    for (let i = 2012; i < 2020; i++){
        var queryParams = '?' + encodeURIComponent('serviceKey') + '=to8ZD63xPeCI3fmOrg%2B8ou7NFlDwTIeVI1w6EzhcG8PGxaPCaALhdSIXHiK7k4Ltr6yDPlaac8ywfdvpYQRzzQ%3D%3D'; /* Service Key*/
        queryParams += '&' + encodeURIComponent('searchYearCd') + '=' + encodeURIComponent(i.toString()); /*2012~2019 */
        queryParams += '&' + encodeURIComponent('siDo') + '=' + encodeURIComponent('11'); /* */
        queryParams += '&' + encodeURIComponent('guGun') + '=' + encodeURIComponent('590'); /* */
        queryParams += '&' + encodeURIComponent('type') + '=' + encodeURIComponent('json'); /* */
        queryParams += '&' + encodeURIComponent('numOfRows') + '=' + encodeURIComponent('10'); /* */
        queryParams += '&' + encodeURIComponent('pageNo') + '=' + encodeURIComponent('1'); /* */
        getAPIdata(url + queryParams)
            .then((obj) => {
                for (let i = 0; i < obj.totalCount; i++){
                    sendDBquery("insert into traffic_danger_zone(traffic_danger_zone_center_x, traffic_danger_zone_center_y) values ("+obj.items.item[i].lo_crd+","+obj.items.item[i].la_crd+");");
                }
            });
    }
}
//putJaywalkingIntoDB();
//getStreetLightdata('https://api.odcloud.kr/api/15037330/v1/uddi:a4e532b3-cacf-4644-96cb-9a51a2faf8b1?page=3&perPage=10&serviceKey=tl%2BhIv%2B1ffnwnlQz3Gwp%2FmF9GzGV%2B%2F4LomNKhm%2BmxUEqCj6UxPmCcil4SQ9tKnmPvMqf2BfhIfn8mujjd2rNtg%3D%3D');
/*
getAPIdata('http://openapi.seoul.go.kr:8088/744e754e486672653332464e674b4a/json/CrtfcUpsoInfo/1/1000/')
    .then((total_length) => {
        let i = 1;
        for(; i < (total_length/1000); i++){
            console.log(i);
            const start = i*1000+1;
            const end = (i+1)*1000;
            let url;
            if(end < total_length){
                url = 'http://openapi.seoul.go.kr:8088/744e754e486672653332464e674b4a/json/CrtfcUpsoInfo/'+start.toString()+'/'+end.toString()+'/';
            }
            else{
                url = 'http://openapi.seoul.go.kr:8088/744e754e486672653332464e674b4a/json/CrtfcUpsoInfo/'+start.toString()+'/'+total_length.toString()+'/';
            }
            getAPIdata(url);
        }

    })
 */
//get data from the DB
function sendDBquery(query){
    return new Promise(resolve => {
        //connection.connect();
        connection.query(query, function(err, res, fields){
            if(err){
                console.log("query request failed");
                console.log(err);
                //connection.end();
                return ;
            }
            const jsonres = JSON.parse(JSON.stringify(res));

            resolve(jsonres);

        });
        //connection.end();
    });
}
/*
getDBdata()
    .then(function(result){
        //send data to client
        app.get('/street_light_api', (req, res) =>{
            res.json({street_light: result});
        })
})
*/
app.get('/street_light_api', (req, res) =>{
    sendDBquery("select * from street_light;")
        .then(function(result){
            res.json({street_light: result});
        })

})
app.get('/restaurant_api', (req, res) => {
    sendDBquery("select * from restaurant;")
        .then((result) => {
            res.json({restaurant: result});
        })
})

app.get('/restaurant_review', (req, res) => {
    sendDBquery("select * from restaurant_review;")
        .then((result) => {
            res.json({restaurant_review: result});
        })
})
app.get('/traffic_danger_zone', (req, res) => {
    sendDBquery("select * from traffic_danger_zone;")
        .then((result) => {
            res.json({traffic_danger_zone: result});
        })
})


module.exports = app;
/*

const http = require('http');
const https = require('https');
const mysql = require('mysql');
const { API_HOST_NAME, API_URL_PATH, DATABASE_KEY} = process.env; // 람다 환경변수 값을 읽어옴.
const api_options = { host: API_HOST_NAME, path: API_URL_PATH, method: 'GET' };
const connection = mysql.createConnection({
  host: config.npd.host,
  user: config.npd.user,
  password: config.npd.password,
  database: config.npd.database,
});
exports.handler = async function(event, context) {
    const promise = new Promise(apiRequest);
    return promise;
};

function apiRequest() {
    let start_date = new Date();
    let end_date = new Date();
    let body_data = '';
    console.log('start.');

    const request = http.request(api_options, function(response) {
        console.log('response.statusCode : ' + response.statusCode);

        if(response.statusCode != 200) {
            console.log('Failed', 'response.statusCode : ' + response.statusCode);
            return;
        }

        // 응답 데이타를 받을때
        response.on('data', function(body_chunk) {
            console.log('response.on.body : ' + body_chunk);
            const obj = JSON.parse(body_chunk);
            const data_length = obj.currentCount;
            const data = obj.data;
            connection.connect();
            connection.query("select * from street_light", function(err, res, fields){
                if(err){
                    console.log("query request failed");
                    console.log(err);
                    return ;
                }

                console.log(res[0]);
            })
        });

        // 응답 데이타가 끝났을때
        response.on('end', function() {
            if(body_data.startsWith('OK')) {
                end_date = new Date();
                let duration_time = (end_date - start_date) / 1000;
                console.log('Success (' + (duration_time) + '초)', body_data);
            } else {
                console.log('Failed', body_data);

            }
        });
    });
    // 요청 timeout
    request.on('timeout', function() {
        console.log('timeout');
        //request.abort();
        });
        // 요청 에러
        request.on('error', function(error) {
            console.log('api call failed');
            console.log(error);

        });
        request.end();

}
"insert into user_info(user_id,user_pwd) values ("+event.userName+","+event.request.userAttributes.string+");"
*/

