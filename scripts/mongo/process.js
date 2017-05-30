// use APIs-BreakingChange-survey


//[1] Cria coleção com BCs com javadoc:
db.getCollection('day_8').find({
    $and: [
        {"classifierAPI" : "API"},
        {"isBreakingChange" : "true"},
        {"category": {$not: /JAVADOC$/}}
    ]
}).forEach(function (val){
    db.getCollection('day_8_comJavadocCodigo').insert(val)
})

//[2] Remove que já teve email enviado anteriormente.
db.getCollection('day_9_comJavadocCodigo').find({}).forEach(function(val){
    
        var emailJaEnviado = db.getCollection('email_enviados').find({"email" : val.email})[0];
        
        if(emailJaEnviado){
            val.day = "9";
            print(val);
           
           db.getCollection('bcs_nao_enviadas_email_repetido').insert(val);
             
           db.getCollection('day_9_comJavadocCodigo').remove({"_id" : val._id})
        }
        
    
})



//[2]  Agrupa por commit.
//Mudar estruturam, para não repetir dados da BC.
db.getCollection('day_8_comJavadocCodigo').aggregate(
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
    db.getCollection('day_8_comJavadocCodigoGroupCommit').insert(bc)
})


//Exibe dados para análise
var j = 1;
db.getCollection('day_8_comJavadocCodigoGroupCommit').find({}).sort({"author": -1}).forEach(function(val){
   
    print( j +  "; " + val.author + "; " + val.email + "; " + val.breakingChanges[0].linkGitHubCommit + "; " + val.breakingChanges[0].nameLibrary);
           for(var i =0; i < val.breakingChanges.length; i++){
            print(val.breakingChanges[i].changedType + ", " + val.breakingChanges[i].structureName + ", " + val.breakingChanges[i].category)        
        }
    j++
    print("\n")
    
})


//Cria mensagem:
db.getCollection('day_8_comJavadocCodigoGroupCommit').find({"commit" : "9b1ec9f3046091e4651616bba13b7c5e41309098"}).forEach(function(val){
                        
        for(var i =0; i < val.breakingChanges.length; i++){
            var code = "";
            // print(val.breakingChanges[i].category)    

            if(val.breakingChanges[i].category === "ENUM REMOVED"){
    
                code = '<b>Remove Enum</b>:';
                code += '<br>constant <code>' + val.breakingChanges[i].structureName + '</code>';
                code += '<br> removed from enum <code>' + val.breakingChanges[i].changedType + '</code>';

            }   

            if(val.breakingChanges[i].category === "TYPE REMOVED"){
    
                code = '<b>Remove Class</b>:';
                code += '<br>class <code>' + val.breakingChanges[i].structureName + '</code>';
                code += '<br> removed from package <code>' + val.breakingChanges[i].changedType + '</code>';

            }

   
            if(val.breakingChanges[i].category === "METHOD LOST VISIBILITY"){
    
                code = '<b>Loss of Method Visibility</b>:';
                code += '<br>The method <code>' + val.breakingChanges[i].structureName + '</code> changed visibility from <code>public</code> to <code>default</code>'
                code += ' in class <code>' + val.breakingChanges[i].changedType + '</code>';

            }

            if(val.breakingChanges[i].category === "TYPE LOST VISIBILITY"){
    
                code = '<b>Loss of Class Visibility</b>:';
                code += '<br>class <code>' + val.breakingChanges[i].structureName + '</code><br>changed visibility from <code>public</code> to <code>default</code>';

            }
            
            if(val.breakingChanges[i].category === "METHOD CHANGED EXCEPTION"){
                code += '<b>Change Method Exceptions</b>:'
                code += 'method <code>' + val.breakingChanges[i].structureName + '</code><br>changed the list exception from <code>IOException</code> to <code>IOException</code>'
                code += '<br>in class <code>' + val.breakingChanges[i].changedType + '</code>';
            }
            
            if(val.breakingChanges[i].category === "SUPER TYPE CHANGED"){
                code += '<b>Change Superclass</b>:'
                code += 'class <code>' + val.breakingChanges[i].structureName + '</code> changed to <code>' + val.breakingChanges[i].structureName + '</code>'
                code += '<br>in class <code>' + val.breakingChanges[i].changedType + '</code>';
            }
              
            if(val.breakingChanges[i].category === "CONSTRUCTOR CHANGED PARAMETERS"){
                code += '<b>Change Constructor Parameters</b>:'
                code += 'constructor <code>' + val.breakingChanges[i].structureName + '</code> changed the list parameters to <code>' + val.breakingChanges[i].structureName + '</code>'
                code += '<br>in class <code>' + val.breakingChanges[i].changedType + '</code>';
            }
            
            if(val.breakingChanges[i].category === "TYPE GAIN MODIFIER FINAL"){
                code += '<b>Gain Modifier final</b>:'
                code += 'class <code>' + val.breakingChanges[i].structureName + '</code> got  the modifier final'
                code += '<br> in package <code>' + val.breakingChanges[i].changedType + '</code>';
            }

            if(val.breakingChanges[i].category === "METHOD CHANGED PARAMETERS"){
                code += '<b>Change Method Parameters</b>:'
                code += '<br>method <code>' + val.breakingChanges[i].structureName + '</code><br> changed the list parameters to <code>' + val.breakingChanges[i].structureName + '</code>'
                code += '<br>in class <code>' + val.breakingChanges[i].changedType + '</code>';
            }
            
            
            
            if(code){
                //print(code)
                var bc = val.breakingChanges[i];
                bc.breakingChangeMessage = code;
                print(bc)
            }

        }

})


//atualiza a lista de emails enviados.


db.getCollection('day_8_comJavadocCodigoGroupCommit').find({}).forEach(function(val){
   
        var contato = {};
        contato.author = val.breakingChanges[0].author;
        contato.email = val.breakingChanges[0].email;
        contato.nameLibrary = val.breakingChanges[0].nameLibrary;
        contato.idCommit = val.breakingChanges[0].idCommit;
        contato.day = "2";
        
        db.getCollection('email_enviados').insert(contato)

})
