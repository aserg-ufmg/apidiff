/**
 * Script para inserir as breackin changes detectadas no banco de dados MongoDB
 */
// -------

// Dados para conexão com o MongoDB
var dbUrl = 'mongodb://127.0.0.1:27017/APIs-BreakingChange-survey-resultado';
var db = null;

//Dados da coleção onde os dados serão inseridos.
var nameCollection = "email-repetidos";

var collection = null;

//Bibliotecas para acessar banco de dados e ler o arquivo de entrada.
var lineReader = require('line-reader');
var MongoClient = require('mongodb').MongoClient;

//Insere os dados na coleção.
var insertRegistry = function(registry, last){
	collection.insert([registry], function (err, result) {
		if (err) {
			console.log("ERROR: " + err);
		} 
		else {
			//console.log(registry.category);
		}
		if(last){ // Se último registro, encerra a conexão.  	
			console.log("\nSuccessfully insert data! \n");
			//db.close();
		}
	});
}

var parserFile = function(day, file){

	var i = 1;
	//last == true se fim do arquivo.
	lineReader.eachLine(file, function(line, last) {

		//Quebra a linha pelo separador (;)
		var data = line.split(";");
		console.log(i);
		i++;
		if(data.length >= 12){
			var registry = new Object();
			registry.day = day;
			registry.author	= data[0];
			registry.email	= data[1];
			registry.nameLibrary = data[2];
			registry.changedType = data[3];	
			registry.structureName = data[4];	
			registry.category = data[5];
			registry.idCommit = data[6];
			registry.messageCommit = data[7];
			registry.timestampCommit = data[8];
			registry.timestampProcess = data[9];
			registry.dateProcessFormat = data[10];
			registry.dateProcess = new Date(new Number(registry.timestampProcess));
			registry.dateCommit = new Date(new Number(registry.timestampCommit));
			registry.isBreakingChange = data[11];
			registry.classifierAPI = data[12];
			registry.description = data[13];
			registry.linkGitHubCommit = "https://github.com/"+registry.nameLibrary+ "/commit/" + registry.idCommit;

			console.log(registry.nameLibrary)
			insertRegistry(registry,last);
		}
		else{
			console.log("ERROR: Data invalid! " + i + ": [DAY" + day + "][" + line + "]");
		}
		
	});
}


var initParser = function(){
	console.log("\nStarted process...");
	MongoClient.connect(dbUrl, function(err, dbMongo) {
		  if(err) {
		  	console.log("\nError connecting in " + dbUrl);
		  }
		  else{
		  		db = dbMongo;
			  	collection = dbMongo.collection(nameCollection);
			  	console.log("\nConnected to " + dbUrl);

			  	day = 0002;
			  	file = "./input/survey1/day_1_output.csv"
			  	parserFile(day, file);
			  	console.log("\n\n FIM !!")
		  }

	});
}

initParser();
