
const http = require('http'); 
const https = require('https'); 
const mysql = require('mysql');
const { config } = require('process');
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

