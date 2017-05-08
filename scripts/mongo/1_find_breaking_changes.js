use APIs-BreakingChange-survey

db.getCollection('day_1').find({
    $and: [
        {"classifierAPI" : "API"},
        {"isBreakingChange" : "true"},
        {"category": {$not: /JAVADOC$/}}
    ]
})