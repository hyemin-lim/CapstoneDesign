//require
const express = require('express');
const app = express();

app.get('/', (req, res) =>{
    res.send('server made by express.js');
})

app.listen(3000, () =>{
    console.log('http server at port 3000');
})