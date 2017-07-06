// use APIs-BreakingChange-survey

var day = '38';

//[1] Cria coleção com BCs com javadoc:
db.getCollection('day_'+ eval(day)).find({
	$and: [
	{"classifierAPI" : "API"},
	{"changedType": { $not: /^\d*$/}},
	{"isBreakingChange" : "true"},
	{"category": {$not: /JAVADOC$/}}
	]
}).forEach(function (val){
	db.getCollection('day_' + eval(day) + '_comJavadocCodigo').insert(val)
})

//[2] Remove que já teve email enviado anteriormente.
db.getCollection('day_' + eval(day) + '_comJavadocCodigo').find({}).forEach(function(val){
	var emailJaEnviado = db.getCollection('email_enviados').find({ $or: [ {"email" : val.email}, {"author" : val.author} ] })[0];
	if(emailJaEnviado){
		val.day = day ;
		print(val);
		db.getCollection('bcs_nao_enviadas_email_repetido').insert(val);
		db.getCollection('day_' + eval(day) + '_comJavadocCodigo').remove({"_id" : val._id})
	}
})

//[3]  Agrupa por commit.
//Mudar estruturam, para não repetir dados da BC.
db.getCollection('day_' + eval(day) + '_comJavadocCodigo').aggregate(
	[
	{ $group : { _id : "$idCommit", breakingChanges: { $push: "$$ROOT" } } }
	]
	).result.forEach(function(val){ 
		var bc = {};
		bc.commit = val._id;
		bc.breakingChanges = val.breakingChanges;
		bc.author = val.breakingChanges[0].author;
		bc.email = val.breakingChanges[0].email;
		print(bc)
		db.getCollection('day_' + eval(day) + '_comJavadocCodigoGroupCommit').insert(bc)
	})

//[4] atualiza a lista de emails enviados.
db.getCollection('day_' + eval(day) + '_comJavadocCodigoGroupCommit').find({}).forEach(function(val){
	var contato = {};
	contato.author = val.breakingChanges[0].author;
	contato.email = val.breakingChanges[0].email;
	contato.nameLibrary = val.breakingChanges[0].nameLibrary;
	contato.idCommit = val.breakingChanges[0].idCommit;
	contato.day = day;
	db.getCollection('email_enviados').insert(contato)

})

//[5] Exibe dados para análise
var j = 1;
db.getCollection('day_26_comJavadocCodigoGroupCommit').find({}).sort({"author": -1}).forEach(function(val){
	print( j +  "; " + val.author + "; " + val.email + "; " + val.breakingChanges[0].linkGitHubCommit + "; " + val.breakingChanges[0].nameLibrary);
	for(var i =0; i < val.breakingChanges.length; i++){
		if(val.breakingChanges[i].changedType ){
			print(val.breakingChanges[i].changedType + ", " + val.breakingChanges[i].structureName + ", " + val.breakingChanges[i].category)        
		}
	}
	j++
	print("\n")
})

