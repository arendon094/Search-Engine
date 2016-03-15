var express = require('express');
var path = require('path');
var app = express();
var MongoClient = require('mongodb').MongoClient;
var collection = require('mongodb').Collection;
var db;
var url = 'mongodb://localhost:27017/CrawledData'
var Promise = require('bluebird');

Promise.promisifyAll(MongoClient);
Promise.promisifyAll(collection.prototype);

app.use(express.static(__dirname + '/public'));
app.use('/images', express.static(__dirname + '/../CrawlerStorage'));

MongoClient.connect(url, function(err, database) {

	if (err) {
		console.log('Something went wrong. Is MongoDB running?');
		throw err;
	}

	app.set('mongo', database);
	require('./routes')(app);

	app.listen(3000, function(){
		console.log('Listening on port 3000');
	});

});
