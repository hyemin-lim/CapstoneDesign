//require
const http = require('http');
const req = require('request');
const express = require('express');
const https = require('https');
const mysql = require('mysql');
const app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));
const connection = mysql.createConnection({
    host: 'capstonedesign.c29qjkumbsjv.us-west-2.rds.amazonaws.com',
    user: 'admin',
    password: "SXJKZ8YQsh4qwD7324IC",
    database: "capstonedesign",
});
app.get('/', (req, res) =>{
    res.send('server made by express.js');

})
app.post('/post', (req, res)=>{
    console.log('receive data');

    var inputData = "";


    req.on('data', (data) => {
        console.log(typeof data);
        inputData += data.toString('utf8');
    });

    req.on('end', () => {
        console.log("email : "+inputData);


    });

    res.send("OK!");
})

app.listen(3000, () =>{
    console.log('http server at port 3000');
})

var request = require('request');
function getdata(url){
    return new Promise(resolve => {
        request(url, function(error, response, body){
                if(!error && response.statusCode==200){
                    console.log(body);
                    const obj = JSON.parse(body);
                    const data_length = obj.currentCount;
                    const data = obj.data;
                    connection.connect();
                    /*
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
                        })
                    }
                    */


                    connection.query("select * from street_light;", function(err, res, fields){
                        if(err){
                            console.log("query request failed");
                            console.log(err);
                            return ;
                        }
                        const jsonres = JSON.parse(JSON.stringify(res));

                        resolve(jsonres);

                    })

                }

            });
    })
}
getdata('https://api.odcloud.kr/api/15037330/v1/uddi:a4e532b3-cacf-4644-96cb-9a51a2faf8b1?page=3&perPage=10&serviceKey=tl%2BhIv%2B1ffnwnlQz3Gwp%2FmF9GzGV%2B%2F4LomNKhm%2BmxUEqCj6UxPmCcil4SQ9tKnmPvMqf2BfhIfn8mujjd2rNtg%3D%3D')
    .then(function(result){
        app.get('/street_light_api', (req, res) =>{
            res.json({street_light: result});
        })

    return result;
})


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
*/

