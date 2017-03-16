/**
 * Scrip para inserir as breackin changes detectadas no banco de dados MongoDB
 */
// -------

// Path e nome do arquivo de entrada.
var file = "./output1.csv";

// Dados para conexão com o MongoDB
var dbUrl = 'mongodb://127.0.0.1:27017/APIDiff';
var db = null;

//Dados da coleção onde os dados serão inseridos.
var nameCollection = "piloto_day1_14032017";
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

var parserFile = function(){

	var i = 0;
	//last == true se fim do arquivo.
	lineReader.eachLine(file, function(line, last) {
	    
		//Quebra a linha pelo separador (;)
		var data = line.split(";");

		if(data.length >= 9){
			var registry = new Object();
			registry.author	= data[0];
			registry.email	= data[1];
			registry.nameLibrary = data[2];
			registry.changedType = data[3];	
			registry.structureName = data[4];	
			registry.category = data[5];
			registry.idCommit = data[6];
			registry.messageCommit = data[7];
			registry.timeCommit = data[8];
			registry.timeProcess = data[9];
			registry.timeProcessFormat = data[10];
			registry.isBreakingChange = data[11];
			registry.classifierAPI = data[12];

			console.log(registry.nameLibrary)
			insertRegistry(registry,last);
		}
		else{
			console.log("ERROR: Data invalid! " + i + ": [" + line + "]");
		}
		i++;
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
			  	console.log("\nReading file " + file + "...\n");
			  	parserFile();
		  }

	});
}

initParser();
