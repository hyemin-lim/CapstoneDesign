//require
const http = require('http');
const express = require('express');
const https = require('https');
const mysql = require('mysql');
const app = express();
const connection = mysql.createConnection({
    host: 'capstonedesign.c29qjkumbsjv.us-west-2.rds.amazonaws.com',
    user: 'admin',
    password: "SXJKZ8YQsh4qwD7324IC",
    database: "capstonedesign",
});
app.get('/', (req, res) =>{
    res.send('server made by express.js');

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
        app.get('/api', (req, res) =>{

            res.json({temp: result});
        })
    return result;
})
const temp = [
    {
        street_light_id: '가족공원길01',
        street_light_x: 126.93194528072515,
        street_light_y: 37.50864875768504
    },
    {
        street_light_id: '구릉길01',
        street_light_x: 126.92559084395599,
        street_light_y: 37.49901025039379
    },
    {
        street_light_id: '국사봉길01',
        street_light_x: 126.92781589891038,
        street_light_y: 37.49602479499344
    },
    {
        street_light_id: '글벗길01',
        street_light_x: 126.94372446293544,
        street_light_y: 37.49419156123445
    },
    {
        street_light_id: '글벗길d1',
        street_light_x: 126.94375116800859,
        street_light_y: 37.492508943325774
    },
    {
        street_light_id: '남성시장01',
        street_light_x: 126.97846532567425,
        street_light_y: 37.488702802186246
    },
    {
        street_light_id: '노들로1-1',
        street_light_x: 126.93010281133252,
        street_light_y: 37.51443933671366
    },
    {
        street_light_id: '대방로01',
        street_light_x: 126.92534094393551,
        street_light_y: 37.5120460594827
    },
    {
        street_light_id: '동작대로01',
        street_light_x: 126.98100964743657,
        street_light_y: 37.47678965034259
    },
    {
        street_light_id: '동작우체국길01',
        street_light_x: 126.91991591984225,
        street_light_y: 37.49757461557787
    },
    {
        street_light_id: '등용길d01',
        street_light_x: 126.93768315124267,
        street_light_y: 37.504358635315
    },
    {
        street_light_id: '만양로01',
        street_light_x: 126.94415384429915,
        street_light_y: 37.51360843173909
    },
    {
        street_light_id: '문화길01',
        street_light_x: 126.93013614949561,
        street_light_y: 37.508100341193874
    },
    {
        street_light_id: '보라매로1-1',
        street_light_x: 126.92647119319315,
        street_light_y: 37.492708936454974
    },
    {
        street_light_id: '사당로01',
        street_light_x: 126.95469321429921,
        street_light_y: 37.49574645922939
    },
    {
        street_light_id: '삼일초등학교 후문(표찰무) 1',
        street_light_x: 126.97517208458551,
        street_light_y: 37.488708913577106
    },
    {
        street_light_id: '서달로01',
        street_light_x: 126.96161941411458,
        street_light_y: 37.50707906110375
    },
    {
        street_light_id: '솔밭로01',
        street_light_x: 126.9687535755195,
        street_light_y: 37.48585795248125
    },
    {
        street_light_id: '시흥대로01',
        street_light_x: 126.90359105343326,
        street_light_y: 37.4850879774328
    },
    {
        street_light_id: '신림로01',
        street_light_x: 126.92835890062025,
        street_light_y: 37.49936628211989
    },
    {
        street_light_id: '신상도길01',
        street_light_x: 126.94737720014732,
        street_light_y: 37.50313618208771
    },
    {
        street_light_id: '알마타길01',
        street_light_x: 126.92509307271968,
        street_light_y: 37.51107957559434
    },
    {
        street_light_id: '약샘길01',
        street_light_x: 126.91374328861416,
        street_light_y: 37.493007122064014
    },
    {
        street_light_id: '장승배기길1',
        street_light_x: 126.94010280762537,
        street_light_y: 37.51325100088487
    },
    {
        street_light_id: '중앙대길01',
        street_light_x: 126.95074636879143,
        street_light_y: 37.50472340446776
    },
    {
        street_light_id: '청운길01',
        street_light_x: 126.95273277635305,
        street_light_y: 37.49776394577089
    },
    {
        street_light_id: '학수길01',
        street_light_x: 126.97720106866385,
        street_light_y: 37.48393397908837
    },
    {
        street_light_id: '흑석로01',
        street_light_x: 126.96080191130416,
        street_light_y: 37.50775454635862
    },
    {
        street_light_id: '흑석로1',
        street_light_x: 126.96346509439766,
        street_light_y: 37.50847845646283
    },
    {
        street_light_id: '흑석한강로01',
        street_light_x: 126.96677693255906,
        street_light_y: 37.50675175743001
    }
]


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
