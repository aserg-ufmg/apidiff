/**
 * Script para inserir as breaking changes detectadas no banco de dados MongoDB
 */

// Path e nome do arquivo de entrada.
var file = "./input/pos-survey/email-repetidos.csv";

// Dados para conexão com o MongoDB
var dbUrl = 'mongodb://127.0.0.1:27017/APIs-BreakingChange-survey-resultado';
var db = null;

//Dados da coleção onde os dados serão inseridos.
var nameCollection = "email-repetidos";
//var nameCollection = "day_40";
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

	var i = 1;
	//last == true se fim do arquivo.
	lineReader.eachLine(file, function(line, last) {

		//Quebra a linha pelo separador (;)
		var data = line.split(",");
		console.log(i);
		i++;
		var registry = new Object();

		//emails-enviados-total-grafico.csv
		var n = data[0];
		registry.n	= Number(n);
		registry.origin = data[1];
		registry.with_answer = data[2];
		registry.data_envio_dia_da_semana_real = data[3];
		registry.data_envio_real = data[4];
		registry.data_envio_proximo_dia_util = data[5];
		registry.hora_envio_real = data[6];
		registry.data_hora_envio_real = data[7];
		registry.data_resposta_dia_da_semana = data[8];
		registry.data_resposta_real = data[9];
		registry.data_resposta_proximo_dia_util = data[10];
		registry.hora_resposta_real = data[11];
		registry.data_hora_resposta_real = data[12];
		registry.email = data[13];
		registry.library = data[14];
		registry.link_commit = data[15];

		if (!registry.data_envio_proximo_dia_util){
			registry.data_envio_proximo_dia_util = registry.data_envio_real;
		}

		if (!registry.data_resposta_proximo_dia_util){
			registry.data_resposta_proximo_dia_util = registry.data_resposta_real;
		}

		console.log(registry.n)
		insertRegistry(registry,last);
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
