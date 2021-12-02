var express = require('express');
var http = require("http");


//router
var indexRouter = require('./routes/index');
//익스프레스 객체 생성
var app = express();
//기본 포트를 app 객체에 속성으로 설정
app.set('port', process.env.SERVER_PORT||3000);

app.use('/', indexRouter);

app.use(function(req, res, next){
    next(createError(404));
});

app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.send("error");
    res.end();
});
//익스프레스 서버 시작
http.createServer(app).listen(app.get('port'), function(){
    console.log("express server running: " + app.get('port'));
});
